package services.products.verticle.daos;

import io.vertx.core.MultiMap;

public interface ListProductsQuery {

	ListProductsQuery setStatus(String string);

	MultiMap getHeaders();

	<T> T execute(Class<T> type);

}
