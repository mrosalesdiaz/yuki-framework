package yuki.framework.dataaccess.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.Db;

public class QueryProxyInvocator implements InvocationHandler {

	@Inject
	private Db db;

	private String query = " select '' as result ";

	private Map<String, Object> parameters;

	private final MultiMap headers = new VertxHttpHeaders();

	public void configure(final String query, final Class<?> returnType, final Map<String, Object> parameters) {
		this.query = query;
		this.parameters = parameters;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final var methodName = method.getName();
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

		final Promise<JsonArray> promise = Promise.promise();

		this.db.getConnection().preparedQuery(this.query).execute(ar -> {
			if (ar.failed()) {
				promise.fail(ar.cause());
				return;
			}

			final var jsonArray = new JsonArray();
			ar.result().forEach(r -> {
				for (int i = 0; i < r.size(); i++) {
					jsonArray.add(new JsonObject().put(r.getColumnName(i), r.getValue(i)));
				}
			});
			promise.complete(jsonArray);
		});

		return promise.future();
	}

}
