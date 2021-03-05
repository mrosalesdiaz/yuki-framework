package yuki.framework.dataaccess.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.io.ByteStreams;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import yuki.framework.dataaccess.Db;

public class QueryProxyInvocator implements InvocationHandler {

    private final static Pattern matchNamedParameters = Pattern.compile("\\b(\\w+)\\W*$");

    @Inject
    private Db db;

    private String parameterOrder[];

    private Map<String, Object> parameters;

    private String query = " select '' as result ";

    public void configure(final String query, final Class<?> returnType, final Map<String, Object> parameters) {
        this.query = query;
        final String[] parts = query.split(":=");
        this.parameterOrder = Stream.of(parts)
                .limit(parts.length - 1)
                .map(String::trim)
                .map(this::getLastWord)
                .toArray(String[]::new);
        this.parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.parameters.putAll(parameters);
    }

    private Future<JsonArray> executeQuery() throws SQLException {
        final Promise<JsonArray> promise = Promise.promise();
        final Tuple parameters = Tuple.tuple(Stream.of(this.parameterOrder)
                .map(this.parameters::get)
                .collect(Collectors.toList()));

        if (this.db.getConnection() == null) {
            promise.fail(new NullPointerException("Database connection is null"));
            return promise.future();
        }

        this.db.getConnection()
                .preparedQuery(this.query)
                .execute(parameters, ar -> {
                    if (ar.failed()) {
                        promise.fail(new DatabaseExecutionException(ar.cause()));
                        return;
                    }

                    final JsonArray jsonArray = new JsonArray();

                    ar.result()
                            .forEach(r -> {
                                if (r == null) {
                                    promise.fail(new ProcessDataException("Result from Database is null."));
                                    return;
                                }

                                try {
                                    final JsonObject jsonObject = new JsonObject();

                                    for (int i = 0; i < r.size(); i++) {
                                        jsonObject.put(r.getColumnName(i), this.processColumnValue(r.getValue(i)));
                                    }

                                    jsonArray.add(jsonObject);
                                } catch (final Exception e) {
                                    promise.fail(new ProcessDataException("Error converting row data into json."));
                                }
                            });

                    promise.complete(jsonArray);
                });

        return promise.future();
    }

    private String getLastWord(final String line) {
        final Matcher matcher = QueryProxyInvocator.matchNamedParameters.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException(String.format("Error parsing the parameter from parameter line: %s", line));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();

        if (methodName.equals("execute")) {
            return this.executeQuery();
        }

        if (methodName.startsWith("set")) {
            this.setParameter(methodName.substring(3), args[0]);
        }

        return proxy;
    }

    private Object processColumnValue(final Object value) {
        if (value instanceof Buffer) {
            return ((Buffer) value).getBytes();
        }

        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay()
                    .toInstant(ZoneOffset.ofHours(0));
        }

        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.ofHours(0));
        }

        if (value instanceof UUID) {
            return value.toString();
        }

        return value;

    }

    private void setParameter(final String parameterName, final Object parameterValue) {
        if (parameterValue == null) {
            return;
        }

        if (parameterValue instanceof InputStream) {
            try {
                this.parameters
                        .put(parameterName, Buffer.buffer(ByteStreams.toByteArray((InputStream) parameterValue)));
            } catch (final IOException e) {
                throw new ParameterValueException(
                        String.format("Error setting the parameter value for: %s", parameterName), e);
            }

            return;
        }

        if (parameterValue instanceof Instant) {
            this.parameters.put(parameterName, ((Instant) parameterValue).atOffset(ZoneOffset.ofHours(0))
                    .toLocalDateTime());
            return;
        }

        this.parameters.put(parameterName, parameterValue);
    }

}
