package yuki.framework.datafunction.generator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import javax.inject.Inject;
import yuki.common.stream.JsonCollector;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.datafunction.common.Templates;
import yuki.framework.datafunction.generator.view.ClassAndPackageNamingTask;
import yuki.framework.datafunction.generator.view.DefineGetterTask;

public class JavaViewClassGenerator {

  public static final String DEFAULT_GEN_PACKAGE_NAME = "yuki.gen.db.view";

  @Inject
  private ClassAndPackageNamingTask classAndPackageNamingTask;

  @Inject
  private DefineGetterTask defineGetterTask;

  public JsonArray generate(JsonArray listOfFunctionDefinitions) {
    return listOfFunctionDefinitions.stream()
        .map(JsonObject.class::cast)
        .map(this::newJavaClassDefinitionJsonObject)
        .map(fnDefinition -> {
          CompilationUnit queryDefinitionClass = StaticJavaParser
              .parse(Templates.JAVA_VIEW_DEFINITION.template());

          this.classAndPackageNamingTask.accept(queryDefinitionClass, fnDefinition);
          this.defineGetterTask.accept(queryDefinitionClass, fnDefinition);

          return new JsonObject()
              .put("filePath", fnDefinition.getString("filePath"))
              .put("classSource", queryDefinitionClass.toString());
        })
        .collect(JsonCollector.toJsonArray());
  }

  private JsonObject newJavaClassDefinitionJsonObject(JsonObject databaseViewDefinition) {
    String packageName = buildPackageName(databaseViewDefinition);
    String className = buildClassName(databaseViewDefinition);
    String filePath = buildFilePath(packageName, className);
    String dbViewName = databaseViewDefinition.getString("viewName");
    JsonArray dbViewColumns = buildColumnsArray(databaseViewDefinition);

    return new JsonObject()
        .put("filePath", filePath)
        .put("className", className)
        .put("packageName", packageName)
        .put("dbViewName", dbViewName)
        .put("columns", dbViewColumns);
  }

  private String buildFilePath(String packageName, String className) {
    return String.format("%s/%s.java",
        packageName.replace('.', '/'),
        className
    );
  }

  private String buildPackageName(JsonObject databaseFunctionDefinition) {
    return String.format("%s.%s",
        DEFAULT_GEN_PACKAGE_NAME,
        databaseFunctionDefinition.getString("schemaName")).toLowerCase();
  }

  private String buildClassName(JsonObject databaseFunctionDefinition) {
    return CaseFormat.LOWER_UNDERSCORE
        .to(CaseFormat.UPPER_CAMEL, databaseFunctionDefinition.getString("viewName"));
  }

  private JsonArray buildColumnsArray(JsonObject databaseFunctionDefinition) {
    String columnsDefinition = databaseFunctionDefinition.getString("columns");

    return Splitter.on(",")
        .omitEmptyStrings()
        .trimResults()
        .splitToList(columnsDefinition)
        .stream()
        .map(this::processColumns)
        .collect(JsonCollector.toJsonArray());
  }

  private JsonObject processColumns(String columnsLine) {
    String parameterName = columnsLine.substring(0, columnsLine.indexOf(" "));
    String databaseType = columnsLine.substring(columnsLine.indexOf(" ") + 1).trim();
    ParameterType parameterType = getEnumDataType(databaseType);
    return new JsonObject()
        .put("name", parameterName)
        .put("databaseType", databaseType)
        .put("parameterTypeEnum", parameterType.name())
        .put("javaType", parameterType.javaType().getCanonicalName());
  }

  private ParameterType getEnumDataType(String dbParameterTypeName) {
    return Arrays.stream(ParameterType.values())
        .filter(e -> Arrays.stream(e.dbType())
            .map(String::trim)
            .anyMatch(
                dbParameterTypeName.trim()::equalsIgnoreCase
            )
        )
        .reduce(ParameterType.STRING, (prev, curr) -> curr);
  }
}
