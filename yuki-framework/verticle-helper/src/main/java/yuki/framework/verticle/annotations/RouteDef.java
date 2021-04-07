package yuki.framework.verticle.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@Retention(RetentionPolicy.RUNTIME)
public @interface RouteDef {

    String path();

    HttpMethod method() default HttpMethod.GET;

    Class<? extends Handler<RoutingContext>> handler();

    Class<? extends Handler<RoutingContext>> errorHandler();

}
