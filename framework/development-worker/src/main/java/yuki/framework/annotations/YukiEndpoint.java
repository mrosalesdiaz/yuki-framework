package yuki.framework.annotations;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public interface YukiEndpoint extends Handler<RoutingContext> {
	HttpMethod getMethod();

	String getPath();
}
