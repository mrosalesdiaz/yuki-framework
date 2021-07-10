package yuki.development.service.dataaccess;

import java.sql.SQLException;

import com.google.inject.Singleton;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

@Singleton
public class Db {
	private PoolOptions poolOptions;
	private PgConnectOptions connectOptions;
	private Vertx vertx;
	private PgPool client;

	public Future<Void> configure(final JsonObject configuration, final Vertx vertx) {
		final Promise<Void> promise = Promise.promise();

		this.connectOptions = PgConnectOptions.fromUri(configuration.getString("jdbcUrl"))
				.setUser(configuration.getString("dbUser"))
				.setPassword(configuration.getString("dbPassword"));

		this.poolOptions = new PoolOptions().setMaxSize(5);
		this.vertx = vertx;
		this.client = PgPool.pool(this.vertx, this.connectOptions, this.poolOptions);

		this.preInitialize(promise);

		return promise.future();
	}

	private void preInitialize(final Promise<Void> promise) {
		try {
			this.getConnection()
					.query(" select 'chanchito' ")
					.execute(r -> promise.complete());
		} catch (final SQLException e) {
			promise.fail(e);
		}
	}

	public PgPool getConnection() throws SQLException {
		return this.client;
	}
}
