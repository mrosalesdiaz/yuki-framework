package services.products.verticle;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import io.vertx.core.AbstractVerticle;
import services.products.controller.CategoriesController;
import services.products.controller.ProductsController;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;

public class ProductsVerticle extends AbstractVerticle {

	@Inject
	private ProductsController productsController;

	@Inject
	private CategoriesController categoriesController;

	@Inject
	private DbConfigurator dbConfigurator;

	@Override
	public void start() throws Exception {

		final var verticleInjector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				this.bind(Db.class).asEagerSingleton();
			}
		});

		verticleInjector.injectMembers(this);

		this.dbConfigurator.init(this.config(), this.vertx);

		final var eb = this.vertx.eventBus();

		eb.consumer("/bus/product/category:create", this.categoriesController::create);
		eb.consumer("/bus/products:search", this.productsController::search);
		eb.consumer("/bus/products:create", this.productsController::create);
		eb.consumer("/bus/products:update", this.productsController::update);
		eb.consumer("/bus/products:delete", this.productsController::delete);
		eb.consumer("/bus/products:read", this.productsController::read);
	}

}
