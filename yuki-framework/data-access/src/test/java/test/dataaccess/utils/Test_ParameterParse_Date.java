package test.dataaccess.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import io.vertx.junit5.VertxExtension;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

import static test.dataaccess.utils.Test_ParameterParse_String.createAnnotation;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_Date {

    @Test
    void Should_ReturnDateDatabase__When_JavaInstantIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATE);
        Instant now = Instant.now();
        LocalDate today = LocalDate.from(now.atOffset(ZoneOffset.UTC));

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isInstanceOf(LocalDate.class)
                .isEqualTo(today);
    }

    @Test
    void Should_ReturnDateDatabase__When_JavaLocalDateIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATE);
        LocalDate today = LocalDate.now();

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, today)
        )
                .isInstanceOf(LocalDate.class)
                .isEqualTo(today);
    }

    @Test
    void Should_ReturnDateDatabase__When_JavaInstantNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATE);
        Instant now = null;

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isNull();
    }

    @Test
    void Should_ReturnDateDatabase__When_JavaLocalDateNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATE);
        LocalDate today = null;

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, today)
        )
                .isNull();
    }

}