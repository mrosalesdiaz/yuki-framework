package yuki.gradleplugin.querydefinition;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.Optional;
import java.util.stream.Stream;

public class SetterProcessor {
    public void addAnnotation(MethodDeclaration methodDeclaration, String databaseTypeAsString, Optional<Integer> length) {
        final String DB_TYPE = getEnumDataType(databaseTypeAsString);

        String javaAnnotationCode;

        if (length.isPresent()) {
            javaAnnotationCode = String.format("class A{ @Parameter(value=ParameterType.%s, length=%s) void test(){} }", DB_TYPE, length.get());
        } else {
            javaAnnotationCode = String.format("class A{ @Parameter(ParameterType.%s) void test(){}}", DB_TYPE);
        }

        methodDeclaration.findAll(AnnotationExpr.class)
                .forEach(a->{
                    a.remove();
                });

        StaticJavaParser.parse(javaAnnotationCode)
                .findAll(AnnotationExpr.class)
                .stream()
                .forEach(e -> {
                    methodDeclaration.addAnnotation(e);
                });
    }

    private String getEnumDataType(String dbParameterTypeName) {
        String[][] mappingTypes = {
                {"BOOLEAN", "BOOLEAN"},
                {"NUMERIC", "NUMERIC"},
                {"BYTEA", "BYTEA"},
                {"DATE", "DATE"},
                {"INTEGER", "INTEGER"},
                {"VARCHAR", "VARCHAR", "CHARACTER","CHAR"},
                {"TEXT", "TEXT"},
                {"TIMESTAMP", "TIMESTAMP_WITHOUT_TIME_ZONE"},
                {"TIME", "TIME"}
        };
        return Stream.of(mappingTypes)
                .filter(e -> Stream.of(e).skip(1).anyMatch(dbParameterTypeName::equalsIgnoreCase))
                .limit(1)
                .map(e -> e[0]).findFirst().orElse("NONE");

    }
}
