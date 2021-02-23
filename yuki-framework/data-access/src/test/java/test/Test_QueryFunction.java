package test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;
import yuki.framework.dataaccess.annotations.QueryDefinitionMetadata;
import yuki.framework.dataaccess.utils.DatabaseExecutionException;
import yuki.framework.dataaccess.utils.QueryDefinition;
import yuki.framework.dataaccess.utils.QueryExecutor;

@DisplayName("ss")
@ExtendWith(VertxExtension.class)
public class Test_QueryFunction {

	@Inject
	private Db db;

	@Inject
	private DbConfigurator dbConfigurator;

	@Inject
	private QueryExecutor queryExecutor;

	@BeforeEach
	void initialize() throws Throwable {
		final Injector injector = Guice.createInjector(new AbstractModule() {

		});

		injector.injectMembers(this);

		final JsonObject jdbcConfiguration = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_hatunmayu")
				.put("dbUser", "hatunmayu")
				.put("dbPassword", "moresecure");

		final Vertx vertx = Vertx.vertx();
		this.dbConfigurator.init(jdbcConfiguration, vertx);

		this.sync(this.db.getConnection()
				.preparedQuery("drop table if exists chanchito cascade;"));
		this.sync(this.db.getConnection()
				.preparedQuery("DROP FUNCTION if exists fn_echo_function;"));
		this.sync(this.db.getConnection()
				.preparedQuery("DROP FUNCTION if exists fn_create_lobby;"));
		this.sync(this.db.getConnection()
				.preparedQuery("create table chanchito(id serial, name varchar);"));
		this.sync(this.db.getConnection()
				.preparedQuery("CREATE OR REPLACE FUNCTION fn_echo_function ( v_string varchar, v_integer integer, v_double numeric, v_boolean boolean, v_instant date, v_timestamp timestamp, v_inputstream bytea)\n"//
						+ "\n"//
						+ "RETURNS table(c_string varchar, c_integer integer, c_double numeric, c_boolean boolean, c_instant date, c_timestamp timestamp, c_inputstream bytea)\n"//
						+ "\n"//
						+ "LANGUAGE plpgsql\n"//
						+ "AS $function$\n"//
						+ "     DECLARE  \n"//
						+ "     BEGIN        \n"//
						+ "        return query \n"//
						+ "            SELECT \n"//
						+ "                v_string as c_string,\n"//
						+ "                v_integer as c_integer,\n"//
						+ "                v_double as c_double,\n"//
						+ "                v_boolean as c_boolean,\n"//
						+ "                v_instant as c_instant,\n"//
						+ "                v_timestamp as c_timestamp,\n"//
						+ "                v_inputstream as c_inputstream;\n"//
						+ "    END; \n"//
						+ "$function$;"));
		this.sync(this.db.getConnection()
				.preparedQuery("CREATE OR REPLACE FUNCTION fn_create_lobby ( name text)\n"//
						+ "\n"//
						+ "RETURNS SETOF chanchito\n"//
						+ "\n"//
						+ "LANGUAGE plpgsql\n"//
						+ "AS $function$\n"//
						+ "     DECLARE  \n"//
						+ "        new_id chanchito.id%type;\n"//
						+ "     BEGIN\n"//
						+ "        INSERT INTO chanchito\n"//
						+ "            (\"name\")\n"//
						+ "        VALUES(name) returning id into new_id;\n"//
						+ "        \n"//
						+ "     \n"//
						+ "        return query \n"//
						+ "            SELECT * FROM chanchito WHERE id = new_id;\n"//
						+ "    END; \n"//
						+ "$function$;"));

	}

	private void sync(final PreparedQuery<RowSet<Row>> preparedQuery) throws Throwable {
		final Promise<Void> promise = Promise.promise();

		final Future<Void> future = promise.future();

		preparedQuery.execute(h -> {
			if (h.failed()) {
				promise.fail(h.cause());
				return;
			}
			promise.complete();
		});

		while (!future.isComplete()) {
			try {
				Thread.sleep(50);
			} catch (final Exception e) {
			}
		}

		if (future.failed()) {
			throw future.cause();
		}

	}

	@Test
	void Should_return_data_When_query_is_executed() throws InterruptedException {
		final VertxTestContext vertxTestContext = new VertxTestContext();

		final FnCreateLobby fnCreateLobby = this.queryExecutor.create(FnCreateLobby.class);
		fnCreateLobby.setName("Hello");

		fnCreateLobby.execute()
				.onComplete(h -> {
					Assertions.assertThat(h.result())
							.isEqualTo(new JsonArray().add(new JsonObject().put("id", 1)
									.put("name", "Hello")));
					vertxTestContext.completeNow();
				})
				.onFailure(vertxTestContext::failNow);

		Assertions.assertThat(vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS))
				.isTrue();
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

}

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_create_lobby( name:= $1 ) ", returnType = JsonArray.class)
interface FnCreateLobby extends QueryDefinition {

	void setName(String name);

}

@QueryDefinitionMetadata(sql = " SELECT * FROM fn_echo_function( v_string:= $1, v_integer:=$2 ,v_double:=$3 ,v_boolean:=$4 ,v_instant:=$5 ,v_timestamp:=$6 ,v_inputstream:=$7 )", returnType = JsonArray.class)
interface FnEcho extends QueryDefinition {

	void setV_string(String string);

	void setV_integer(Integer integer);

	void setV_double(Double double1);

	void setV_boolean(Boolean boolean1);

	void setV_instant(Instant instant);

	void setV_timestamp(Instant instant);

	void setV_inputstream(InputStream name);

}
