package yuki.framework.dataaccess;

import java.sql.SQLException;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class DbConfigurator {
	@Inject
	private Db db;

	public Future<Void> init(final JsonObject configuration, final Vertx vertx) {
		final Promise<Void> promise = Promise.promise();

		this.db.init(configuration, vertx);
		this.preInitialize(promise);

		return promise.future();
	}

	private void preInitialize(final Promise<Void> promise) {
		try {
			this.db.getConnection().query(" select 'chanchito' ").execute(r -> promise.complete());
		} catch (final SQLException e) {
			throw new RuntimeException("Error trying to pre-initialize the connection", e);
		}
	}
}
