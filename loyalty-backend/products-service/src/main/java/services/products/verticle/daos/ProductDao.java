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

		final var sqlExecutor = this.queryExecutor.execute(ListProductsQuery.class);

		deliveryOptions.setHeaders(sqlExecutor.getHeaders());

		sqlExecutor.setStatus("active");

		return sqlExecutor.execute(JsonArray.class);
	}

}
