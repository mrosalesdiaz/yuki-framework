package services.products.controller;

import javax.inject.Inject;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import services.products.verticle.daos.CategoryDao;

public class CategoriesController {

	@Inject
	private CategoryDao categoryDao;

	public void create(final Message<JsonObject> message) {
		this.categoryDao.create(message).onComplete(ar -> {
			if (ar.failed()) {
				message.fail(500, "Error saving data");
				return;
			}

			final var deliveryOptions = new DeliveryOptions();
			message.reply(ar.result(), deliveryOptions);
		});
	}
}
