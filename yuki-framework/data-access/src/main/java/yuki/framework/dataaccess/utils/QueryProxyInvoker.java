package yuki.framework.dataaccess.utils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.annotations.Parameter;

public class QueryProxyInvoker implements InvocationHandler {

    private final static Pattern matchNamedParameters = Pattern.compile("\\b(\\w+)\\W*$");

    @Inject
    private Db db;

    private String[] parameterOrder;

    private Map<String, Object> parameters;

    private String query = " select '' as result ";

    public void configure(final String query, final Map<String, Object> parameters) {
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

    private Future<JsonArray> executeQuery() {
        final Promise<JsonArray> promise = Promise.promise();
        final Tuple parameters = Tuple.tuple(Stream.of(this.parameterOrder)
                .map(this.parameters::get)
                .collect(Collectors.toList()));

        if (this.db.getPgPool() == null) {
            promise.fail(new NullPointerException("Database connection is null"));
            return promise.future();
        }

        this.db.getPgPool()
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
        final Matcher matcher = QueryProxyInvoker.matchNamedParameters.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new RuntimeException(String.format("Error parsing the parameter from parameter line: %s", line));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final String methodName = method.getName();

        if (methodName.equals("execute")) {
            return this.executeQuery();
        }

        if (methodName.startsWith("set")) {
            this.setParameter(methodName.substring(3), args[0], method.getAnnotation(Parameter.class));
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

    private void setParameter(final String parameterName, final Object parameterValue, Parameter parameterAnnotation) {
        if (parameterValue == null) {
            return;
        }
// TODO: Replace this by accessing ParameterType definition
        assert parameterAnnotation != null : "Parameter must have annotation to define database type";

        switch (parameterAnnotation.value()) {
            case BOOLEAN:
                this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (Boolean) parameterValue));
                break;
            case DOUBLE:
                this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (Double) parameterValue));
                break;
            case BYTEA:
                this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (InputStream) parameterValue));
                break;
            case DATETIME:
            case DATE:
            case TIME:
                if (parameterValue instanceof Instant) {
                    this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (Instant) parameterValue));
                } else if (parameterValue instanceof LocalDate) {
                    this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (LocalDate) parameterValue));
                } else if (parameterValue instanceof LocalDateTime) {
                    this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (LocalDateTime) parameterValue));
                } else if (parameterValue instanceof LocalTime) {
                    this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (LocalTime) parameterValue));
                } else {
                    throw new RuntimeException(String.format("Unsupported parameter type: %s", parameterValue.getClass()));
                }

                break;
            case INTEGER:
                this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (Integer) parameterValue));
                break;
            case STRING:
                this.parameters.put(parameterName, ProcessDbParameter.parse(parameterAnnotation, (String) parameterValue));
                break;
        }
    }

}
