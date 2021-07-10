package test.dataaccess.utils;


import io.vertx.junit5.VertxExtension;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.utils.ProcessDbParameter;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class Test_ParameterParse_String {


  public static Parameter createAnnotation(ParameterType parameterType, int length) {
    try {
      Parameter   parameter = Parameter.class.getConstructor().newInstance();

      Map<String, Object> parameters = new HashMap<>();

      parameters.put("value", parameterType);
      parameters.put("length", length);
      //TODO: Deprecated  sun.reflect.annotation.AnnotationParser;
      return parameter;
    } catch (Throwable e){
      throw  new RuntimeException(e);
    }
  }

  public static Parameter createAnnotation(ParameterType parameterType)
      throws IllegalAccessException, InstantiationException {
    return Test_ParameterParse_String.createAnnotation(parameterType, Integer.MIN_VALUE);
  }

  @Test
  void Should_ReturnVarcharLimitedDatabase__When_JavaStringIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING, 4);
    String data = "hello world";
    Assertions.assertThat(
        ProcessDbParameter.parse(parameter, data)
    )
        .isInstanceOf(String.class)
        .isEqualTo(data.substring(0, 4));
  }

  @Test
  void Should_ReturnNullVarcharLimitedDatabase__When_JavaStringNullIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING, 4);
    String data = null;
    Assertions.assertThat(
        ProcessDbParameter.parse(parameter, data)
    )
        .isNull();
  }

  @Test
  void Should_ReturnVarcharUnlimitedDatabase__When_JavaStringIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING);
    String data = "hello world";
    Assertions.assertThat(
        ProcessDbParameter.parse(parameter, data)
    )
        .isInstanceOf(String.class)
        .isEqualTo(data);
  }

  @Test
  void Should_ReturnNullVarcharUnlimitedDatabase__When_JavaStringNullIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING);
    String data = null;
    Assertions.assertThat(
        ProcessDbParameter.parse(parameter, data)
    )
        .isNull();
  }

  @Test
  void Should_ReturnTextBlankLimitedPaddedDatabase__When_JavaStringIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING);
    String data = "hello world";
    Assertions.assertThat(

        ProcessDbParameter.parse(parameter, data)
    )
        .isInstanceOf(String.class)
        .isEqualTo(data);
  }

  @Test
  void Should_ReturnCharacterBlankLimitedPaddedNullDatabase__When_JavaStringNullIsPassed()
      throws InstantiationException, IllegalAccessException {
    Parameter parameter = createAnnotation(ParameterType.STRING);
    String data = null;
    Assertions.assertThat(
        ProcessDbParameter.parse(parameter, data)
    )
        .isNull();
  }
}