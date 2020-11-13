package services.products.verticle;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.base.Stopwatch;
import com.google.inject.Guice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import yuki.framework.dataschema.FlywayMain;

@ExtendWith(VertxExtension.class)
public class ProductsVerticleTest {

	@BeforeEach
	public void prepareDatabase() {
		FlywayMain.executeFlayway("clean", "jdbc:postgresql://localhost/db_loyalty", "loyalty", "moresecure",
				"products", true);
		FlywayMain.executeFlayway("migrate", "jdbc:postgresql://localhost/db_loyalty", "loyalty", "moresecure",
				"products");
	}

	@Test
	public void Should_create_a_category_When_name_is_passed() throws Throwable {
		final var testContext = new VertxTestContext();
		final var vertx = Vertx.vertx();
		final var eventbus = vertx.eventBus();

		vertx.deployVerticle(this.verticleFactory(ProductsVerticle.class),
				this.createDefaultDeployemntOptions().setWorker(true), ar -> {
					if (ar.failed()) {
						testContext.failNow(ar.cause());
					}

					final JsonObject newCategory = new JsonObject().put("name", "category-1");
					final Stopwatch stopwatch = Stopwatch.createStarted();
					eventbus.request("/bus/product/category:create", newCategory, mar -> {
						stopwatch.stop();
						System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
						if (mar.failed()) {
							testContext.failNow(mar.cause());
						}

						testContext.verify(() -> {

							org.assertj.core.api.Assertions.assertThat(mar.result()).isNotNull();
							org.assertj.core.api.Assertions.assertThat(mar.result().body()).isNotNull();
							org.assertj.core.api.Assertions.assertThat(mar.result().body())
									.hasSameClassAs(new JsonArray());

							final JsonObject body = (JsonObject) ((JsonArray) mar.result().body()).stream().findFirst()
									.orElseThrow();

							org.assertj.core.api.Assertions.assertThat(body.containsKey("id")).isTrue();
							org.assertj.core.api.Assertions.assertThat(body.containsKey("name")).isTrue();

							org.assertj.core.api.Assertions.assertThat(body.getString("name"))
									.isEqualTo(newCategory.getString("name"));
							org.assertj.core.api.Assertions.assertThat(body.getInteger("id")).isNotNull();

							org.assertj.core.api.Assertions.assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS))
									.isLessThan(100);

						});

						testContext.completeNow();
					});

				});

		org.assertj.core.api.Assertions.assertThat(testContext.awaitCompletion(100, TimeUnit.SECONDS)).isTrue();
		if (testContext.failed()) {
			throw testContext.causeOfFailure();
		}
	}

	private DeploymentOptions createDefaultDeployemntOptions() {
		final var config = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_loyalty?search_path=products")
				.put("dbUser", "loyalty").put("dbPassword", "moresecure");
		return new DeploymentOptions().setConfig(config);
	}

	private Supplier<Verticle> verticleFactory(final Class<? extends AbstractVerticle> verticleClass) {
		return new Supplier<>() {
			@Override
			public Verticle get() {
				final var injector = Guice.createInjector();
				return injector.getInstance(verticleClass);
			}

		};
	}

	public void Should_create_one_category_when_all_parameters_are_correct() throws InterruptedException {
		final var testContext = new VertxTestContext();
		final var injector = Guice.createInjector();
		final var deplomentOptions = this.createDefaultDeployemntOptions().setWorker(true).setInstances(10)
				.setWorkerPoolSize(5);

		final var vertx = Vertx.vertx();
		final var eventBus = vertx.eventBus();

		vertx.deployVerticle(new Supplier<Verticle>() {
			@Override
			public Verticle get() {
				final var verticle = new ProductsVerticle();
				injector.injectMembers(verticle);
				return verticle;
			}
		}, deplomentOptions, r -> {
			if (r.failed()) {
				Assertions.fail("Verticle cannot be deployed", r.cause());
				testContext.failNow(r.cause());
				return;
			}

			eventBus.request("/bus/products:create", new JsonObject().put("name", "category-name"), ac -> {
				if (ac.failed()) {
					Assertions.fail("Cannot complete execution", ac.cause());
					testContext.failNow(ac.cause());
					return;
				}

				System.out.println(" Client ::" + ac.result().body());
				Assertions.assertTrue(this.isJsonArray(ac), "Should be a JsonArray");
				Assertions.assertTrue(this.isNotEmpty(ac), "Should not be empty");
				Assertions.assertFalse(this.isHasPaginationMetadata(ac), "Should has pagination header");
				Assertions.assertFalse(this.isHasProductLinks(ac), "Additional links for view details");

			});
		});
//testContext.completeNow();
		testContext.awaitCompletion(1, TimeUnit.MINUTES);
	}

	public void start_server() throws InterruptedException {
		final var injector = Guice.createInjector();

		final var testContext = new VertxTestContext();

		new JsonObject();

		final var deplomentOptions = this.createDefaultDeployemntOptions().setWorker(true).setInstances(10)
				.setWorkerPoolSize(5);

		final var vertx = Vertx.vertx();

		final var eventBus = vertx.eventBus();

		vertx.deployVerticle(new Supplier<Verticle>() {
			@Override
			public Verticle get() {
				final var verticle = new ProductsVerticle();
				injector.injectMembers(verticle);
				return verticle;
			}
		}, deplomentOptions, r -> {
			if (r.failed()) {
				Assertions.fail("Verticle cannot be deployed", r.cause());
				testContext.failNow(r.cause());
				return;
			}

			for (int i = 0; i < 100; i++) {
				final var b = System.currentTimeMillis();
				eventBus.request("/bus/products:search", new JsonObject().put("q", "s"), ac -> {
					if (ac.failed()) {
						Assertions.fail("Cannot complete execution", ac.cause());
						testContext.failNow(ac.cause());
						return;
					}
					System.out.println(("::::::::" + (System.currentTimeMillis() - b)));
					System.out.println(" Client ::" + ac.result().body());
					Assertions.assertTrue(this.isJsonArray(ac), "Should be a JsonArray");
					Assertions.assertTrue(this.isNotEmpty(ac), "Should not be empty");
					Assertions.assertFalse(this.isHasPaginationMetadata(ac), "Should has pagination header");
					Assertions.assertFalse(this.isHasProductLinks(ac), "Additional links for view details");

				});
			}
		});
//testContext.completeNow();
		testContext.awaitCompletion(1, TimeUnit.MINUTES);

	}

	private boolean isHasProductLinks(final AsyncResult<Message<Object>> ac) {
		if (!(ac.result().body() instanceof JsonArray)) {
			return false;
		}

		final var reponse = (JsonArray) ac.result().body();

		return reponse.stream().anyMatch(e -> ((JsonObject) e).getString("link") != null);
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
