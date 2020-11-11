package services.products.verticle.daos;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import services.products.queries.SearchProductsQuery;
import yuki.framework.dataaccess.utils.QueryExecutor;

public class ProductDao {

	@Inject
	private QueryExecutor queryExecutor;

	public Future<JsonArray> search(final DeliveryOptions deliveryOptions) {
		final var headers = deliveryOptions.getHeaders();
		final var query = this.queryExecutor.create(SearchProductsQuery.class);

		if (deliveryOptions.getHeaders() == null) {
			return query.execute();
		}

		if (headers.contains("name")) {
			query.setName(headers.get("name"));
		}

		return query.execute();

	}

}
