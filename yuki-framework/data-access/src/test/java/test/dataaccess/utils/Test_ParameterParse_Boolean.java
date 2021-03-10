package test.dataaccess.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.junit5.VertxExtension;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

import static test.dataaccess.utils.Test_ParameterParse_String.createAnnotation;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_Boolean {

    @Test
    void Should_ReturnBooleanDatabase__When_JavaBooleanIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.BOOLEAN);

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, Boolean.TRUE)
        ).isTrue();
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, Boolean.FALSE)
        ).isFalse();
    }

    @Test
    void Should_ReturnNullBooleanDatabase__When_JavaBooleanNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.BOOLEAN);
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, (Boolean) null)
        ).isNull();
    }

}