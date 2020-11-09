package demo.rest;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.endpoints.annotations.EndpointExtension;
import yuki.resources.TemplateEndpoint;

@EndpointExtension(TemplateEndpoint.class)
public class Chanchito implements Handler<RoutingContext> {

	@Override
	public void handle(final RoutingContext routingContext) {
		final var json = new JsonObject().put("action", "executed");
		routingContext.response().end(json.toString());
		// System.out.println(a);
	}

}
