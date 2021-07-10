package yuki.gradleplugin.tasks;

import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Task;
import yuki.framework.datafunction.dao.FunctionsDao;
import yuki.framework.datafunction.generator.JavaFnClassGenerator;
import yuki.framework.verticle.common.ExceptionUtil;
import yuki.gradleplugin.YukiPluginExtension;

public class GenDataFunctionsAction implements Action<Task> {

  public static final String TASK_NAME = "genFunctions";
  private static final String SRC_MAIN_GEN_YUKI = "src/main/gen-yuki";
  @Inject
  private FunctionsDao functionsDao;
  @Inject
  private JavaFnClassGenerator javaFnClassGenerator;

  @Override
  public void execute(Task task) {
    final YukiPluginExtension yukiPluginParameters = task.getProject()
        .getExtensions()
        .getByType(YukiPluginExtension.class);

    final File sourceFolder = new File(task.getProject()
        .relativePath(SRC_MAIN_GEN_YUKI));

    final File yukiGenFolder = Paths.get(task.getProject()
        .getProjectDir()
        .toString(), sourceFolder.toString())
        .toFile();

    try {
      MoreFiles.deleteDirectoryContents(
          yukiGenFolder.toPath()
              .resolve(JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME
                  .replace(".", "/")),
          RecursiveDeleteOption.ALLOW_INSECURE
      );

      List<Throwable> errors = new ArrayList<>();
      JsonArray generatedJavaClasses = javaFnClassGenerator
          .generate(functionsDao.readFunctions(yukiPluginParameters.getSchema().get()));

      generatedJavaClasses.stream()
          .map(JsonObject.class::cast)
          .forEach(o -> ExceptionUtil.catchException(() -> {
            File javaFilePath = new File(yukiGenFolder, o.getString("filePath"));
            javaFilePath.getParentFile().mkdirs();
            Files.write(o.getString("classSource").getBytes(StandardCharsets.UTF_8), javaFilePath);
          }, errors::add));

      if (!errors.isEmpty()) {
        throw new IOException(
            String.format("Errors found creating java files: %s",
                errors.stream()
                    .map(e -> e.getMessage()).collect(Collectors.joining(", ")))
        );
      }

    } catch (SQLException exception) {
      exception.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
