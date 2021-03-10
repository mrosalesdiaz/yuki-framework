package test.dataaccess.utils;


import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.junit5.VertxExtension;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_InputStream {

    @Test
    void Should_ReturnByteaDatabase__When_JavaInputStreamIsPassed() throws InstantiationException, IllegalAccessException {
        //TODO Pending to implement as it is not required yet
    }

    @Test
    void Should_ReturnByteaDatabase__When_JavaInputStreamNullIsPassed() {
        //TODO Pending to implement as it is not required yet
    }
}