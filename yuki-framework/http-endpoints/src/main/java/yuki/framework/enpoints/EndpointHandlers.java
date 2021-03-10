package yuki.framework.enpoints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.inject.Inject;
import com.google.inject.Injector;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.endpoints.annotations.EndpointDefinition;
import yuki.framework.endpoints.annotations.EndpointExtension;
import yuki.framework.server.RestServerVerticle;

public class EndpointHandlers {

	private static final Logger logger = LoggerFactory.getLogger(RestServerVerticle.class);

	@Inject
	private Injector injector;

	private Handler<RoutingContext> emptyResponse(final EndpointDefinition endpointDefinition) {
		System.out.println("Create empty handler");
		return rc -> {
			rc.response().end(new JsonObject().put("method", endpointDefinition.method())
					.put("path", endpointDefinition.path()).toString());
		};
	}

	private Set<Class<? extends YukiEndpoint>> getEndpointsDefinitions() {
		final Reflections reflections = new Reflections("yuki.resources", new TypeAnnotationsScanner(), new SubTypesScanner());
		final HashSet returnData = new HashSet<Class<? extends YukiEndpoint>>();

		// TODO: Implement validation of extension type
		reflections.getTypesAnnotatedWith(EndpointDefinition.class).stream()
				.forEach(e -> returnData.add((Class<? extends YukiEndpoint>) e));

		return returnData;
	}

	private Set<Class<? extends Handler<RoutingContext>>> getExtensionsFromProject(final String extensionsPackage) {
		final Reflections reflections = new Reflections(extensionsPackage, new TypeAnnotationsScanner(), new SubTypesScanner());
		final HashSet returnData = new HashSet<Class<? extends Handler<RoutingContext>>>();

		// TODO: Implement validation of extension type
		reflections.getTypesAnnotatedWith(EndpointExtension.class).stream()
				.forEach(e -> returnData.add((Class<? extends Handler<RoutingContext>>) e));

		return returnData;
	}

	private Map<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> getRoutesAndExtensions(
			final String extensionsPackage) {
		final Map<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> returnValue = new HashMap<>();

		final Set<Class<? extends YukiEndpoint>> endpointsDefinitions = this.getEndpointsDefinitions();

		if (endpointsDefinitions.isEmpty()) {
			return new HashMap<>();
		}

		endpointsDefinitions.forEach(e -> returnValue.put(e, null));

		this.getExtensionsFromProject(extensionsPackage).stream().forEach(ec -> {
			final Class<? extends Handler<RoutingContext>> extensionHandler = ec;
			returnValue.put(ec.getAnnotation(EndpointExtension.class).value(), extensionHandler);
		});

		return returnValue;
	}

	public void registerHandlers(final Router apiRouter, final String extensionsPackage) {
		this.getRoutesAndExtensions(extensionsPackage).entrySet()
				.forEach(e -> this.registerEndpointAndExtension(e, apiRouter));
	}

	private void registerEndpointAndExtension(
			final Entry<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> mapEndpointAndExtension,
			final Router apiRouter) {

		final EndpointDefinition endpointDefinition = mapEndpointAndExtension.getKey().getAnnotation(EndpointDefinition.class);
		final Object routerInstance = this.injector.getInstance(mapEndpointAndExtension.getKey());

		Handler<RoutingContext> handlerRequestClass;

		if (mapEndpointAndExtension.getValue() == null) {
			handlerRequestClass = this.emptyResponse(endpointDefinition);
		} else {
			handlerRequestClass = this.injector.getInstance(mapEndpointAndExtension.getValue());
		}

		this.injector.injectMembers(routerInstance);

		final Route route = apiRouter.route(endpointDefinition.method(), endpointDefinition.path())
				.handler(handlerRequestClass::handle);

		EndpointHandlers.logger.info(String.format("'%1$s' %2$s => %3$s", route.getPath(),
				mapEndpointAndExtension.getKey(), mapEndpointAndExtension.getValue()));
	}

}
