package yuki.resources;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.annotations.YukiEndpoint;

public class TestResource implements YukiEndpoint {

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.GET;
	}

	@Override
	public String getPath() {
		return "/dbname/chanchito";
	}

	@Override
	public void handle(final RoutingContext event) {
	}

}
