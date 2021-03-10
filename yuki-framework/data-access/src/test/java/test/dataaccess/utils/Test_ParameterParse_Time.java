package test.dataaccess.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import io.vertx.junit5.VertxExtension;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

import static test.dataaccess.utils.Test_ParameterParse_String.createAnnotation;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_Time {

    @Test
    void Should_ReturnTimeDatabase__When_JavaInstantIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TIME);

        Instant now = Instant.now();
        LocalTime nowTime = LocalTime.from(now.atOffset(ZoneOffset.UTC));

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isInstanceOf(LocalTime.class)
                .isEqualTo(nowTime);
    }

    @Test
    void Should_ReturnTimeDatabase__When_JavaLocalTimeIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TIME);

        LocalTime nowTime = LocalTime.now();

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, nowTime)
        )
                .isInstanceOf(LocalTime.class)
                .isEqualTo(nowTime);
    }

    @Test
    void Should_ReturnTimeDatabase__When_JavaLocalTimeNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TIME);

        LocalTime nowTime = null;

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, nowTime)
        )
                .isNull();
    }

    @Test
    void Should_ReturnTimeDatabase__When_JavaInstantNullIsPassed() throws InstantiationException, IllegalAccessException {
        Parameter parameter = createAnnotation(ParameterType.TIME);

        Instant now = null;

        Assertions.assertThat(
                ProcessDbParameter.parse(parameter, now)
        )
                .isNull();
    }
}