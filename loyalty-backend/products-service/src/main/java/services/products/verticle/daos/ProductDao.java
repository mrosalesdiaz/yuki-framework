package services.products.verticle.daos;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import services.products.verticle.data.QueryExecutor;

public class ProductDao {

	@Inject
	private QueryExecutor queryExecutor;

	public @Nullable JsonArray list(final DeliveryOptions deliveryOptions) {

		try {
			final var query = this.queryExecutor.execute(ListProductsQuery.class);

			query.setName("test");

			deliveryOptions.setHeaders(query.getHeaders());

			return query.execute(JsonArray.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		// deliveryOptions.setHeaders(sqlExecutor.getHeaders());

		// sqlExecutor.setStatus("active");

		// return sqlExecutor.execute(JsonArray.class);
		return null;
	}

}
