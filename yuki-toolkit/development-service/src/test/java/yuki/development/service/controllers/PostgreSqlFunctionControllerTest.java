package yuki.development.service.controllers;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import yuki.development.service.dataaccess.Db;
import yuki.development.service.verticle.ApiServiceVerticle;

@ExtendWith(MockitoExtension.class)
public class PostgreSqlFunctionControllerTest {

	@Spy
	private final Db db = Mockito.mock(Db.class);

	@Inject
	private PostgreSqlFunctionController postgreSqlFunctionController;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void Should_obtain_one_function_definition_When_it_called_with_schema_parameter() throws Throwable {
		final var testContext = new VertxTestContext();
		final var vertx = Vertx.vertx();

		final Future<Void> future = Mockito.mock(Future.class);

		Mockito.doReturn(future)
				.when(this.db)
				.configure(ArgumentMatchers.any(), ArgumentMatchers.any());

		Mockito.doReturn(null)
				.when(this.db)
				.configure(ArgumentMatchers.any(), ArgumentMatchers.any());

		vertx.deployVerticle(this.verticleFactory(ApiServiceVerticle.class), this.createDefaultDeployemntOptions()
				.setWorker(true), ar -> {
					if (ar.failed()) {
						testContext.failNow(ar.cause());
					}

					testContext.completeNow();
				});

		org.assertj.core.api.Assertions.assertThat(testContext.awaitCompletion(100, TimeUnit.SECONDS))
				.isTrue();
		if (testContext.failed()) {
			throw testContext.causeOfFailure();
		}

	}

	private Supplier<Verticle> verticleFactory(final Class<? extends AbstractVerticle> verticleClass) {
		return () -> {
			final var instance = Mockito.spy(verticleClass);

			return instance;
		};
	}

	private DeploymentOptions createDefaultDeployemntOptions() {
		final var config = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_loyalty?search_path=products")
				.put("dbUser", "loyalty")
				.put("rootContext", "api")
				.put("serverPort", 10009)
				.put("dbPassword", "moresecure");
		return new DeploymentOptions().setConfig(config);
	}
}
