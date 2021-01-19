package yuki.plugin;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import com.google.inject.Guice;
import com.google.inject.Injector;

import yuki.plugin.actions.GenEndpointsAction;
import yuki.plugin.actions.GenFunctionsAction;
import yuki.plugin.developmentserver.DevelopmentService;
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

		project.getExtensions()
				.create("yuki", YukiPluginExtension.class);

		project.task(GenEndpointsAction.TASK_NAME)
				.doLast(injector.getInstance(GenEndpointsAction.class));

		project.task(GenFunctionsAction.TASK_NAME)
				.doLast(injector.getInstance(GenFunctionsAction.class));

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