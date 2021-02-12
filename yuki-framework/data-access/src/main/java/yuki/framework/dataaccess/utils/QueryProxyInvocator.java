package yuki.framework.dataaccess.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Stopwatch;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import yuki.framework.dataaccess.Db;

public class QueryProxyInvocator implements InvocationHandler {

	@Inject
	private Db db;

	private String query = " select '' as result ";

	private Map<String, Object> parameters;

	private String parameterOrder[];

	private final MultiMap headers = new VertxHttpHeaders();

	private final static Pattern matchNamedParameters = Pattern.compile("\\b(\\w+)\\W*$");

	Stopwatch preapring;

	public void configure(final String query, final Class<?> returnType, final Map<String, Object> parameters) {
		this.preapring = Stopwatch.createStarted();
		this.query = query;
		final Stopwatch sp = Stopwatch.createStarted();
		final String[] parts = query.split(":=");
		this.parameterOrder = Stream.of(parts).limit(parts.length - 1).map(String::trim).map(this::getLastWord)
				.toArray(String[]::new);
		this.parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.parameters.putAll(parameters);
		System.out.println("Processing parameter: " + sp.elapsed(TimeUnit.MILLISECONDS));
	}

	public String getLastWord(final String line) {
		final Matcher matcher = QueryProxyInvocator.matchNamedParameters.matcher(line);
		if (matcher.find()) {

			return matcher.group(1);
		}

		throw new RuntimeException(String.format("Error parsing the parameter from par line: %s", line));
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final String methodName = method.getName();
		if (methodName.equals("getHeaders")) {
			return this.getHeaders();
		}

		if (methodName.equals("execute")) {
			return this.executeQuery();
		}

		if (methodName.startsWith("set")) {
			this.setParameter(methodName.substring(3), args[0]);
		}
		return proxy;
	}

	private Object getHeaders() {
		return this.headers;
	}

	private void setParameter(final String parameterName, final Object parameterValue) {
		this.parameters.put(parameterName, parameterValue);
	}

	private Future<JsonArray> executeQuery() throws SQLException {
		System.out.println("Preparing: " + this.preapring.elapsed(TimeUnit.MILLISECONDS));
		final Promise<JsonArray> promise = Promise.promise();
		final Tuple parameters = Tuple
				.tuple(Stream.of(this.parameterOrder).map(this.parameters::get).collect(Collectors.toList()));

		final Stopwatch gettingConnection = Stopwatch.createStarted();
		this.db.getConnection().preparedQuery(this.query).execute(parameters, ar -> {
			System.out.println("Getting connection: " + gettingConnection.elapsed(TimeUnit.MILLISECONDS));
			if (ar.failed()) {
				promise.fail(ar.cause());
				return;
			}

			final Stopwatch stopwatch = Stopwatch.createStarted();
			final JsonArray jsonArray = new JsonArray();
			ar.result().forEach(r -> {
				final JsonObject jsonObject = new JsonObject();

				for (int i = 0; i < r.size(); i++) {
					jsonObject.put(r.getColumnName(i), r.getValue(i));
				}

				jsonArray.add(jsonObject);
			});
			System.out.println("Parsing: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
			promise.complete(jsonArray);
		});

		return promise.future();
	}

}
