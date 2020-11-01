package yuki.framework.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import yuki.framework.annotations.YukiEndpoint;
import yuki.framework.endpoints.Extension;
import yuki.resources.TestResource;
import yuki.resources.TestResource2;

public class EndpointHandlers {

	private static final Logger logger = LoggerFactory.getLogger(RestServerVerticle.class);

	@Inject
	private Injector injector;

	private Handler<RoutingContext> emptyResponse(final YukiEndpoint routerInstance) {
		System.out.println("Create empty handler");
		return rc -> {
			rc.response().end(new JsonObject().put("method", routerInstance.getMethod().name())
					.put("path", routerInstance.getPath()).toString());
		};
	}

	private Set<Class<? extends YukiEndpoint>> getEndpointsDefinitions() {
		return Sets.newHashSet(TestResource.class, TestResource2.class);
	}

	private Set<Class<? extends Handler<RoutingContext>>> getExtensionsFromProject() {
		final var reflections = new Reflections("demo.rest", new TypeAnnotationsScanner(), new SubTypesScanner());
		final var returnData = new HashSet<Class<? extends Handler<RoutingContext>>>();

		// TODO: Implement validation of extension type
		reflections.getTypesAnnotatedWith(Extension.class).stream()
				.forEach(e -> returnData.add((Class<? extends Handler<RoutingContext>>) e));

		return returnData;
	}

	private Map<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> getRoutesAndExtensions() {
		final Map<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> returnValue = new HashMap<>();

		final var endpointsDefinitions = this.getEndpointsDefinitions();

		if (endpointsDefinitions.isEmpty()) {
			return new HashMap<>();
		}

		endpointsDefinitions.forEach(e -> returnValue.put(e, null));

		this.getExtensionsFromProject();

		this.getExtensionsFromProject().stream().forEach(ec -> {
			final Class<? extends Handler<RoutingContext>> extensionHandler = ec;
			returnValue.put(ec.getAnnotation(Extension.class).value(), extensionHandler);
		});

		return returnValue;
	}

	public void registerHandlers(final Router apiRouter) {

		final var routers = this.getRoutesAndExtensions();

		for (final Entry<Class<? extends YukiEndpoint>, Class<? extends Handler<RoutingContext>>> mapEndpointAndExtension : routers
				.entrySet()) {

			final var routerInstance = this.injector.getInstance(mapEndpointAndExtension.getKey());
			Handler<RoutingContext> handlerRequestClass;

			if (mapEndpointAndExtension.getValue() == null) {
				handlerRequestClass = this.emptyResponse(routerInstance);
			} else {
				handlerRequestClass = this.injector.getInstance(mapEndpointAndExtension.getValue());
			}

			this.injector.injectMembers(routerInstance);

			final var route = apiRouter.route(routerInstance.getMethod(), routerInstance.getPath()).handler(e -> {
				System.out.println("EndpointHandlers.registerHandlers()");
				handlerRequestClass.handle(e);
			});

			EndpointHandlers.logger.info(String.format("'%1$s' %2$s => %3$s", route.getPath(),
					mapEndpointAndExtension.getKey(), mapEndpointAndExtension.getValue()));
		}

	}

}
