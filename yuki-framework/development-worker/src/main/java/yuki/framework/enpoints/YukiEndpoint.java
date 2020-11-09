package yuki.framework.enpoints;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface YukiEndpoint extends Handler<RoutingContext> {
}
