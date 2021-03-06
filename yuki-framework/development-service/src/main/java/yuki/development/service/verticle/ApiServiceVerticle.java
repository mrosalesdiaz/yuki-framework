package yuki.development.service.verticle;

import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import yuki.development.service.controllers.PostgreSqlFunctionController;
import yuki.development.service.dataaccess.Db;
import yuki.development.service.endpoints.EndpointsController;
import yuki.development.service.guice.GuiceModule;

public class ApiServiceVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiServiceVerticle.class);

	private HttpServer httpServer;

	@Inject
	private Db db;

	private Router createApiRouter(final Vertx vertx) {
		return Router.router(vertx)
				.mountSubRouter(this.config()
						.getString("rootContext", "/api"), Router.router(vertx));
	}

	@Override
	public JsonObject config() {
		final JsonObject current = super.config();

		return new JsonObject().put("jdbcUrl", "postgresql://localhost/db_loyalty?search_path=products")
				.put("dbUser", "loyalty")
				.put("dbPassword", "moresecure")
				.put("endpointsModel", "/Volumes/sdcard/yuki/yuki-toolkit/development-service/files/endpoints-model.mdj")
				.mergeIn(current);
	}

	private void createServer(final Vertx vertx, final Router router, final Promise<Void> startPromise) {
		final String realClassName = this.getClass()
				.getName();
		final int port = this.config()
				.getInteger("serverPort", 10009);
		this.httpServer = vertx.createHttpServer();
		this.httpServer.requestHandler(router);
		this.httpServer.listen(port, e -> {
			if (e.failed()) {
				ApiServiceVerticle.LOGGER.info(String
						.format("The server cannot be started in port: %s with Verticle %s", port, realClassName), e
								.cause());
				return;
			}

			ApiServiceVerticle.LOGGER
					.info(String.format("Yuki Web Server started in port: %s with Verticle %s", e.result()
							.actualPort(), realClassName));
			startPromise.complete();
		});
	}

	@Override
	public void start(final Promise<Void> startPromise) throws Exception {
		ApiServiceVerticle.LOGGER
				.info(String.format("Starting Development API server in verticle: %s", this.deploymentID()));

		final Injector injector = Guice.createInjector(new GuiceModule());

		injector.injectMembers(this);

		this.db.configure(this.config(), this.vertx)
				.onComplete(h -> {
					if (h.failed()) {
						startPromise.fail(h.cause());
					}

					final Router mainRouter = Router.router(this.vertx);

					final Router apiRouter = this.createApiRouter(this.vertx);

					this.createInfoRoute(injector, mainRouter, this.config());

					mainRouter.mountSubRouter("/api", apiRouter);

					this.createFunctionsRoute(injector, apiRouter);
					this.createEndpointsRoute(injector, apiRouter, this.config());

					this.createServer(this.vertx, mainRouter, startPromise);

				});

	}

	private void createInfoRoute(final Injector injector, final Router apiRouter, final JsonObject config) {
		injector.getInstance(PostgreSqlFunctionController.class);
		apiRouter.head()
				.handler(rc -> rc.response()
						.putHeader("yuki-version", "x.x.x")
						.end());

	}

	private void createFunctionsRoute(final Injector injector, final Router apiRouter) {
		final PostgreSqlFunctionController controller = injector.getInstance(PostgreSqlFunctionController.class);
		apiRouter.get("/:schemaName/functions")
				.handler(controller::getPostgreSqlFunctions);
	}

	private void createEndpointsRoute(final Injector injector, final Router apiRouter, final JsonObject config) {
		final EndpointsController controller = injector.getInstance(EndpointsController.class);
		apiRouter.get("/endpoints")
				.handler(rc -> controller.getEndpoints(rc, config));
	}

	@Override
	public void stop(final Promise<Void> stopPromise) throws Exception {
		ApiServiceVerticle.LOGGER.info(String.format("Shutdown API server in verticle: %s", this.deploymentID()));
		this.httpServer.close(stopPromise);
	}

}
