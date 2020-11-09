package loyalty.backend.endpoints;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.endpoints.annotations.EndpointExtension;
import yuki.resources.GetPatients;

@EndpointExtension(GetPatients.class)
public class GetPatientsEx implements Handler<RoutingContext> {

	@Override
	public void handle(final RoutingContext rc) {
		rc.response().end(new JsonObject().put("Hello", "World").toString());
	}

}
