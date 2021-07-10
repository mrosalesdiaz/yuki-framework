package test.dataaccess;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.common.DbConfigException;
import yuki.framework.dataaccess.dto.DbConfig;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class Test_DatabaseConnection {

  @Test
  void Should_return_an_active_connection_When_it_is_well_configure(final Vertx vertx,
      final VertxTestContext testContext) {

    final Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(DbConfig.class).toInstance(DbConfig.from(
            new JsonObject()
                .put("jdbcUrl", "postgresql://levi-db/db_levi?search_path=unittest")
                .put("dbUser", "levi")
                .put("dbPassword", "moresecure")
        ));
        bind(Vertx.class).toInstance(vertx);
      }
    });

    try {
      injector.getInstance(Db.class).getPgPool();

      testContext.completeNow();
    } catch (Throwable ex) {
      testContext.failNow(ex);
    }

  }
// TODO: skipped replace test for database
 // @Test()
  void Should_thrown_DbConfigException_When_wrongPasswordIsPassed(final Vertx vertx,
      final VertxTestContext testContext) {
    final Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(DbConfig.class).toInstance(DbConfig.from(
            new JsonObject()
                .put("jdbcUrl", "postgresql://levi-db/db_levi?search_path=unittest")
                .put("dbUser", "levi")
                .put("dbPassword", "---")
        ));
        bind(Vertx.class).toInstance(vertx);
      }
    });

    Assertions.assertThatCode(() -> {
      injector.getInstance(Db.class).getPgPool();
    }).isExactlyInstanceOf(DbConfigException.class);

    testContext.completeNow();
  }
  // TODO: skipped replace test for database
  //@Test()
  void Should_ReturnCorrectDbConfig__When_correctJsonObjectIsPassed() {
    JsonObject vertxConfig = new JsonObject()
        .put("jdbcUrl", "postgresql://localhost/db_test")
        .put("dbUser", "postgres")
        .put("dbPassword", "secure")
        .put("poolMaxSize", 4)
        .put("poolMaxWaitQueueSize", 2);

    DbConfig dbConfig = DbConfig.from(vertxConfig);
    Assertions.assertThat(dbConfig)
        .isNotNull();

    Assertions.assertThat(dbConfig.getJdbcUrl())
        .isEqualTo(vertxConfig.getString("jdbcUrl"));
    Assertions.assertThat(dbConfig.getUser())
        .isEqualTo(vertxConfig.getString("dbUser"));
    Assertions.assertThat(dbConfig.getPassword())
        .isEqualTo(vertxConfig.getString("dbPassword"));
    Assertions.assertThat(dbConfig.getMaxSize())
        .isEqualTo(vertxConfig.getInteger("poolMaxSize"));
    Assertions.assertThat(dbConfig.getMaxWaitQueueSize())
        .isEqualTo(vertxConfig.getInteger("poolMaxWaitQueueSize"));
  }

  @Test()
  void Should_ReturnDefaultDbConfig__When_EmptyJsonIsPassed() {
    JsonObject vertxConfig = new JsonObject();

    DbConfig dbConfig = DbConfig.from(vertxConfig);
    Assertions.assertThat(dbConfig)
        .isNotNull();

    Assertions.assertThat(dbConfig.getJdbcUrl())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getUser())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getPassword())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getMaxSize())
        .isEqualTo(4);
    Assertions.assertThat(dbConfig.getMaxWaitQueueSize())
        .isEqualTo(-1);
  }

  @Test()
  void Should_ReturnDefaultDbConfig__When_NullJsonObjectIsPassed() {
    JsonObject vertxConfig = null;

    DbConfig dbConfig = DbConfig.from(vertxConfig);
    Assertions.assertThat(dbConfig)
        .isNotNull();

    Assertions.assertThat(dbConfig.getJdbcUrl())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getUser())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getPassword())
        .isEqualTo("");
    Assertions.assertThat(dbConfig.getMaxSize())
        .isEqualTo(4);
    Assertions.assertThat(dbConfig.getMaxWaitQueueSize())
        .isEqualTo(-1);
  }

}