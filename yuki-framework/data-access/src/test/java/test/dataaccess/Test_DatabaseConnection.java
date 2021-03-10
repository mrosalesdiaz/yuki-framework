package test.dataaccess;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgException;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;

@ExtendWith(VertxExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
public class Test_DatabaseConnection {

    @Inject
    private Db db;
    @Inject
    private DbConfigurator dbConfigurator;

    public static ConfigRetriever loadConfiguration(final Vertx vertx, final String value) {
        final ConfigStoreOptions configStoreOptions = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", value));
        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions()
                .addStore(configStoreOptions);

        return ConfigRetriever.create(vertx, configRetrieverOptions);
    }

    @BeforeEach
    void initialize() {
        final Injector injector = Guice.createInjector(new AbstractModule() {

        });

        injector.injectMembers(this);
    }

    @Test
    @Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
    void Should_return_an_active_connection_When_it_is_well_configure(final Vertx vertx,
                                                                      final VertxTestContext testContext) {

        final ConfigRetriever configRetriever = Test_DatabaseConnection.loadConfiguration(vertx, "./config.json");

        final Promise<JsonObject> promiseReadConfig = Promise.promise();
        final Promise<Void> promiseDbConfiguration = Promise.promise();

        configRetriever.getConfig(promiseReadConfig::handle);

        promiseReadConfig.future()
                .onComplete(r -> {
                    r.result().stream().forEach(e -> vertx.getOrCreateContext().config().put(e.getKey(), e.getValue()));
                    this.dbConfigurator.init(vertx,
                            promiseDbConfiguration::handle);

                });

        promiseDbConfiguration.future()
                .onComplete(r -> testContext.verify(() -> {
                    Assertions.assertThat(this.db.getConnection())
                            .isNotNull();

                    testContext.completeNow();
                }));

    }

    @Test()
    void Should_thrown_exception_When_password_is_wrong(final Vertx vertx,
                                                        final VertxTestContext testContext) {
        final ConfigRetriever configRetriever = Test_DatabaseConnection.loadConfiguration(vertx, "./config.json");

        final Promise<JsonObject> promiseReadConfig = Promise.promise();
        Promise.promise();

        configRetriever.getConfig(promiseReadConfig::handle);

        promiseReadConfig.future()
                .onComplete(r -> testContext.verify(() -> {

                    r.result()
                            .put("dbPassword", "")
                            .stream()
                            .forEach(e -> vertx.getOrCreateContext().config().put(e.getKey(), e.getValue()));

                    this.dbConfigurator.init(vertx,
                            ar -> testContext.verify(() -> {
                                Assertions.assertThat(ar.cause().getClass()).isEqualTo(PgException.class);

                                testContext.completeNow();
                            }));

                }));

    }

}
