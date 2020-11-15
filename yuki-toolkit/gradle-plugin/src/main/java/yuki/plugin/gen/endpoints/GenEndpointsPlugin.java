package yuki.plugin.gen.endpoints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import com.google.inject.Guice;
import com.google.inject.Injector;

import yuki.plugin.developmentserver.DevelopmentService;
import yuki.plugin.enpoints.parser.ResourcesTree;
import yuki.plugin.gen.querydefinitions.QueryDefinitionsCreator;
import yuki.plugin.guice.GuiceModule;

public class GenEndpointsPlugin implements Plugin<Project> {
	private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";

	@Inject
	public DevelopmentService developmentService;

	@Inject
	public QueryDefinitionsCreator queryDefinitionsCreator;

	@Override
	public void apply(final Project project) {

		final Injector injector = Guice.createInjector(new GuiceModule());
		injector.injectMembers(this);

		final var sourceFolder = new File(project.relativePath(GenEndpointsPlugin.SRC_MAIN_GEN_YUKI));

		this.addGenYukiSourceFolder(project, sourceFolder);

		final var yukiPluginParameters = project.getExtensions()
				.create("yuki", YukiPluginExtension.class);

		final var paremeters = project.getExtensions()
				.create("yuki", GenEndpointsPluginExtension.class);

		project.task("genFunctions")
				.doLast(task -> {
					this.developmentService.getDbFunctionDefinitions(yukiPluginParameters)
							.forEach(f -> {
								try {
									this.queryDefinitionsCreator
											.updateJavaClassDefinition(f, yukiPluginParameters, sourceFolder);
								} catch (final IOException e) {
									throw new GradleException(
											String.format("Error during creation of file %s ", f.getFunctionName()), e);
								}
							});
				});

		project.task("genEndpoints")
				.doLast(task -> {
					final var resourcesTree = new ResourcesTree();
					try {
						resourcesTree.loadStarUml(Paths.get(paremeters.startUmlFile), paremeters.rootPath);

						final var tasks = new CreateClassesTasks();
						Path path;
						if (paremeters.output == null) {
							path = Paths.get(project.getProjectDir()
									.toString(), sourceFolder.toString(), "src/main/java/gen-yuki");
						} else {
							path = Paths.get(paremeters.output);
						}
						tasks.create(resourcesTree.getResourceList(), path);

					} catch (final IOException e) {
						throw new RuntimeException("Error generating resources", e);
					}

				});

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