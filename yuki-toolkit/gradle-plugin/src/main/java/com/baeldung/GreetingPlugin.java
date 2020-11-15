package com.baeldung;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import endpoints.HelloDump;

public class GreetingPlugin implements Plugin<Project> {
	@Override
	public void apply(final Project project) {

		final var javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
		final var sourceSets = javaPlugin.getSourceSets();
		final var mainSourceSet2 = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final var java = mainSourceSet2.getJava();
		final var folder = java.getSrcDirs();

		folder.add(new File(project.relativePath("src/main/gen-yuki")));
		java.setSrcDirs(folder);

		// sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).
		// getJava().addToAntBuilder("gen-yuki",);

		// sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getAllJava().plus(project.files("src/main/gen-yuki/**"));
		// .srcDir(task.getOutput().getParentFile());

		if (System.currentTimeMillis() > 0) {
			return;
		}

		final var tasks = (Task) project.getTasksByName("classes", false).toArray()[0];
		final var mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final var classFolder = mainSourceSet.getOutput().getClassesDirs().getFiles();

		mainSourceSet.getJava().getSourceDirectories().plus(project.files("./src/main/java/gen-yuki"));

		javaPlugin.getSourceSets().all(sourceSet -> {
			((IConventionAware) sourceSet.getOutput()).getConventionMapping();

			final var configurations = project.getConfigurations();

			GreetingPlugin.this.defineConfigurationsForSourceSet(sourceSet, configurations);
		});

		tasks.doFirst(task -> {
			try {

				System.out.println(classFolder);
				final var classPath = Paths.get(classFolder.toArray()[0].toString(), "Hello.class");
				Files.copy(new ByteArrayInputStream(HelloDump.dump()), classPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Hello, vuild");
		});
		project.getExtensions().create("greeting", GreetingPluginExtension.class);

		project.task("hello").doLast(task -> {
			// System.out.println("Hello, " + this.extension.getGreeter());
			// System.out.println("I have a message for You: " +
			// this.extension.getMessage());
		});
	}

	private void defineConfigurationsForSourceSet(final SourceSet sourceSet,
			final ConfigurationContainer configurations) {

		final var compileConfigurationYUki = configurations.maybeCreate("yuki-gen");
		compileConfigurationYUki.setVisible(false);
		compileConfigurationYUki.setDescription("Dependencies for " + sourceSet + ".");

		// sourceSet.setCompileClasspath(compileConfigurationYUki);
		sourceSet.setRuntimeClasspath(sourceSet.getOutput().plus(compileConfigurationYUki));

		if (System.currentTimeMillis() > 0) {
			return;
		}
		final var compileConfiguration = configurations.maybeCreate(sourceSet.getCompileConfigurationName());
		compileConfiguration.setVisible(false);
		compileConfiguration.setDescription("Dependencies for " + sourceSet + ".");

		final var runtimeConfiguration = configurations.maybeCreate(sourceSet.getRuntimeConfigurationName());
		runtimeConfiguration.setVisible(false);
		runtimeConfiguration.extendsFrom(compileConfiguration);
		runtimeConfiguration.setDescription("Runtime dependencies for " + sourceSet + ".");

		final var compileOnlyConfiguration = configurations.maybeCreate(sourceSet.getCompileOnlyConfigurationName());
		compileOnlyConfiguration.setVisible(false);
		compileOnlyConfiguration.extendsFrom(compileConfiguration);
		compileOnlyConfiguration.setDescription("Compile dependencies for " + sourceSet + ".");

		final var compileClasspathConfiguration = configurations
				.maybeCreate(sourceSet.getCompileClasspathConfigurationName());
		compileClasspathConfiguration.setVisible(false);
		compileClasspathConfiguration.extendsFrom(compileOnlyConfiguration);
		compileClasspathConfiguration.setDescription("Compile classpath for " + sourceSet + ".");

		sourceSet.setCompileClasspath(compileClasspathConfiguration);
		sourceSet.setRuntimeClasspath(sourceSet.getOutput().plus(runtimeConfiguration));
	}
}