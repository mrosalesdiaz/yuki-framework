package test.verticle.initialize;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import yuki.framework.verticle.WebHelpers;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)

public class Test_RouteDefAnnotation {

  @Test
  void Should_ReturnSimpleText__When_SimpleRouteIsDefined(Vertx vertx,
      VertxTestContext vertxTestContext) throws ClassNotFoundException {
    Handler<AsyncResult<HttpResponse<Buffer>>> verifyHttpResponse = rc -> vertxTestContext
        .verify(() -> {
          if (rc.failed()) {
            vertxTestContext.failNow(rc.cause());
          }
          Assertions.assertThat(rc.result().body())
              .isEqualTo(new JsonArray().toBuffer());

          vertxTestContext.completeNow();
        });

    final Router childRouter = Router.router(vertx);

    childRouter.route().handler(BodyHandler.create());
    childRouter.route().handler(ResponseContentTypeHandler.create());

    Injector rootInjector = Guice.createInjector();

    WebHelpers.register(
        childRouter,
        "/simple/:id",
        HttpMethod.POST,
        rootInjector,
        ReturnEmptyJsonArrayHandler.class,
        ErrorHandler.class
    );

    testHttpRequest(
        childRouter,
        verifyHttpResponse,
        vertx,
        vertxTestContext,
        HttpMethod.POST,
        "/api/simple/123"
    );
  }

  @Test
  @Timeout(value = 1, unit = TimeUnit.HOURS)
  void Should_ReturnRuntimeException__When_SimpleWithErrorRouteIsDefined(Vertx vertx,
      VertxTestContext vertxTestContext) throws ClassNotFoundException {
    Handler<AsyncResult<HttpResponse<Buffer>>> verifyHttpResponse = rc -> vertxTestContext
        .verify(() -> {

          Assertions.assertThat(rc.result().statusCode())
              .isEqualTo(500);

          vertxTestContext.completeNow();
        });

    final Router childRouter = Router.router(vertx);

    childRouter.route().handler(BodyHandler.create());
    childRouter.route().handler(ResponseContentTypeHandler.create());

    Injector rootInjector = Guice.createInjector();

    WebHelpers.register(
        childRouter,
        "/simpleWithError/:id",
        HttpMethod.GET,
        rootInjector,
        ReturnRuntimeExceptionHandler.class,
        ErrorHandler.class
    );

    testHttpRequest(
        childRouter,
        verifyHttpResponse,
        vertx,
        vertxTestContext,
        HttpMethod.GET,
        "/api/simpleWithError/123"
    );
  }

  private void testHttpRequest(Router childRouter,
      Handler<AsyncResult<HttpResponse<Buffer>>> verifyHttpResponse, Vertx vertx,
      VertxTestContext vertxTestContext, HttpMethod httpMethod, String requestPath) {
    HttpServer httpServer = vertx.createHttpServer();

    final Router rootRouter = Router.router(vertx);

    rootRouter.mountSubRouter("/api", childRouter);

    httpServer.requestHandler(rootRouter).listen(12829, deployed -> {
      if (deployed.failed()) {
        vertxTestContext.failNow(deployed.cause());
        return;
      }

      WebClientOptions options = new WebClientOptions();
      options.setKeepAlive(false);
      WebClient client = WebClient.create(vertx, options);

      client
          .request(httpMethod, deployed.result().actualPort(), "localhost", requestPath)
          .send(verifyHttpResponse);
    });
  }
}


class ReturnEmptyJsonArrayHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext event) {
    event.response().end(new JsonArray().toBuffer());
  }
}


class ReturnRuntimeExceptionHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext event) {
    throw new RuntimeException();
  }
}

class ErrorHandler implements Handler<RoutingContext> {

  @Override
  public void handle(RoutingContext event) {
    if (event.failed()) {
      System.out.println(event.failure());
    }
    event.response().setStatusCode(500).end();
  }
}