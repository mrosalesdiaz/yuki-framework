package yuki.framework.verticle;

import com.google.inject.Injector;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class WebHelpers {

    public static void register(Router router, String path, HttpMethod method, Injector rootInjector, Class<? extends Handler<RoutingContext>> handler, Class<? extends Handler<RoutingContext>> errorHandler) {
        Handler<RoutingContext> instanceHandler = rootInjector.getInstance(handler);
        Handler<RoutingContext> instanceErrorHandler = rootInjector.getInstance(errorHandler);

        router
                .route(path)
                .method(method)
                .handler(instanceHandler::handle)
                .failureHandler(instanceErrorHandler::handle);
    }

}
