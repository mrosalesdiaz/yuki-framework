package yuki.framework.datafunction.generator.fn;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.common.base.CaseFormat;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ParameterSettersTask implements
    BiConsumer<CompilationUnit, JsonObject> {


  @Override
  public void accept(CompilationUnit compilationUnit, JsonObject definitions) {

    final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit
        .findFirst(ClassOrInterfaceDeclaration.class)
        .get();

    final MethodDeclaration methodTemplate = classOrInterfaceDeclaration
        .findFirst(MethodDeclaration.class)
        .get()
        .clone();

    classOrInterfaceDeclaration.findFirst(MethodDeclaration.class)
        .get()
        .remove();

    definitions.getJsonArray("dbFunctionParameters").stream()
        .map(JsonObject.class::cast)
        .forEach(o -> {

          String databaseType = o.getString("databaseType");
          String javaType = o.getString("javaType");
          String parameterName = o.getString("name");
          String parameterTypeEnum = o.getString("parameterTypeEnum");

          MethodDeclaration methodDeclaration =
              classOrInterfaceDeclaration
                  .addMethod(String
                      .format("set%1$s",
                          CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, parameterName)));

          methodDeclaration
              .addParameter(new Parameter(new ClassOrInterfaceType(null, javaType), parameterName))
              .setBody(null);

          this.addAnnotation(methodDeclaration, parameterTypeEnum, Optional.empty());
        });
  }

  void addAnnotation(MethodDeclaration methodDeclaration, String databaseTypeAsString,
      Optional<Integer> length) {

    String javaAnnotationCode;

    if (length.isPresent()) {
      javaAnnotationCode = String
          .format("class A{ @Parameter(value=ParameterType.%s, length=%s) void test(){} }",
              databaseTypeAsString, length.get());
    } else {
      javaAnnotationCode = String
          .format("class A{ @Parameter(ParameterType.%s) void test(){}}", databaseTypeAsString);
    }

    methodDeclaration.findAll(AnnotationExpr.class)
        .forEach(a -> {
          a.remove();
        });

    StaticJavaParser.parse(javaAnnotationCode)
        .findAll(AnnotationExpr.class)
        .stream()
        .forEach(e -> {
          methodDeclaration.addAnnotation(e);
        });
  }
}
