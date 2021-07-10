package test.dataaccess.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.vertx.junit5.VertxExtension;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

import static test.dataaccess.utils.Test_ParameterParse_String.createAnnotation;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_DateTime {
    @Test
    void Should_ReturnDateTimeDatabase__When_JavaInstantIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATETIME);

        Instant now = Instant.now();
        LocalDateTime today = LocalDateTime.from(now.atOffset(ZoneOffset.UTC));

        org.assertj.core.api.Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isInstanceOf(LocalDateTime.class)
                .isEqualTo(today);
    }

    @Test
    void Should_ReturnDateTimeDatabase__When_JavaInstantNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATE);
        Instant now = null;

        org.assertj.core.api.Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isNull();
    }

    @Test
    void Should_ReturnDateTimeDatabase__When_JavaLocalDateTimeIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATETIME);
        LocalDateTime today = LocalDateTime.now();

        org.assertj.core.api.Assertions.assertThat(
                ProcessDbParameter.parse(parameter, today)
        )
                .isInstanceOf(LocalDateTime.class)
                .isEqualTo(today);
    }

    @Test
    void Should_ReturnDateTimeDatabase__When_JavaLocalDateTimeNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.DATETIME);
        LocalDateTime today = null;

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, today)
        )
                .isNull();
    }
}