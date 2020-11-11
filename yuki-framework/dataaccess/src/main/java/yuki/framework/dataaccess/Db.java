package yuki.framework.dataaccess;

import java.sql.SQLException;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class Db {
	private PoolOptions poolOptions;
	private PgConnectOptions connectOptions;
	private Vertx vertx;
	private PgPool client;

	void init(final JsonObject configuration, final Vertx vertx) {
		this.connectOptions = PgConnectOptions.fromUri(configuration.getString("jdbcUrl"))
				.setUser(configuration.getString("dbUser")).setPassword(configuration.getString("dbPassword"));

		this.poolOptions = new PoolOptions().setMaxSize(5);
		this.vertx = vertx;
		this.client = PgPool.pool(this.vertx, this.connectOptions, this.poolOptions);
	}

	public PgPool getConnection() throws SQLException {
		return this.client;
	}
}
