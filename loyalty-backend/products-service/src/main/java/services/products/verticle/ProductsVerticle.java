package services.products.verticle;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import io.vertx.core.AbstractVerticle;
import services.products.verticle.controller.CrudController;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;

public class ProductsVerticle extends AbstractVerticle {

	@Inject
	private CrudController crudController;

	@Inject
	private DbConfigurator configurator;

	@Override
	public void start() throws Exception {

		final var verticleInjector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				this.bind(Db.class).asEagerSingleton();
			}
		});

		verticleInjector.injectMembers(this);

		this.configurator.init(this.config());

		final var eb = this.vertx.eventBus();

		eb.consumer("/bus/products", this.crudController::list);
	}

}
