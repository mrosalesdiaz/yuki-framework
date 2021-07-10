package yuki.plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

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

		final File sourceFolder = new File(project.relativePath(YukiGenPlugin.SRC_MAIN_GEN_YUKI));

		this.addGenYukiSourceFolder(project, sourceFolder);

		project.getExtensions()
				.create("yuki", YukiPluginExtension.class);

		final Map<String, Object> taskParameters = new HashMap<>();

		taskParameters.put("group", "yuki");

		project.task(taskParameters, GenEndpointsAction.TASK_NAME)
				.doLast(injector.getInstance(GenEndpointsAction.class));

		project.task(taskParameters, GenFunctionsAction.TASK_NAME)
				.doLast(injector.getInstance(GenFunctionsAction.class));

	}

	private File addGenYukiSourceFolder(final Project project, final File sourceFolder) {
		final JavaPluginConvention javaPlugin = project.getConvention()
				.getPlugin(JavaPluginConvention.class);
		final SourceSetContainer sourceSets = javaPlugin.getSourceSets();
		final SourceSet mainSourceSet2 = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final SourceDirectorySet java = mainSourceSet2.getJava();
		final Set<File> folder = java.getSrcDirs();

		folder.add(sourceFolder);
		java.setSrcDirs(folder);
		return sourceFolder;
	}

}