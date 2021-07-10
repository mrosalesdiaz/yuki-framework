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
import yuki.framework.datafunction.generator.fn.ClassAndPackageNamingTask;
import yuki.framework.datafunction.generator.fn.ParameterSettersTask;
import yuki.framework.datafunction.generator.fn.SqlQueryAnnotationTask;

public class JavaFnClassGenerator {

  public static final String DEFAULT_GEN_PACKAGE_NAME = "yuki.gen.db.fn";

  @Inject
  private ClassAndPackageNamingTask classAndPackageNamingTask;
  @Inject
  private SqlQueryAnnotationTask sqlQueryAnnotationTask;
  @Inject
  private ParameterSettersTask parameterSettersTask;

  public JsonArray generate(JsonArray listOfFunctionDefinitions) {
    return listOfFunctionDefinitions.stream()
        .map(JsonObject.class::cast)
        .map(this::newJavaClassDefinitionJsonObject)
        .map(fnDefinition -> {
          CompilationUnit queryDefinitionClass = StaticJavaParser
              .parse(Templates.JAVA_FN_DEFINITION.template());

          this.classAndPackageNamingTask.accept(queryDefinitionClass, fnDefinition);

          this.sqlQueryAnnotationTask.accept(queryDefinitionClass, fnDefinition);

          this.parameterSettersTask.accept(queryDefinitionClass, fnDefinition);

          return new JsonObject()
              .put("filePath", fnDefinition.getString("filePath"))
              .put("classSource", queryDefinitionClass.toString());
        })
        .collect(JsonCollector.toJsonArray());
  }

  private JsonObject newJavaClassDefinitionJsonObject(JsonObject databaseFunctionDefinition) {
    String packageName = buildPackageName(databaseFunctionDefinition);
    String className = buildClassName(databaseFunctionDefinition);
    String filePath = buildFilePath(packageName, className);
    String dbFunctionName = databaseFunctionDefinition.getString("functionName");
    JsonArray dbFunctionParameters = buildArgumentsArray(databaseFunctionDefinition);
    String dbReturnType = databaseFunctionDefinition.getString("functionReturn");

    JsonObject result = new JsonObject()
        .put("filePath", filePath)
        .put("className", className)
        .put("packageName", packageName)
        .put("dbFunctionName", dbFunctionName)
        .put("dbFunctionParameters", dbFunctionParameters)
        .put("dbReturnType", dbReturnType);

    return result;
  }

  private JsonArray buildArgumentsArray(JsonObject databaseFunctionDefinition) {
    String functionArgumentsDefinition = databaseFunctionDefinition.getString("functionArguments");

    return Splitter.on(",")
        .omitEmptyStrings()
        .trimResults()
        .splitToList(functionArgumentsDefinition)
        .stream()
        .map(this::processFunctionArgument)
        .collect(JsonCollector.toJsonArray());
  }

  private JsonObject processFunctionArgument(String argumentLine) {
    String parameterName = argumentLine.substring(0, argumentLine.indexOf(" "));
    String databaseType = argumentLine.substring(argumentLine.indexOf(" ") + 1).trim();
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
    return Splitter.on("__")
        .splitToList(databaseFunctionDefinition.getString("functionName"))
        .stream()
        .skip(1)
        .map(s -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s))
        .reduce("", (prev, curr) -> String.format("Fn%s",curr));
  }
}
