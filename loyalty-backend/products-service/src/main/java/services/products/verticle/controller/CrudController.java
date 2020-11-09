package services.products.verticle.controller;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import services.products.verticle.daos.ProductDao;

public class CrudController {

	@Inject
	private ProductDao productDao;

	public void list(final Message<JsonObject> message) {
		message.body().getString("productId");

		this.productDao.list(null);

		this.reply(message::reply, this.productDao::list, message);
	}

	private void reply(final Consumer<Object> object, final Function<DeliveryOptions, JsonArray> fn,
			final Message<JsonObject> message) {
		final var deliveryOptions = new DeliveryOptions();
		message.reply(fn.apply(deliveryOptions), deliveryOptions);
	}

}
