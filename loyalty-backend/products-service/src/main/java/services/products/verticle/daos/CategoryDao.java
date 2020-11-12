package services.products.verticle.daos;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.utils.QueryExecutor;

public class CategoryDao {

	@Inject
	private QueryExecutor queryExecutor;

	public Future<JsonArray> create(final Message<JsonObject> message) {
		final var query = this.queryExecutor.create(CreateNewCategory.class);

		query.setName(message.body().getString("name"));

		return query.execute();

	}

}
