package services.products.verticle.data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.vertx.core.MultiMap;
import io.vertx.core.http.impl.headers.VertxHttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.Db;

public class QueryInvocator implements InvocationHandler {

	@Inject
	private Db db;

	private String query = " select '' as result ";
	private Map<String, Object> parameters = new HashMap<>();
	private final MultiMap headers = new VertxHttpHeaders();
	private Class<?> returnType;

	public void configure(final String query, final Class<?> returnType, final Map<String, Object> parameters) {
		this.query = query;
		this.parameters = parameters;
		this.returnType = returnType;
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
		return null;
	}

	private Object getHeaders() {
		return this.headers;
	}

	private void setParameter(final String parameterName, final Object parameterValue) {
		this.parameters.put(parameterName, parameterValue);

	}

	private Object executeQuery() throws SQLException {
		try (var connection = this.db.getConnection()) {
			try (var preparedStatement = connection.prepareStatement(this.query)) {
				try (var resultSet = preparedStatement.executeQuery()) {
					if (this.returnType.equals(JsonArray.class)) {
						return new JsonArray();
					} else {
						return new JsonObject();
					}
				}
			}
		}
	}

}
