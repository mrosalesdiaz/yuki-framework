package yuki.resources;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.endpoints.annotations.EndpointDefinition;
import yuki.framework.enpoints.YukiEndpoint;

@EndpointDefinition(method = HttpMethod.PUT, path = "/patients/:patientId")
public class UpdatePatient implements YukiEndpoint {

    @Override
    public void handle(final RoutingContext event) {
    }
}
