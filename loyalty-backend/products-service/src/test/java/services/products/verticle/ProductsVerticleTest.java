package services.products.verticle;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class ProductsVerticleTest {
	@Test
	public void start_server() throws InterruptedException {
		final var testContext = new VertxTestContext();

		final var config = new JsonObject();

		final var deplomentOptions = new DeploymentOptions().setConfig(config).setWorker(true).setInstances(1)
				.setWorkerPoolSize(1);

		final var vertx = Vertx.vertx();

		final var eventBus = vertx.eventBus();

		final var verticle = new ProductsVerticle();

		vertx.deployVerticle(verticle, deplomentOptions, r -> {
			eventBus.request("/bus/products", new JsonObject().put("q", "s"), ac -> {
				System.out.println(" Client ::" + ac.result().body());
				Assertions.assertTrue(this.isJsonArray(ac), "Should be a JsonArray");
				Assertions.assertTrue(this.isNotEmpty(ac), "Should not be empty");
				Assertions.assertTrue(this.isHasPaginationMetadata(ac), "Should has pagination header");
				// Assertions.assertTrue(this.isHasProductLinks(r));

				testContext.completeNow();
			});
		});

		testContext.awaitCompletion(10, TimeUnit.MINUTES);

	}

	private boolean isHasPaginationMetadata(final AsyncResult<Message<Object>> ac) {
		return ac.result().headers().contains("x-range");
	}

	private boolean isNotEmpty(final AsyncResult<Message<Object>> ac) {
		return !JsonArray.class.cast(ac.result().body()).isEmpty();
	}

	private boolean isJsonArray(final AsyncResult<Message<Object>> ac) {
		try {
			return ac.result().body() instanceof JsonArray;
		} catch (final Exception e) {
			return false;
		}
	}
}
