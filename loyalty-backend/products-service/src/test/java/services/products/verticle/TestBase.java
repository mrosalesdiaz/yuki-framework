package services.products.verticle;

import java.util.function.Supplier;

import com.google.inject.Guice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

public class TestBase {

	protected DeploymentOptions createDefaultDeployemntOptions() {
		final var config = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_loyalty?search_path=products")
				.put("dbUser", "loyalty")
				.put("dbPassword", "moresecure");
		return new DeploymentOptions().setConfig(config);
	}

	protected Supplier<Verticle> verticleFactory(final Class<? extends AbstractVerticle> verticleClass) {
		return new Supplier<>() {
			@Override
			public Verticle get() {
				final var injector = Guice.createInjector();
				return injector.getInstance(verticleClass);
			}

		};
	}
}
