package yuki.plugin.gen.querydefinitions;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import yuki.gradleplugin.querydefinition.SetterProcessor;
import yuki.plugin.YukiPluginExtension;
import yuki.plugin.gen.dtos.DbFunctionDefinition;

public class NewQueryDefinitionGenerator {

    @Inject
    private SetterProcessor setterProcessor;

    public InputStream execute(final String yukiGenQueriesRelativeFolder, final DbFunctionDefinition f,
                               final YukiPluginExtension parameters) throws IOException {

        final String yukiGenQueriesPackage = String.join(".", yukiGenQueriesRelativeFolder.split("\\/"));
        final CompilationUnit queryDefinitionClass = StaticJavaParser.parse(JavaClassHelper.getJavaTemplateForQueryDefinition());

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        this.applyClassNameAndPackage(queryDefinitionClass, f, yukiGenQueriesPackage);

        this.applyQueryInAnnotation(queryDefinitionClass, f);

        this.createSettersForParameters(queryDefinitionClass, f);

        this.saveJavaFileIntoStream(queryDefinitionClass, outputStream);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private void applyClassNameAndPackage(final CompilationUnit queryDefinitionClass, final DbFunctionDefinition f,
                                          final String packageName) {
        queryDefinitionClass.findFirst(ClassOrInterfaceDeclaration.class)
                .get()
                .setName(f.getName());
        queryDefinitionClass.findFirst(PackageDeclaration.class)
                .get()
                .setName(packageName);

    }

    private void saveJavaFileIntoStream(final CompilationUnit queryDefinitionClass,
                                        final ByteArrayOutputStream outputStream) throws IOException {
        ByteStreams.copy(new ByteArrayInputStream(queryDefinitionClass.toString()
                .getBytes(Charsets.UTF_8)), outputStream);

    }

    private void createSettersForParameters(final CompilationUnit queryDefinitionClass, final DbFunctionDefinition f) {

        final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = queryDefinitionClass
                .findFirst(ClassOrInterfaceDeclaration.class)
                .get();

        final MethodDeclaration methodTemplate = classOrInterfaceDeclaration.findFirst(MethodDeclaration.class)
                .get()
                .clone();

        classOrInterfaceDeclaration.findFirst(MethodDeclaration.class)
                .get()
                .remove();

        System.out.println(methodTemplate);

        f.getParameters()
                .entrySet()
                .stream()
                .forEach(p -> {
                    MethodDeclaration methodDeclaration =
                            classOrInterfaceDeclaration
                                    .addMethod(String
                                            .format("set%1$s", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, p.getKey())));

                    methodDeclaration.addParameter(new Parameter(this.mapToJavaType(p.getValue()), p.getKey()))
                            .setBody(null);

                    this.setterProcessor.addAnnotation(methodDeclaration,p.getValue(), Optional.empty());
                });

    }

    private Type mapToJavaType(final String databaseType) {
        if (databaseType.equalsIgnoreCase("TEXT")) {
            return new ClassOrInterfaceType(null, "String");
        }
        return new ClassOrInterfaceType(null, "Object");
    }

    private void applyQueryInAnnotation(final CompilationUnit queryDefinitionClass, final DbFunctionDefinition f) {
        queryDefinitionClass.findFirst(ClassOrInterfaceDeclaration.class)
                .get()
                .findAll(NormalAnnotationExpr.class)
                .get(0)
                .findAll(MemberValuePair.class)
                .stream()
                .filter(e -> e.getNameAsString()
                        .equals("sql"))
                .forEach(e -> {
                    e.getValue()
                            .asStringLiteralExpr()
                            .setValue(String.format(" SELECT * FROM %1$s(%2$s) ", f.getFunctionName(), this
                                    .buildFunctionParametersString(f)));
                });

    }

    private String buildFunctionParametersString(final DbFunctionDefinition dbFunctionDefinition) {
        final AtomicInteger index = new AtomicInteger(1);

        String parametersString = dbFunctionDefinition.getParameters()
                .entrySet()
                .stream()
                .map(e -> String.format("%1$s := %2$s", e.getKey(), String.format("$%s", index.getAndIncrement())))
                .collect(Collectors.joining(" ,"));

        if (!Objects.equals("", parametersString)) {
            parametersString = String.format(" %s ", parametersString);
        }

        return parametersString;
    }

}
