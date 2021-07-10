package test.dataaccess.common;

import com.google.common.base.Supplier;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.ParseException;
import yuki.framework.dataschema.FlywayMain;


public class TestHelper {

  public static void dbClean(final String path) throws InterruptedException, ParseException {
    final JsonObject configResult = TestHelper.getConfig(path);
    final URI jdbcUri = URI.create(configResult.getString("jdbcUrl"));

    final String server = "jdbc:" + jdbcUri.toString().split("\\?")[0];
    final String dbUser = configResult.getString("dbUser");
    final String dbPassword = configResult.getString("dbPassword");
    final String schema = jdbcUri.getQuery().split("\\=")[1];
    final String actionClean = "clean";

    FlywayMain.main(new String[]{"--server", server, "--user", dbUser,
        "--password", dbPassword, "--schemas", schema, "--action", actionClean});

  }

  public static void dbCleanAndMigrate(final String path)
      throws ParseException, InterruptedException {
    final JsonObject configResult = TestHelper.getConfig(path);
    final URI jdbcUri = URI.create(configResult.getString("jdbcUrl"));

    final String server = "jdbc:" + jdbcUri.toString().split("\\?")[0];
    final String dbUser = configResult.getString("dbUser");
    final String dbPassword = configResult.getString("dbPassword");
    final String schema = jdbcUri.getQuery().split("\\=")[1];
    final String actionClean = "clean";
    final String actionMigrate = "migrate";

    FlywayMain.main(new String[]{"--server", server, "--user", dbUser,
        "--password", dbPassword, "--schemas", schema, "--action", actionClean});

    FlywayMain.main(new String[]{"--server", server, "--user", dbUser,
        "--password", dbPassword, "--schemas", schema, "--action", actionMigrate});
  }

  public static JsonObject getConfig(final String path) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    final JsonObject configResult = new JsonObject();
    final Promise<JsonObject> config = Promise.promise();
    final Vertx vertx = Vertx.vertx();
    TestHelper.loadConfiguration(vertx, path).getConfig(config::handle);

    config.future().onComplete(s -> {
      s.result().stream().forEach(e -> configResult.put(e.getKey(), e.getValue()));
      latch.countDown();
    });

    latch.await(60, TimeUnit.SECONDS);
    return configResult;
  }

  public static ConfigRetriever loadConfiguration(final Vertx vertx, final String value) {
    final ConfigStoreOptions configStoreOptions = new ConfigStoreOptions().setType("file")
        .setConfig(new JsonObject().put("path", value));

    final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
        .addStore(configStoreOptions);
    return ConfigRetriever.create(vertx, configRetrieverOptions);
  }

  public static <T> Optional<T> ifException(Supplier<T> supplier) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  public static void catchException(VertxTestContext.ExecutionBlock executionBlock,
      Handler<Throwable> throwableHandler) {
    try {
      executionBlock.apply();
    } catch (Throwable e) {
      throwableHandler.handle(e);
    }
  }

  public static Future<Void> deployVerticle(Vertx vertx, String vertxClass,
      DeploymentOptions deploymentOptions) {
    Promise<Void> promise = Promise.promise();

    vertx.deployVerticle(
        vertxClass,
        deploymentOptions, r -> {
          if (r.failed()) {
            promise.fail(r.cause());
            r.cause().printStackTrace();
            return;
          }
          promise.complete();
        });

    return promise.future();
  }

  public static Future<JsonObject> getConfig(Vertx vertx, final String path) {
    ConfigStoreOptions configStoreOptions = new ConfigStoreOptions().setType("file")
        .setConfig(new JsonObject().put("path", path));

    ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
        .addStore(configStoreOptions);

    return ConfigRetriever.create(vertx, configRetrieverOptions).getConfig();
  }

}
