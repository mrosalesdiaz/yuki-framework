package yuki.framework.datafunction.generator.view;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.vertx.core.json.JsonObject;
import java.util.function.BiConsumer;

public class ClassAndPackageNamingTask implements
    BiConsumer<CompilationUnit, JsonObject> {


  @Override
  public void accept(CompilationUnit compilationUnit, JsonObject definitions) {
    final String className=definitions.getString("className");
    final String packageName=definitions.getString("packageName");

    compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
        .get()
        .setName(className);
    compilationUnit.findFirst(PackageDeclaration.class)
        .get()
        .setName(packageName);

  }
}
