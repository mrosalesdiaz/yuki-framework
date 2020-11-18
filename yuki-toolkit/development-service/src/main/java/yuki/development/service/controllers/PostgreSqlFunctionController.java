package yuki.development.service.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import yuki.development.service.dataaccess.Db;

public class PostgreSqlFunctionController {

	private static final String COLUMN_PARAMETERS = "functionArguments";

	private static final String COLUMN_FUNCTION_NAME = "functionName";

	private static final String COLUMN_CLASS_NAME = "className";

	protected static final String COLUMN_PARAMETER_TYPE = "type";

	@Inject
	private Db db;

	@Inject
	private RowProcessor rowProcessor;

	public void getPostgreSqlFunctions(final RoutingContext rc) {
		try {
			final var schema = rc.request()
					.getParam("schemaName");
			final String query = Resources.toString(Resources
					.getResource(this.getClass(), "/sql/get-functions-definitions.sql"), Charsets.UTF_8);
			this.db.getConnection()
					.query(query)
					.execute(this.rowProcessor.asJsonArray(a -> {
						if (a.failed()) {
							rc.fail(a.cause());
							return;
						}

						var responseEntity = new JsonArray();

						try {
							responseEntity = a.result()
									.stream()
									.map(JsonObject.class::cast)
									.filter(o -> o.getString("schemaName", "")
											.equalsIgnoreCase(schema))
									.map(this::postProcessClassName)
									.map(this::postProcessParameterType)
									.sorted(this.postSortingForSuffixNaming())
									.map(this.postProcessAddClassNameSuffix(new HashMap<>()))
									.collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::add));

						} catch (final Exception e) {
							rc.fail(e);
							return;
						}

						rc.response()
								.setStatusCode(200)
								.putHeader("content-type", "application/json; charset=utf-8")
								.end(responseEntity.toString());

					}));
		} catch (final SQLException | IOException e) {
			rc.fail(e);
		}
	}

	private Comparator<JsonObject> postSortingForSuffixNaming() {
		return new Comparator<>() {

			@Override
			public int compare(final JsonObject o1, final JsonObject o2) {
				return this.buildNamePlusParameters(o1)
						.compareTo(this.buildNamePlusParameters(o2));
			}

			private String buildNamePlusParameters(final JsonObject o) {
				return String.format("%s_%s", o.getString(PostgreSqlFunctionController.COLUMN_CLASS_NAME), o
						.getJsonArray(PostgreSqlFunctionController.COLUMN_PARAMETERS)
						.stream()
						.map(JsonObject.class::cast)
						.map(i -> i.getString(PostgreSqlFunctionController.COLUMN_PARAMETER_TYPE))
						.collect(Collectors.joining("_"))

				);
			}
		};
	}

	private Function<JsonObject, JsonObject> postProcessAddClassNameSuffix(final Map<String, Integer> nameStore) {

		return obj -> {

			final var string = obj.getString(PostgreSqlFunctionController.COLUMN_FUNCTION_NAME);
			final var className = obj.getString(PostgreSqlFunctionController.COLUMN_CLASS_NAME);

			nameStore.put(string, nameStore.getOrDefault(string, -1) + 1);

			if (nameStore.get(string) > 0) {
				obj.put(PostgreSqlFunctionController.COLUMN_CLASS_NAME, String
						.format("%s%s", className, nameStore.get(string)));
			}

			return obj;
		};
	}

	public void updateSuffixNames(final JsonArray responseEntity) {
		final Map<String, Integer> names = new HashMap<>();
		for (int i = 0; i < responseEntity.size(); i++) {
			final JsonObject obj = responseEntity.getJsonObject(i);
			final var string = obj.getString(PostgreSqlFunctionController.COLUMN_FUNCTION_NAME);
			final var className = obj.getString(PostgreSqlFunctionController.COLUMN_CLASS_NAME);

			names.put(string, names.getOrDefault(string, -1) + 1);

			if (names.get(string) > 0) {
				obj.put(PostgreSqlFunctionController.COLUMN_CLASS_NAME, String
						.format("%s%s", className, names.get(string)));
			}

		}

	}

	private JsonObject postProcessClassName(final JsonObject jsonObject) {
		if (!jsonObject.containsKey(PostgreSqlFunctionController.COLUMN_FUNCTION_NAME)) {
			return jsonObject;
		}

		final JsonArray arrayOfParameterDefinitions = Stream
				.of(jsonObject.getString(PostgreSqlFunctionController.COLUMN_PARAMETERS, "")
						.split(","))
				.map(String::trim)
				.map(s -> s.split(" "))
				.map(s -> new JsonObject().put("name", s[0].trim())
						.put("type", s[1].trim()))
				.collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::add));

		jsonObject.put(PostgreSqlFunctionController.COLUMN_PARAMETERS, arrayOfParameterDefinitions);

		return jsonObject;
	}

	private JsonObject postProcessParameterType(final JsonObject jsonObject) {
		if (!jsonObject.containsKey(PostgreSqlFunctionController.COLUMN_PARAMETERS)) {
			return jsonObject;
		}

		final var functionName = jsonObject.getString(PostgreSqlFunctionController.COLUMN_FUNCTION_NAME, "ErrorName");

		final var className = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, functionName);

		jsonObject.put(PostgreSqlFunctionController.COLUMN_CLASS_NAME, className);

		return jsonObject;
	}

}
