package yuki.development.service.endpoints;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class EndpointsController {
	@Inject
	private ResourcesTree resourcesTree;

	public void getEndpoints(final RoutingContext rc, final JsonObject config) {

		try {
			this.resourcesTree
					.loadStarUml(Paths.get(config.getString("endpointsModel")), config.getString("rootEndpoints"));
		} catch (final IOException e) {
			rc.fail(e);
			return;
		}

		final var jsonArray = Stream.of(this.resourcesTree.getResourceList())
				.map(e -> new JsonObject().put("path", e[0])
						.put("className", e[1])
						.put("method", e[2]))
				.collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::add));

		rc.response()
				.setStatusCode(200)
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(jsonArray.toString());
	}

}
