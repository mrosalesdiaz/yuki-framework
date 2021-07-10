package yuki.gradleplugin.tasks;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.flywaydb.core.Flyway;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import yuki.framework.datafunction.dao.Ds;
import yuki.gradleplugin.YukiPluginExtension;

public class GenFlywayCleanAction implements Action<Task> {

  public static final String TASK_NAME = "dbClean";

  @Inject
  private Ds ds;

  @Override
  public void execute(Task task) {
    final YukiPluginExtension yukiPluginParameters = task.getProject()
        .getExtensions()
        .getByType(YukiPluginExtension.class);

    Flyway flyway = Flyway.configure(getProjectClassPath(task.getProject()))
        .dataSource(ds.getDataSource())
        .outOfOrder(true)
        .cleanOnValidationError(false)
        .load();

    flyway.clean();
  }

  private ClassLoader getProjectClassPath(Project project) {
    Set<URL> extraURLs = new HashSet<>();
    JavaPluginConvention plugin = project.getConvention()
        .getPlugin(JavaPluginConvention.class);

    for (SourceSet sourceSet : plugin.getSourceSets()) {
      try {
        FileCollection classesDirs = sourceSet.getOutput().getClassesDirs();
        for (File directory : classesDirs.getFiles()) {
          URL classesUrl = directory.toURI().toURL();
          extraURLs.add(classesUrl);
        }
        URL resourcesUrl = sourceSet.getOutput().getResourcesDir().toURI().toURL();
        extraURLs.add(resourcesUrl);
      } catch (NoSuchMethodError | MalformedURLException ex) {
        ex.printStackTrace();
      }

    }

    return new URLClassLoader(
        extraURLs.toArray(new URL[0]),
        project.getBuildscript().getClassLoader());
  }

}
