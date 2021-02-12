package yuki.framework.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import yuki.framework.enpoints.EndpointHandlers;
import yuki.framework.guice.GuiceModule;

public class RestServerVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(RestServerVerticle.class);

	private HttpServer httpServer;

	private Router createApiRouter(final Vertx vertx) {
		return Router.router(vertx).mountSubRouter(this.config().getString("rootContext", "/api"),
				Router.router(vertx));
	}

	private void createServer(final Vertx vertx, final Router router, final Promise<Void> startPromise) {
		final String realClassName = this.getClass().getName();
		final int port = this.config().getInteger("serverPort", 8080);
		this.httpServer = vertx.createHttpServer();
		this.httpServer.requestHandler(router);
		this.httpServer.listen(port, e -> {
			if (e.failed()) {
				RestServerVerticle.logger.info(String
						.format("The server cannot be started in port: %s with Verticle %s", port, realClassName));
				return;
			}

			RestServerVerticle.logger.info(String.format("Yuki Web Server started in port: %s with Verticle %s",
					e.result().actualPort(), realClassName));
			startPromise.complete();
		});
	}

	@Override
	public void start(final Promise<Void> startPromise) throws Exception {
		RestServerVerticle.logger.info(String.format("Starting API server in verticle: %s", this.deploymentID()));

		final Injector injector = Guice.createInjector(new GuiceModule());

		final EndpointHandlers endpointsHandlers = injector.getInstance(EndpointHandlers.class);

		final Router mainRouter = Router.router(this.vertx);

		final Router apiRouter = this.createApiRouter(this.vertx);

		mainRouter.mountSubRouter("/api", apiRouter);

		endpointsHandlers.registerHandlers(apiRouter,
				this.config().getString("extensionsPackage", "loyalty.backend.endpoints"));

		this.createServer(this.vertx, mainRouter, startPromise);

	}

	@Override
	public void stop(final Promise<Void> stopPromise) throws Exception {
		RestServerVerticle.logger.info(String.format("Shutdown API server in verticle: %s", this.deploymentID()));
		this.httpServer.close(stopPromise);
	}

}
