package yuki.development.service.controllers;

import java.io.IOException;
import java.sql.SQLException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class PostgreSqlFunctionController {

	private PoolOptions poolOptions;
	private PgConnectOptions connectOptions;
	private Vertx vertx;
	private PgPool client;

	public Future<Void> configure(final JsonObject configuration, final Vertx vertx) {
		final Promise<Void> promise = Promise.promise();

		this.connectOptions = PgConnectOptions.fromUri(configuration.getString("jdbcUrl"))
				.setUser(configuration.getString("dbUser")).setPassword(configuration.getString("dbPassword"));

		this.poolOptions = new PoolOptions().setMaxSize(5);
		this.vertx = vertx;
		this.client = PgPool.pool(this.vertx, this.connectOptions, this.poolOptions);

		this.preInitialize(promise);

		return promise.future();
	}

	private void preInitialize(final Promise<Void> promise) {
		try {
			this.getConnection().query(" select 'chanchito' ").execute(r -> promise.complete());
		} catch (final SQLException e) {
			promise.fail(e);
		}
	}

	public PgPool getConnection() throws SQLException {
		return this.client;
	}

	public void getPostgreSqlFunctions(final RoutingContext rc) {
		try {
			final String query = Resources.toString(
					Resources.getResource(this.getClass(), "/sql/get-functions-definitions.sql"), Charsets.UTF_8);
			this.getConnection().query(query).execute(ar -> {
				if (ar.failed()) {
					rc.fail(ar.cause());
				}
				final JsonArray jsonArray = new JsonArray();
				ar.result().forEach(r -> {
					final JsonObject jsonObject = new JsonObject();

					for (int i = 0; i < r.size(); i++) {
						jsonObject.put(r.getColumnName(i), r.getValue(i));
					}

					jsonArray.add(jsonObject);
				});

				rc.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
						.end(jsonArray.toString());
			});
		} catch (final SQLException | IOException e) {
			rc.fail(e);
		}
	}
}
