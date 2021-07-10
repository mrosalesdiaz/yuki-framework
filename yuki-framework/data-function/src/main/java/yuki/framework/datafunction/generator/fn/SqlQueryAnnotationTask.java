package yuki.framework.datafunction.generator.fn;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class SqlQueryAnnotationTask implements
    BiConsumer<CompilationUnit, JsonObject> {


  @Override
  public void accept(CompilationUnit compilationUnit, JsonObject definitions) {
    AtomicInteger parameterIndex = new AtomicInteger(1);
    String databaseFunctionName = definitions.getString("dbFunctionName");
    String databaseFunctionArguments = definitions.getJsonArray("dbFunctionParameters").stream()
        .map(JsonObject.class::cast)
        .map(o -> {
          return String
              .format("/* %1$s : %2$s */ %3$s := $%4$s",
                  o.getString("databaseType"),
                  o.getString("parameterTypeEnum"),
                  o.getString("name"),
                  parameterIndex.getAndAdd(1)
              );
        })
        .reduce("", (prev, curr) -> {
          if (prev.equals("")) {
            return curr;
          }
          return String.format("%s, %s", prev, curr);
        });


    compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
        .get()
        .findAll(NormalAnnotationExpr.class)
        .get(0)
        .findAll(MemberValuePair.class)
        .stream()
        .filter(e -> e.getNameAsString()
            .equals("sql"))
        .forEach(e ->
            e.getValue()
                .asStringLiteralExpr()
                .setValue(String.format(" SELECT * FROM %1$s(%2$s) ", databaseFunctionName,
                    databaseFunctionArguments))
        );
  }
}
