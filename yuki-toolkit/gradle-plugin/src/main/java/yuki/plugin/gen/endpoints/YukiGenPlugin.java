package yuki.plugin.gen.endpoints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

import com.google.inject.Guice;
import com.google.inject.Injector;

import feign.Feign;
import feign.codec.StringDecoder;
import yuki.plugin.developmentserver.DevelopmentService;
import yuki.plugin.gen.queryclasses.YukiDevelopment;
import yuki.plugin.gen.querydefinitions.EndpointDefinitionsCreator;
import yuki.plugin.gen.querydefinitions.QueryDefinitionsCreator;
import yuki.plugin.guice.GuiceModule;

public class YukiGenPlugin implements Plugin<Project> {
	private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";

	@Inject
	public DevelopmentService developmentService;

	@Inject
	public QueryDefinitionsCreator queryDefinitionsCreator;

	@Inject
	public EndpointDefinitionsCreator endpointDefinitionsCreator;

	@Override
	public void apply(final Project project) {

		final Injector injector = Guice.createInjector(new GuiceModule());
		injector.injectMembers(this);

		final var sourceFolder = new File(project.relativePath(YukiGenPlugin.SRC_MAIN_GEN_YUKI));

		this.addGenYukiSourceFolder(project, sourceFolder);

		final var yukiPluginParameters = project.getExtensions()
				.create("yuki", YukiPluginExtension.class);

		project.task("genFunctions")
				.doLast(task -> {

					this.checkDevelopmentServer(task.getName(), yukiPluginParameters.getServiceUrl());

					this.developmentService.getDbFunctionDefinitions(yukiPluginParameters)
							.forEach(f -> {
								try {
									this.queryDefinitionsCreator
											.updateJavaClassDefinition(f, yukiPluginParameters, Paths
													.get(project.getProjectDir()
															.toString(), sourceFolder.toString())
													.toFile());
								} catch (final IOException e) {
									throw new GradleException(
											String.format("Error during creation of file %s ", f.getFunctionName()), e);
								}
							});
				});

		project.task("genEndpoints")
				.doLast(task -> {

					this.checkDevelopmentServer(task.getName(), yukiPluginParameters.getServiceUrl());

					this.developmentService.getEndpointsDefinitions(yukiPluginParameters)
							.forEach(endpoint -> {
								try {
									this.endpointDefinitionsCreator
											.createEndpoint(endpoint, Paths.get(project.getProjectDir()
													.toString(), sourceFolder.toString(), "yuki/resources"));
								} catch (final IOException e) {
									throw new GradleException(String
											.format("Error during creation of class %s ", endpoint.getClassName()), e);
								}
							});

				});

	}

	private void checkDevelopmentServer(final String name, final Property<String> property) {
		if (!property.isPresent()) {
			throw new GradleException(String
					.format("Error executing task: %s. Configuration for URL is not present. check yuki.serviceUrl property.", name));
		}

		this.checkHttpConnection(name, property.get());

	}

	private void checkHttpConnection(final String taskName, final String stringUrl) {
		try {
			Feign.builder()
					.decoder(new StringDecoder())
					.target(YukiDevelopment.class, stringUrl)
					.checkStatus();
		} catch (final Exception e) {
			throw new GradleException(
					String.format("Development server(%2$s) is down for task: %1$s ", taskName, stringUrl), e);
		}
	}

	private File addGenYukiSourceFolder(final Project project, final File sourceFolder) {
		final var javaPlugin = project.getConvention()
				.getPlugin(JavaPluginConvention.class);
		final var sourceSets = javaPlugin.getSourceSets();
		final var mainSourceSet2 = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final var java = mainSourceSet2.getJava();
		final var folder = java.getSrcDirs();

		folder.add(sourceFolder);
		java.setSrcDirs(folder);
		return sourceFolder;
	}

}