package services.products.verticle;

import javax.inject.Inject;

import io.vertx.core.AbstractVerticle;
import services.products.verticle.controller.CrudController;

public class ProductsVerticle extends AbstractVerticle {

	@Inject
	private CrudController crudController;

	@Override
	public void start() throws Exception {
		final var eb = this.vertx.eventBus();

		eb.consumer("/bus/products", this.crudController::list);
	}

}
