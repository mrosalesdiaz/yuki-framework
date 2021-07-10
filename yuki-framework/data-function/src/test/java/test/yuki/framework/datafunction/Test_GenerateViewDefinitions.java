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
import java.util.stream.IntStream;
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
import yuki.framework.datafunction.dao.ViewsDao;
import yuki.framework.datafunction.generator.JavaViewClassGenerator;

@ExtendWith(MockitoExtension.class)
public class Test_GenerateViewDefinitions {

  private Injector rootInjector;

  private static Stream<Arguments> correctListOfViews() {
    JsonArray inParameters = new JsonArray();
    JsonArray expectedResult = new JsonArray();

    // Basic view
    inParameters.add(
        new JsonArray()
            .add(
                new JsonObject()
                    .put("id", 1)
                    .put("table_schema", "authentication")
                    .put("table_name", "v_queue")
                    .put("column_name", "c_varchar")
                    .put("data_type", "varchar")
            )
            .add(
                new JsonObject()
                    .put("id", 1)
                    .put("table_schema", "authentication")
                    .put("table_name", "v_queue")
                    .put("column_name", "c_integer")
                    .put("data_type", "int")
            )
    );
    expectedResult.add(
        new JsonArray()
            .add(
                new JsonObject()
                    .put("filePath", "yuki/gen/db/view/authentication/VQueue.java")
                    .put("className", "VQueue")
                    .put("packageName",
                        String.format("%s.authentication",
                            JavaViewClassGenerator.DEFAULT_GEN_PACKAGE_NAME))
                    .put("methodDefinitions",
                        "@Get(\"c_varchar\")java.lang.String getC_varchar();, @Get(\"c_integer\")java.lang.Integer getC_integer();")
            )
    );

    return
        IntStream.range(0, inParameters.size())
            .mapToObj(
                i -> Arguments.of(inParameters.getJsonArray(i), expectedResult.getJsonArray(i)))
            .collect(Collectors.toList())
            .stream();
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
  @MethodSource("correctListOfViews")
  void Should_ReturnJavaFunctionDefinitions__When_FunctionsAreReadFromDatabase(JsonArray testInput,
      JsonArray expectedResult)
      throws SQLException, IOException {

    // Prepare Mock
    Connection connection = rootInjector.getInstance(Ds.class).getConnection();
    PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    ResultSet resultSet = Mockito.mock(ResultSet.class);

    Mockito
        .when(connection.prepareStatement(
            Mockito.startsWith("-- List all vies defined in a schema passed as parameter")))
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
        .thenAnswer(invocation -> testInput.getJsonObject(atomicInteger.get())//error
            .getString(invocation.getArgument(0)));

    //Execute
    JavaViewClassGenerator generator = rootInjector.getInstance(JavaViewClassGenerator.class);
    ViewsDao viewsDao = rootInjector.getInstance(ViewsDao.class);

    JsonArray listOfViewDefinitions = viewsDao.readViews("authentication");

    Assertions.assertThat(listOfViewDefinitions)
        .hasSize(expectedResult.size());

    Assertions.assertThat(listOfViewDefinitions.getJsonObject(0).getMap())
        .containsOnlyKeys(
            "schemaName",
            "viewName",
            "columns"
        );

    JsonArray generatedJavaClasses = generator.generate(listOfViewDefinitions);

    Assertions.assertThat(generatedJavaClasses)
        .hasSize(expectedResult.size());

    List<Executable> producers = new ArrayList<>();

    for (int i = 0; i < expectedResult.size(); i++) {
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
                .map(MethodDeclaration::toString)
                .map(String::trim)
                .collect(Collectors.joining(", "))
                .replace("\n","")
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
