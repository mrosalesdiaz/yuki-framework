package test.yuki.gradleplugin.querydefinition;


import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import yuki.gradleplugin.querydefinition.SetterProcessor;
import yuki.plugin.gen.querydefinitions.JavaClassHelper;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_QueryDefinitionParameters {


    private Injector rootInjector;

    @BeforeEach
    void initialize() {
        this.rootInjector = Guice.createInjector(new AbstractModule() {
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
            "boolean:@Parameter(ParameterType.BOOLEAN)",
            "numeric:@Parameter(ParameterType.NUMERIC)",
            "bytea:@Parameter(ParameterType.BYTEA)",
            "date:@Parameter(ParameterType.DATE)",
            "integer:@Parameter(ParameterType.INTEGER)",
            "varchar:@Parameter(ParameterType.VARCHAR)",
            "char:@Parameter(ParameterType.VARCHAR)",
            "character:@Parameter(ParameterType.VARCHAR)",
            "text:@Parameter(ParameterType.TEXT)",
        // TODO: skipped check why it returns ParameterType.NONE
           // "timestamp without time zone:@Parameter(ParameterType.TIMESTAMP_WITHOUT_TIME_ZONE)",
            "time:@Parameter(ParameterType.TIME)"
    }, delimiter = ':')
    void Should_ReturnAMethodWithAnnotation__When_MethodIsPassed(String input, String expected) throws IOException {
        SetterProcessor instance = rootInjector.getInstance(SetterProcessor.class);

        Assertions.assertThat(instance).isNotNull();

        CompilationUnit queryDefinitionClass = StaticJavaParser.parse(JavaClassHelper.getJavaTemplateForQueryDefinition());

        List<MethodDeclaration> methodDeclaration = queryDefinitionClass.findAll(MethodDeclaration.class);

        Assertions
                .assertThat(methodDeclaration)
                .withFailMessage("Error processing the Java Template: must have only one method definition")
                .hasSize(1)
                .withFailMessage("Error processing the Java Template: one annotation of type Parameter must be present")
                .is(new Condition<>(m -> {
                    try {
                        return m.get(0).getAnnotation(0).getNameAsString().equals("Parameter");
                    } catch (Exception ex) {
                        return false;
                    }
                }, "Validate has annotation defined" +
                        ""));

        Iterator<String> parts = Splitter.on(",").split(input).iterator();
        String dbType = parts.next();
        Optional<Integer> dbLength = Optional.ofNullable(ifException(() -> new Integer(parts.next())).orElse(null));

        methodDeclaration.stream().forEach(m -> instance.addAnnotation(m, input,dbLength));

        Assertions
                .assertThat(methodDeclaration.get(0).getAnnotation(0).toString())
                .isNotNull()
                .isEqualTo(expected);

    }

    private <T> Optional<T> ifException(Supplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }
}
