package services.products.controller;

import javax.inject.Inject;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import services.products.verticle.daos.ProductDao;

public class ProductsController {

	@Inject
	private ProductDao productDao;

	public void read(final Message<JsonObject> message) {
		message.reply(new JsonObject());
	}

	public void search(final Message<JsonObject> message) {
		final var deliveryOptions = new DeliveryOptions();

		this.productDao.search(deliveryOptions).onComplete(ar -> {
			if (ar.failed()) {
				message.fail(500, "Error getting data");
				return;
			}

			message.reply(ar.result(), deliveryOptions);
		});
	}

	public void create(final Message<JsonObject> message) {
		message.reply(new JsonObject());
	}

	public void update(final Message<JsonObject> message) {
		message.reply(new JsonObject());
	}

	public void delete(final Message<JsonObject> message) {
		message.reply(new JsonObject());
	}
}
