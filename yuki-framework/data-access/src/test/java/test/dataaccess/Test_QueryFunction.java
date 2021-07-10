package test.dataaccess;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.cli.ParseException;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test.dataaccess.common.TestHelper;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.dto.DbConfig;
import yuki.framework.dataaccess.utils.QueryDefinition;
import yuki.framework.dataaccess.utils.QueryExecutor;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_echo_function( v_string:= $1, v_integer:=$2 ,v_double:=$3 ,v_boolean:=$4 ,v_instant:=$5 ,v_timestamp:=$6 ,v_inputstream:=$7 )")
interface FnEcho extends QueryDefinition {

  @Parameter(ParameterType.BOOLEAN)
  void setV_boolean(Boolean boolean1);

  @Parameter(ParameterType.DOUBLE)
  void setV_double(Double double1);

  @Parameter(ParameterType.BYTEA)
  void setV_inputstream(InputStream name);

  @Parameter(ParameterType.DATE)
  void setV_instant(Instant instant);

  @Parameter(ParameterType.INTEGER)
  void setV_integer(Integer integer);

  @Parameter(ParameterType.STRING)
  void setV_string(String string);

  @Parameter(ParameterType.DATETIME)
  void setV_timestamp(Instant instant);

}

@ExtendWith(VertxExtension.class)
public class Test_QueryFunction {

  private final static String JSON_CONFIG_FILE = "./test-config.json";

  @AfterAll
  static void afterAll() throws ParseException, InterruptedException {
    JsonObject config = TestHelper.getConfig(JSON_CONFIG_FILE);

    URI jdbcUri = URI.create(
        String.format("dummy://%s", config.getString("jdbcUrl").split("\\:\\/\\/")[1]));
    String dbUser = config.getString("dbUser");
    String dbPassword = config.getString("dbPassword");

    Flyway flyway = Flyway.configure()
        .dataSource(String.format("jdbc:postgresql://%s:%s%s", jdbcUri.getHost(),
            Math.max(jdbcUri.getPort(), 5432),
            jdbcUri.getPath()), dbUser, dbPassword)
        .schemas("unittest")
        .outOfOrder(true)
        .cleanOnValidationError(false)
        .load();

    flyway.clean();
  }

  @BeforeEach
  void beforeEach() throws Throwable {
    JsonObject config = TestHelper.getConfig(JSON_CONFIG_FILE);

    URI jdbcUri = URI.create(
        String.format("dummy://%s", config.getString("jdbcUrl").split("\\:\\/\\/")[1]));
    String dbUser = config.getString("dbUser");
    String dbPassword = config.getString("dbPassword");

    Flyway flyway = Flyway.configure()
        .dataSource(String.format("jdbc:postgresql://%s:%s%s", jdbcUri.getHost(),
            Math.max(jdbcUri.getPort(), 5432),
            jdbcUri.getPath()), dbUser, dbPassword)
        .schemas("unittest")
        .outOfOrder(true)
        .cleanOnValidationError(false)
        .load();

    flyway.clean();
    flyway.migrate();
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
  void Should_return_boolean_When_boolean_parameter_is_passed(Vertx vertx,
      final VertxTestContext vertxTestContext)
      throws Throwable {

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_boolean(false);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> vertx.executeBlocking(p -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);
        Assertions.assertThat(jsonObject.getBoolean("c_boolean"))
            .isEqualTo(false);
      });
      p.complete();
    });

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_double_When_double_parameter_is_passed(Vertx vertx,
      VertxTestContext vertxTestContext)
      throws Throwable {

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_double(99.99);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);
        Assertions.assertThat(jsonObject.getDouble("c_double"))
            .isEqualTo(99.99);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_inputstream_When_inputstream_parameter_is_passed(
      Vertx vertx, VertxTestContext vertxTestContext)
      throws Throwable {

    final String data = "test test";
    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_inputstream(new ByteArrayInputStream(data.getBytes()));

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);
        Assertions.assertThat(new String(jsonObject.getBinary("c_inputstream")))
            .isEqualTo(data);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_instant_date_When_instant_with_time_parameter_is_passed(
      Vertx vertx, VertxTestContext vertxTestContext)
      throws Throwable {

    final Instant now = Instant.now();
    final Instant now2 = now.truncatedTo(ChronoUnit.DAYS);

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_instant(now);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);
        Assertions.assertThat(jsonObject.getInstant("c_instant"))
            .isEqualTo(now2);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  // @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_instant_When_instant_parameter_is_passed(
      Vertx vertx, VertxTestContext vertxTestContext)
      throws Throwable {

    final Instant now = Instant.now()
        .truncatedTo(ChronoUnit.DAYS);

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);

        Assertions.assertThat(jsonObject.getInstant("c_instant"))
            .isEqualTo(now);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_instant_with_time_When_instant_with_time_parameter_is_passed(
      Vertx vertx, VertxTestContext vertxTestContext) throws Throwable {

    final Instant now = Instant.now()
        .truncatedTo(ChronoUnit.DAYS);

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_timestamp(now);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);

        Assertions.assertThat(jsonObject.getInstant("c_timestamp"))
            .isEqualTo(now);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
  void Should_return_integer_When_integer_parameter_is_passed(
      Vertx vertx, VertxTestContext vertxTestContext)
      throws Throwable {

    final Instant now = Instant.now()
        .truncatedTo(ChronoUnit.DAYS);

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_integer(9);

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);

        Assertions.assertThat(jsonObject.getDouble("c_integer"))
            .isEqualTo(9);
      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());
  }

  @Test
  @Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
  void Should_return_string_When_string_parameter_is_passed(Vertx vertx,
      VertxTestContext vertxTestContext)
      throws Throwable {

    final Instant now = Instant.now()
        .truncatedTo(ChronoUnit.DAYS);

    Function<JsonObject, Future<Injector>> createInjector = config -> {
      vertx.getOrCreateContext().config().mergeIn(config);

      return Future.succeededFuture(Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Vertx.class).toInstance(vertx);
          bind(DbConfig.class).toInstance(DbConfig.from(config));
        }
      }));
    };

    Function<Injector, Future<JsonArray>> executeFunction = injector -> {
      final FnEcho fnEcho = injector.getInstance(QueryExecutor.class).create(FnEcho.class);

      fnEcho.setV_string("Hello");

      return fnEcho.execute();
    };

    Function<JsonArray, Future<Void>> verifyResult = result -> {
      vertxTestContext.verify(() -> {
        final JsonObject jsonObject = result.getJsonObject(0);

        Assertions.assertThat(jsonObject.getString("c_string"))
            .isEqualTo("Hello");

      });
      return Future.succeededFuture();
    };

    TestHelper.getConfig(vertx, JSON_CONFIG_FILE)
        .compose(createInjector)
        .compose(executeFunction)
        .compose(verifyResult)
        .onFailure(vertxTestContext::failNow)
        .onSuccess(r -> vertxTestContext.completeNow());

  }

}
