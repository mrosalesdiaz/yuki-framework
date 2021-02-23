package yuki.framework.dataaccess;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.utils.DbConfigurationException;

/**
 * Database configuration object it configures the connections and also test the
 * singleton {@link Db}.
 *
 * @author mrosalesdiaz
 *
 */
public class DbConfigurator {
	@Inject
	private Db db;

	/**
	 * @param configuration set of parameters to configure the connection. Following
	 *                      parameters are supported: dbUser,dbPassword and jdbcUrl.
	 * @param vertx         vertx instance to use at time pool is created.
	 */
	public void init(final JsonObject configuration, final Vertx vertx) {
		try {
			this.db.init(configuration, vertx);
			this.preInitialize();
		} catch (final Throwable e) {
			throw new DbConfigurationException("Error trying to pre-initialize the connection", e);
		}
	}

	/**
	 * Test connection by executing a simple dummy query
	 *
	 * @throws Throwable if any unexpected error arises
	 */
	private void preInitialize() throws Throwable {
		final Promise<Void> promise = Promise.promise();

		this.db.getConnection()
				.query(" select 'chanchito' ")
				.execute(r -> {
					if (r.failed()) {
						promise.fail(r.cause());
						return;
					}

					promise.complete();
				});

		final Future<Void> future = promise.future();

		while (!future.isComplete()) {
			try {
				Thread.sleep(50);
			} catch (final Exception e) {
			}
		}

		if (future.failed()) {
			throw future.cause();
		}
	}
}
