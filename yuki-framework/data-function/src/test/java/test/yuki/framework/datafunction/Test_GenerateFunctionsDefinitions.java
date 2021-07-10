package test.yuki.framework.datafunction;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import yuki.framework.datafunction.dao.Ds;
import yuki.framework.datafunction.dao.FunctionsDao;
import yuki.framework.datafunction.generator.JavaFnClassGenerator;

@ExtendWith(MockitoExtension.class)
public class Test_GenerateFunctionsDefinitions {

  private Injector rootInjector;

  private static Stream<Arguments> correctListOfMethods() {
    JsonArray inParameters = new JsonArray();
    JsonArray expectedResult = new JsonArray();

    // Basic function
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_string character varying, p_string_2 character varying")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_string(java.lang.String p_string), void setP_string_2(java.lang.String p_string_2)")
    );

    // String parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments",
                "p_string varchar, p_text text, p_chracter_varying character varying")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_string(java.lang.String p_string), void setP_text(java.lang.String p_text), void setP_chracter_varying(java.lang.String p_chracter_varying)")
    );

    // Integer parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_integer int")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_integer(java.lang.Integer p_integer)")
    );
    // Double parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_double double")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_double(java.lang.Double p_double)")
    );
    // LocalDate parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_date date")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_date(java.time.LocalDate p_date)")
    );
    // LocalDateTime parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_timestamp timestamp")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_timestamp(java.time.LocalDateTime p_timestamp)")
    );
    // LocalTime parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_time time")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_time(java.time.LocalTime p_time)")
    );
    // Boolean parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_boolean bool")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_boolean(java.lang.Boolean p_boolean)")
    );
    // Bytes parameter
    inParameters.add(
        new JsonObject()
            .put("id", 1)
            .put("schemaName", "authentication")
            .put("functionName", "fn_authentication__list_elements")
            .put("hasMany", false)
            .put("functionArguments", "p_bytea bytea")
            .put("functionReturn", "bool")
    );
    expectedResult.add(
        new JsonObject()
            .put("filePath", "yuki/gen/db/fn/authentication/FnListElements.java")
            .put("className", "FnListElements")
            .put("packageName",
                String.format("%s.authentication", JavaFnClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
            .put("methodDefinitions",
                "void setP_bytea(java.lang.Byte[] p_bytea)")
    );

    return Stream.of(
        Arguments.of(inParameters, expectedResult));
  }

  @BeforeEach
  void beforeEach() throws SQLException {

    HikariConfig hikariConfig = Mockito.mock(HikariConfig.class);
    Ds ds = Mockito.mock(Ds.class);
    Connection connection = Mockito.mock(Connection.class);
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito.doReturn(connection).when(ds).getConnection();

    rootInjector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(HikariConfig.class).toInstance(hikariConfig);
        bind(Ds.class).toInstance(ds);

        // For this test
        bind(PreparedStatement.class).toInstance(preparedStatement);
        bind(ResultSet.class).toInstance(resultSet);
      }
    });
  }

  @ParameterizedTest
  @MethodSource("correctListOfMethods")
  void Should_ReturnJavaFunctionDefinitions__When_FunctionsAreReadFromDatabase(JsonArray testInput,
      JsonArray expectedResult)
      throws SQLException, IOException {

    // Prepare Mock
    Connection connection = rootInjector.getInstance(Ds.class).getConnection();
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito
        .when(connection.prepareStatement(
            Mockito.startsWith("-- List all functions defined in a schema passed as parameter")))
        .thenReturn(preparedStatement);
    Mockito.doReturn(resultSet)
        .when(preparedStatement)
        .executeQuery();

    AtomicInteger atomicInteger = new AtomicInteger(-1);

    Mockito
        .when(resultSet.next())
        .thenAnswer(new Answer<Boolean>() {
          private int index = testInput.size();

          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
            atomicInteger.addAndGet(1);
            return index-- > 0;
          }
        });

    Mockito
        .when(resultSet.getString(Mockito.anyString()))
        .thenAnswer(invocation -> testInput.getJsonObject(atomicInteger.get())
            .getString(invocation.getArgument(0)));

    Mockito
        .when(resultSet.getBoolean(Mockito.anyString()))
        .thenAnswer(invocation -> testInput.getJsonObject(atomicInteger.get())
            .getBoolean(invocation.getArgument(0)));

    Mockito
        .when(resultSet.getInt(Mockito.anyString()))
        .thenAnswer(invocation -> testInput.getJsonObject(atomicInteger.get())
            .getInteger(invocation.getArgument(0)));

    //Execute
    JavaFnClassGenerator generator = rootInjector.getInstance(JavaFnClassGenerator.class);
    FunctionsDao functionsDao = rootInjector.getInstance(FunctionsDao.class);

    JsonArray listOfFunctionDefinitions = functionsDao.readFunctions("authentication");

    Assertions.assertThat(listOfFunctionDefinitions)
        .hasSize(testInput.size());

    Assertions.assertThat(listOfFunctionDefinitions.getJsonObject(0).getMap())
        .containsOnlyKeys(
            "id",
            "schemaName",
            "functionName",
            "hasMany",
            "functionArguments",
            "functionReturn"
        );

    JsonArray generatedJavaClasses = generator.generate(listOfFunctionDefinitions);

    Assertions.assertThat(generatedJavaClasses)
        .hasSize(testInput.size());

    List<Executable> producers = new ArrayList<>();

    for (int i = 0; i < testInput.size(); i++) {
      final int currentIndex = i;

      JsonObject currentExpectedResult = expectedResult.getJsonObject(currentIndex);
      JsonObject actualGeneratedResult = generatedJavaClasses.getJsonObject(currentIndex);

      producers.add(() -> {
        Assertions.assertThat(actualGeneratedResult.getMap())
            .containsOnlyKeys(
                "filePath",
                "classSource"
            )
            .containsEntry("filePath", currentExpectedResult.getString("filePath"));

        CompilationUnit compilationUnit = StaticJavaParser
            .parse(
                actualGeneratedResult
                    .getString("classSource")
            );

        Assertions.assertThat(
            compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get().getNameAsString()
        )
            .isEqualTo(
                currentExpectedResult
                    .getString("className")
            );

        Assertions.assertThat(
            compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get()
                .getFullyQualifiedName()
                .get()
        )
            .startsWith(
                currentExpectedResult
                    .getString("packageName")
            );

        Assertions.assertThat(
            compilationUnit
                .findFirst(ClassOrInterfaceDeclaration.class).get()
                .findAll(MethodDeclaration.class)
                .stream()
                .map(m -> m.getDeclarationAsString(true, true, true))
                .map(String::trim)
                .collect(Collectors.joining(", "))
        )
            .isEqualTo(
                expectedResult.getJsonObject(currentIndex)
                    .getString("methodDefinitions")
            );
      });
    }

    org.junit.jupiter.api.Assertions.assertAll(producers);
  }

}
