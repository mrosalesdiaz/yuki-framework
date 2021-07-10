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
import yuki.plugin.gen.querydefinitions.EndpointDefinitionsCreator;

public class GenEndpointsAction implements Action<Task> {

	private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";

	public static final String TASK_NAME = "genEndpoints";

	@Inject
	public DevelopmentService developmentService;

	@Inject
	public EndpointDefinitionsCreator endpointDefinitionsCreator;

	@Override
	public void execute(final Task task) {

		final YukiPluginExtension yukiPluginParameters = task.getProject()
				.getExtensions()
				.getByType(YukiPluginExtension.class);

		final File sourceFolder = new File(task.getProject()
				.relativePath(GenEndpointsAction.SRC_MAIN_GEN_YUKI));

		final File yukiGenFolder = Paths.get(task.getProject()
				.getProjectDir()
				.toString(), sourceFolder.toString())
				.toFile();

		TaskUtils.checkDevelopmentServer(task.getName(), yukiPluginParameters.getServiceUrl());

		this.developmentService.getEndpointsDefinitions(yukiPluginParameters)
				.forEach(endpoint -> {
					try {
						this.endpointDefinitionsCreator.createEndpoint(endpoint, yukiGenFolder.toPath());
					} catch (final IOException e) {
						throw new GradleException(
								String.format("Error during creation of class %s ", endpoint.getClassName()), e);
					}
				});
	}

}
