package yuki.framework.dataaccess;

import com.google.inject.Singleton;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

/**
 * Singleton class to access database connection.
 *
 * It depends on {@link yuki.framework.dataaccess.DbConfigurator} in order to configure url connection.
 *
 * @author mrosalesdiaz
 *
 */
@Singleton
public class Db {
	private PgPool client;

	/**
	 * @return a connection create from database pool
	 */
	public PgPool getConnection() {
		return this.client;
	}

	/**
	 * @param configuration set of parameters to configure the connection. Following
	 *                      parameters are supported: dbUser,dbPassword and jdbcUrl.
	 * @param vertx         vertx instance to use at time pool is created.
	 */
	public void init(final JsonObject configuration, final Vertx vertx) {

		final PgConnectOptions connectOptions = PgConnectOptions.fromUri(configuration.getString("jdbcUrl"))
				.setUser(configuration.getString("dbUser")).setPassword(configuration.getString("dbPassword"));

		final PoolOptions poolOptions = new PoolOptions();
		this.client = PgPool.pool(vertx, connectOptions, poolOptions);
	}
}
