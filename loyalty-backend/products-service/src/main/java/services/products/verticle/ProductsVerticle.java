package services.products.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ProductsVerticle extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		final var eb = this.vertx.eventBus();
		eb.consumer("/bus/products", (final Message<JsonObject> m) -> {
			System.out.println(m.body().getString("q"));
			System.out.println("ProductsVerticle.start()");
			final var deliveryOptions = new DeliveryOptions().addHeader("x-range", "1-100/200");
			m.reply(new JsonArray().add("helo"), deliveryOptions);
		});
	}

}
