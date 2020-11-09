package yuki.resources;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.endpoints.annotations.EndpointDefinition;
import yuki.framework.enpoints.YukiEndpoint;

@EndpointDefinition(method = HttpMethod.POST, path = "/patients/:patientId/exams")
public class CreateExamForPatient implements YukiEndpoint {

    @Override
    public void handle(final RoutingContext event) {
    }
}
