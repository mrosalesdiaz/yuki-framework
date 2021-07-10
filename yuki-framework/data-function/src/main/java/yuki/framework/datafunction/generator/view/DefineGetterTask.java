package yuki.framework.datafunction.generator.view;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.common.base.CaseFormat;
import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import yuki.framework.dataaccess.annotations.ParameterType;

public class DefineGetterTask implements
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

    definitions.getJsonArray("columns").stream()
        .map(JsonObject.class::cast)
        .forEach(o -> {

          String databaseType = o.getString("databaseType");

          ParameterType parameterType= getEnumDataType(databaseType);
          String javaType = parameterType.javaType().getCanonicalName();
          String propertyName = o.getString("name");
          String parameterTypeEnum = o.getString("typeEnum");

          MethodDeclaration methodDeclaration =
              classOrInterfaceDeclaration
                  .addMethod(String
                      .format("get%1$s",
                          CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, propertyName)));

          methodDeclaration
              .setType(new ClassOrInterfaceType(null,javaType))
              .setBody(null);

          this.addAnnotation(methodDeclaration,propertyName);


        });
  }

  void addAnnotation(MethodDeclaration methodDeclaration, String propertyName) {

    String javaAnnotationCode = String
          .format("class A{ @Get(\"%s\") void test(){}}", propertyName);

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
