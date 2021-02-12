package yuki.plugin.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Task;

import yuki.plugin.TaskUtils;
import yuki.plugin.YukiPluginExtension;
import yuki.plugin.developmentserver.DevelopmentService;
import yuki.plugin.gen.querydefinitions.QueryDefinitionsCreator;

public class GenFunctionsAction implements Action<Task> {

	private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";

	public static final String TASK_NAME = "genFunctions";

	@Inject
	public DevelopmentService developmentService;

	@Inject
	public QueryDefinitionsCreator queryDefinitionsCreator;

	@Override
	public void execute(final Task task) {

		final var yukiPluginParameters = task.getProject()
				.getExtensions()
				.getByType(YukiPluginExtension.class);

		final var sourceFolder = new File(task.getProject()
				.relativePath(GenFunctionsAction.SRC_MAIN_GEN_YUKI));

		final var yukiGenFolder = Paths.get(task.getProject()
				.getProjectDir()
				.toString(), sourceFolder.toString())
				.toFile();

		TaskUtils.checkDevelopmentServer(task.getName(), yukiPluginParameters.getServiceUrl());

		this.developmentService.getDbFunctionDefinitions(yukiPluginParameters)
				.forEach(f -> {
					try {
						System.out.println(f.getFunctionName());
						this.queryDefinitionsCreator.updateJavaClassDefinition(f, yukiPluginParameters, yukiGenFolder);
					} catch (final IOException e) {
						throw new GradleException(
								String.format("Error during creation of file %s ", f.getFunctionName()), e);
					}
				});
	}

}
