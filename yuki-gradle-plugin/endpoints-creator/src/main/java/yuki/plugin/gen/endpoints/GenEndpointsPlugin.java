package yuki.plugin.gen.endpoints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import yuki.plugin.enpoints.parser.ResourcesTree;

public class GenEndpointsPlugin implements Plugin<Project> {
	@Override
	public void apply(final Project project) {

		final var javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
		final var sourceSets = javaPlugin.getSourceSets();
		final var mainSourceSet2 = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final var java = mainSourceSet2.getJava();
		final var folder = java.getSrcDirs();

		final var sourceFolder = new File(project.relativePath("src/main/gen-yuki"));
		folder.add(sourceFolder);
		java.setSrcDirs(folder);

		final var paremeters = project.getExtensions().create("startUml", GenEndpointsPluginExtension.class);

		project.task("genEndpoints").doLast(task -> {
			final var resourcesTree = new ResourcesTree();
			try {
				resourcesTree.loadStarUml(Paths.get(paremeters.startUmlFile), paremeters.rootPath);

				final var tasks = new CreateClassesTasks();
				Path path;
				if (paremeters.output == null) {
					path = Paths.get(project.getProjectDir().toString(), sourceFolder.toString(),
							"src/main/java/gen-yuki");
				} else {
					path = Paths.get(paremeters.output);
				}
				tasks.create(resourcesTree.getResourceList(), path);

			} catch (final IOException e) {
				throw new RuntimeException("Error generating resources", e);
			}

		});
	}

}