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
public class Test_ParameterParse_Integer {

    @Test
    void Should_ReturnIntegerDatabase__When_JavaIntegerIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.INTEGER);
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, 12)
        ).isEqualTo(12);
    }

    @Test
    void Should_ReturnNullIntegerDatabase__When_JavaIntegerNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.INTEGER);
        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, (Integer) null)
        ).isNull();
    }

}