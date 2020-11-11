package yuki.framework.dataaccess;

import javax.inject.Inject;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class DbConfigurator {
	@Inject
	private Db db;

	public void init(final JsonObject configuration, final Vertx vertx) {
		this.db.init(configuration, vertx);
	}
}
