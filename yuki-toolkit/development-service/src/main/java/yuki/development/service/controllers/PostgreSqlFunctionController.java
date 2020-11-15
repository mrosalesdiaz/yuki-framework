package yuki.development.service.controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import yuki.development.service.dataaccess.Db;

public class PostgreSqlFunctionController {

	@Inject
	private Db db;

	public void getPostgreSqlFunctions(final RoutingContext rc) {
		try {
			final String query = Resources.toString(Resources
					.getResource(this.getClass(), "/sql/get-functions-definitions.sql"), Charsets.UTF_8);
			this.db.getConnection()
					.query(query)
					.execute(ar -> {
						if (ar.failed()) {
							rc.fail(ar.cause());
						}
						final JsonArray jsonArray = new JsonArray();
						ar.result()
								.forEach(r -> {
									final JsonObject jsonObject = new JsonObject();

									for (int i = 0; i < r.size(); i++) {
										jsonObject.put(r.getColumnName(i), r.getValue(i));
									}

									jsonArray.add(jsonObject);
								});

						rc.response()
								.setStatusCode(201)
								.putHeader("content-type", "application/json; charset=utf-8")
								.end(jsonArray.toString());
					});
		} catch (final SQLException | IOException e) {
			rc.fail(e);
		}
	}
}
