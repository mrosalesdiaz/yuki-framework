package yuki.builders.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import yuki.builders.commons.ApiException;
import yuki.builders.guice.GuiceModule;

public class EndpointsBuilderVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(EndpointsBuilderVerticle.class);

	private HttpServer httpServer;

	private void createServer(final Vertx vertx, final Router router, final Promise<Void> startPromise) {
		this.httpServer = vertx.createHttpServer();
		this.httpServer.requestHandler(router);
		this.httpServer.listen(this.config().getInteger("serverPort", 10000), e -> {
			EndpointsBuilderVerticle.logger
					.info(String.format("Yuki Builder Web Server started in port: %s", e.result().actualPort()));
			startPromise.complete();
		});
	}

	private String getTemplateContent() {
		try {
			final URL resourceUri = Resources.getResource(EndpointsBuilderVerticle.class,
					"/templates/endpoint-template.tpl");
			return Resources.toString(resourceUri, Charsets.UTF_8);
		} catch (final IOException e) {
			throw new ApiException("Erro getting template for endpoints definition class.", e);
		}
	}

	private void handleClassCreation(final RoutingContext rc) {
		final String endpointName = rc.pathParam("className");

		if (Strings.isNullOrEmpty(endpointName)) {
			throw new ApiException("Endpoint name has wrong value");
		}

		final JsonObject jsonObject = rc.getBodyAsJson();

		if (jsonObject == null) {
			throw new ApiException("Body requst does not contains value");
		}

		final CompilationUnit compilationUnit = StaticJavaParser.parse(this.getTemplateContent());

		final TypeDeclaration typeDeclaration = compilationUnit.findFirst(TypeDeclaration.class)
				.orElseThrow(() -> new ApiException("Java template file for endpoint class is missed"));

		typeDeclaration.setName(endpointName);

		final AnnotationExpr annotationExpr = typeDeclaration.getAnnotation(0).asAnnotationExpr();

		annotationExpr.findAll(MemberValuePair.class).stream().forEach(mvp -> {
			mvp.findFirst(MemberValuePair.class).ifPresent(e -> {
				System.out.println("MemberValuePair " + e.getName());
				if (e.getNameAsString().equals("path")) {
					System.out.println("updated path");
					e.setValue(new StringLiteralExpr(jsonObject.getString("path", "/error/empty/path/")));
				} else if (e.getNameAsString().equals("method")) {
					System.out.println("updated methd");
					e.findFirst(FieldAccessExpr.class).get()
							.setName(new SimpleName(jsonObject.getString("method", "GET")));
				}
			});
		});

		this.saveJavaFile(endpointName, compilationUnit.toString());

		rc.response().end();
	}

	private void saveJavaFile(final String className, final String javaFileContent) {
		final String fileName = String.format("%s.java", className);
		final Path javaOutputFolder = Paths.get(this.config().getString("javaOutputFolder",
				"/Volumes/sdcard/yuki/framework/development-worker/src/main/java/yuki/resources"));
		final Path javaFilePath = javaOutputFolder.resolve(fileName);
		try (final InputStream content = new ByteArrayInputStream(
				javaFileContent.toString().getBytes(Charsets.UTF_8))) {
			Files.copy(content, javaFilePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			throw new ApiException("Error saving java file.", e);
		}

	}

	@Override
	public void start(final Promise<Void> startPromise) throws Exception {
		EndpointsBuilderVerticle.logger
				.info(String.format("Starting Builder API server in verticle: %s", this.deploymentID()));

		final Injector injector = Guice.createInjector(new GuiceModule());

		injector.injectMembers(this);

		final Router mainRouter = Router.router(this.vertx);

		final Router apiRouter = Router.router(this.vertx);
		apiRouter.route().handler(BodyHandler.create());

		mainRouter.mountSubRouter(this.config().getString("rootContext", "/builder"), apiRouter);

		final Route route = apiRouter.post("/endpoints/:className").handler(this::handleClassCreation);

		System.out.println(route.getPath());
		this.createServer(this.vertx, mainRouter, startPromise);

	}

	@Override
	public void stop(final Promise<Void> stopPromise) throws Exception {
		EndpointsBuilderVerticle.logger.info(String.format("Shutdown API server in verticle: %s", this.deploymentID()));
		this.httpServer.close(stopPromise);
	}
}
