package yuki.gradleplugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import yuki.framework.datafunction.dao.Ds;
import yuki.gradleplugin.guice.GuiceModule;
import yuki.gradleplugin.tasks.GenDataFunctionsAction;
import yuki.gradleplugin.tasks.GenFlywayCleanAction;
import yuki.gradleplugin.tasks.GenFlywayMigrateAction;

public class YukiGenPlugin implements Plugin<Project> {

  private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";

  @Override
  public void apply(final Project project) {
    final File sourceFolder = new File(project.relativePath(YukiGenPlugin.SRC_MAIN_GEN_YUKI));
    this.addGenYukiSourceFolder(project, sourceFolder);

    project.getExtensions()
        .create("yuki", YukiPluginExtension.class);

    project.afterEvaluate(conf -> {

      final YukiPluginExtension yukiPluginParameters = project
          .getExtensions()
          .getByType(YukiPluginExtension.class);

      final Injector injector = Guice.createInjector(new GuiceModule(yukiPluginParameters));
      injector.injectMembers(this);
      final Map<String, Object> taskParameters = new HashMap<>();

      taskParameters.put("group", "yuki");

      project.task(taskParameters, GenDataFunctionsAction.TASK_NAME)
          .doLast(injector.getInstance(GenDataFunctionsAction.class))
          .doLast(t->injector.getInstance(Ds.class).close());

      project.task(taskParameters, GenFlywayMigrateAction.TASK_NAME)
          .dependsOn(project.getTasks().getByName("processResources"))
          .doLast(injector.getInstance(GenFlywayMigrateAction.class))
          .doLast(t->injector.getInstance(Ds.class).close());

      project.task(taskParameters, GenFlywayCleanAction.TASK_NAME)
          .dependsOn(project.getTasks().getByName("processResources"))
          .doLast(injector.getInstance(GenFlywayCleanAction.class))
          .doLast(t->injector.getInstance(Ds.class).close());
    });
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