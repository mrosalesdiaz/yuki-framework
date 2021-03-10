package test.dataaccess.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import io.vertx.junit5.VertxExtension;
import sun.reflect.annotation.AnnotationParser;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_String {

    public static Parameter createAnnotation(ParameterType parameterType, int length) throws IllegalAccessException, InstantiationException {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("value", parameterType);
        parameters.put("length", length);

        return (Parameter) AnnotationParser.annotationForMap(Parameter.class, parameters);
    }

    public static Parameter createAnnotation(ParameterType parameterType) throws IllegalAccessException, InstantiationException {
        return Test_ParameterParse_String.createAnnotation(parameterType, Integer.MIN_VALUE);
    }

    @Test
    void Should_ReturnVarcharLimitedDatabase__When_JavaStringIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.VARCHAR, 4);
        String data = "hello world";
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, data)
        )
                .isInstanceOf(String.class)
                .isEqualTo(data.substring(0, 4));
    }

    @Test
    void Should_ReturnNullVarcharLimitedDatabase__When_JavaStringNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.VARCHAR, 4);
        String data = null;
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, data)
        )
                .isNull();
    }

    @Test
    void Should_ReturnVarcharUnlimitedDatabase__When_JavaStringIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.VARCHAR);
        String data = "hello world";
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, data)
        )
                .isInstanceOf(String.class)
                .isEqualTo(data);
    }

    @Test
    void Should_ReturnNullVarcharUnlimitedDatabase__When_JavaStringNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.VARCHAR);
        String data = null;
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, data)
        )
                .isNull();
    }

    @Test
    void Should_ReturnTextBlankLimitedPaddedDatabase__When_JavaStringIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TEXT);
        String data = "hello world";
        Assertions.assertThat(

                ProcessDbParameter.parse(parameter, data)
        )
                .isInstanceOf(String.class)
                .isEqualTo(data);
    }

    @Test
    void Should_ReturnCharacterBlankLimitedPaddedNullDatabase__When_JavaStringNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TEXT);
        String data = null;
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, data)
        )
                .isNull();
    }
}