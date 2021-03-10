package test.dataaccess;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.cli.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import test.dataaccess.common.TestHelper;
import yuki.framework.dataaccess.DbConfigurator;
import yuki.framework.dataaccess.annotations.Parameter;
import yuki.framework.dataaccess.annotations.ParameterType;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.DatabaseExecutionException;
import yuki.framework.dataaccess.utils.QueryDefinition;
import yuki.framework.dataaccess.utils.QueryExecutor;

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_echo_function( v_string:= $1, v_integer:=$2 ,v_double:=$3 ,v_boolean:=$4 ,v_instant:=$5 ,v_timestamp:=$6 ,v_inputstream:=$7 )")
interface FnEcho extends QueryDefinition {

    @Parameter(ParameterType.BOOLEAN)
    void setV_boolean(Boolean boolean1);

    @Parameter(ParameterType.NUMERIC)
    void setV_double(Double double1);

    @Parameter(ParameterType.BYTEA)
    void setV_inputstream(InputStream name);

    @Parameter(ParameterType.DATE)
    void setV_instant(Instant instant);

    @Parameter(ParameterType.INTEGER)
    void setV_integer(Integer integer);

    @Parameter(ParameterType.VARCHAR)
    void setV_string(String string);

    @Parameter(ParameterType.TIMESTAMP_WITHOUT_TIME_ZONE)
    void setV_timestamp(Instant instant);

}

@ExtendWith(VertxExtension.class)
public class Test_QueryFunction {
    @AfterAll
    static void cleanUp() throws ParseException, InterruptedException {
        TestHelper.dbClean("./config.json");
    }

    @BeforeAll
    static void init() throws ParseException, InterruptedException {
        TestHelper.dbCleanAndMigrate("./config.json");
    }

    @Inject
    private DbConfigurator dbConfigurator;

    @Inject
    private QueryExecutor queryExecutor;

    @BeforeEach
    void initialize() throws Throwable {
        final Injector injector = Guice.createInjector(new AbstractModule() {

        });
        injector.injectMembers(this);

        final CountDownLatch latch = new CountDownLatch(1);

        final Promise<JsonObject> config = Promise.promise();
        final Vertx vertx = Vertx.vertx();
        TestHelper.loadConfiguration(vertx, "./config.json").getConfig(config::handle);

        config.future().onComplete(s -> {
            s.result().stream().forEach(e -> vertx.getOrCreateContext().config().put(e.getKey(), e.getValue()));
            this.dbConfigurator.init(vertx, e -> {
                latch.countDown();
            });
        });

        latch.await(60, TimeUnit.SECONDS);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_boolean_When_boolean_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        fnEcho.setV_boolean(false);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getBoolean("c_boolean"))
                            .isEqualTo(false);

                    vertxTestContext.completeNow();

                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_double_When_double_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        fnEcho.setV_double(99.99);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getDouble("c_double"))
                            .isEqualTo(99.99);

                    vertxTestContext.completeNow();

                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_inputstream_When_inputstream_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        final String data = "test test";
        fnEcho.setV_inputstream(new ByteArrayInputStream(data.getBytes()));

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(new String(jsonObject.getBinary("c_inputstream")))
                            .isEqualTo(data);

                    vertxTestContext.completeNow();

                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_instant_date_When_instant_with_time_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        final Instant now = Instant.now();
        fnEcho.setV_instant(now);
        final Instant now2 = now.truncatedTo(ChronoUnit.DAYS);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getInstant("c_instant"))
                            .isEqualTo(now2);

                    vertxTestContext.completeNow();

                }))
                .onFailure(e -> vertxTestContext.verify(() -> {
                    Assertions.assertThat(e.getClass())
                            .isEqualTo(DatabaseExecutionException.class);
                    vertxTestContext.completeNow();
                }));
    }

    // @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_instant_When_instant_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        final Instant now = Instant.now()
                .truncatedTo(ChronoUnit.DAYS);
        fnEcho.setV_instant(now);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getInstant("c_instant"))
                            .isEqualTo(now);

                    vertxTestContext.completeNow();

                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_instant_with_time_When_instant_with_time_parameter_is_passed(
            final VertxTestContext vertxTestContext) throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        final Instant now = Instant.now();
        fnEcho.setV_timestamp(now);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getInstant("c_timestamp"))
                            .isEqualTo(now);

                    vertxTestContext.completeNow();
                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void Should_return_integer_When_integer_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        fnEcho.setV_integer(9);

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getDouble("c_integer"))
                            .isEqualTo(9);

                    vertxTestContext.completeNow();

                }))
                .onFailure(vertxTestContext::failNow);
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
    void Should_return_string_When_string_parameter_is_passed(final VertxTestContext vertxTestContext)
            throws Throwable {

        final FnEcho fnEcho = this.queryExecutor.create(FnEcho.class);

        fnEcho.setV_string("Hello");

        fnEcho.execute()
                .onSuccess(h -> vertxTestContext.verify(() -> {
                    final JsonObject jsonObject = h.getJsonObject(0);

                    Assertions.assertThat(jsonObject.getString("c_string"))
                            .isEqualTo("Hello");

                    vertxTestContext.completeNow();
                }))
                .onFailure(vertxTestContext::failNow);
    }

}
