package services.products.verticle;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.base.Stopwatch;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import yuki.framework.dataschema.FlywayMain;

@ExtendWith(VertxExtension.class)
public class ProductsVerticleTest extends TestBase {

	@BeforeEach
	public void prepareDatabase() {
		FlywayMain
				.executeFlayway("clean", "jdbc:postgresql://localhost/db_loyalty", "loyalty", "moresecure", "products", true);
		FlywayMain
				.executeFlayway("migrate", "jdbc:postgresql://localhost/db_loyalty", "loyalty", "moresecure", "products");
	}

	@Test
	public void Should_create_a_category_When_name_is_passed() throws Throwable {

		final var testContext = new VertxTestContext();

		final var vertx = Vertx.vertx();
		final var eventbus = vertx.eventBus();
		final var deploymentOptions = this.createDefaultDeployemntOptions()
				.setWorker(true);
		final var verticleFactory = this.verticleFactory(ProductsVerticle.class);

		// Deploy Verticle
		vertx.deployVerticle(verticleFactory, deploymentOptions, ar -> {

			if (ar.failed()) {
				testContext.failNow(ar.cause());
			}

			final JsonObject newCategory = new JsonObject().put("name", "category-1");

			final Stopwatch stopwatch = Stopwatch.createStarted();

			// Execute command
			eventbus.request("/bus/product/category:create", newCategory, mar -> {
				stopwatch.stop();

				org.assertj.core.api.Assertions.assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS))
						.isLessThan(50);

				if (mar.failed()) {
					testContext.failNow(mar.cause());
				}

				testContext.verify(() -> {

					org.assertj.core.api.Assertions.assertThat(mar.result())
							.isNotNull();
					org.assertj.core.api.Assertions.assertThat(mar.result()
							.body())
							.isNotNull();
					org.assertj.core.api.Assertions.assertThat(mar.result()
							.body())
							.hasSameClassAs(new JsonArray());

					final JsonObject body = (JsonObject) ((JsonArray) mar.result()
							.body()).stream()
									.findFirst()
									.orElseThrow();

					org.assertj.core.api.Assertions.assertThat(body.containsKey("id"))
							.isTrue();
					org.assertj.core.api.Assertions.assertThat(body.containsKey("name"))
							.isTrue();

					org.assertj.core.api.Assertions.assertThat(body.getString("name"))
							.isEqualTo(newCategory.getString("name"));
					org.assertj.core.api.Assertions.assertThat(body.getInteger("id"))
							.isNotNull();

					org.assertj.core.api.Assertions.assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS))
							.isLessThan(100);

				});

				testContext.completeNow();
			});

		});

		org.assertj.core.api.Assertions.assertThat(testContext.awaitCompletion(100, TimeUnit.SECONDS))
				.isTrue();
		if (testContext.failed()) {
			throw testContext.causeOfFailure();
		}
	}

}
