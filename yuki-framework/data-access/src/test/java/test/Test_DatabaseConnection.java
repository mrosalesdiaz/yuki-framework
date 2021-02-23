package test;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import yuki.framework.dataaccess.Db;
import yuki.framework.dataaccess.DbConfigurator;
import yuki.framework.dataaccess.utils.DbConfigurationException;

public class Test_DatabaseConnection {

	@Inject
	private Db db;

	@Inject
	private DbConfigurator dbConfigurator;

	@BeforeEach
	void initialize() {
		final Injector injector = Guice.createInjector(new AbstractModule() {

		});

		injector.injectMembers(this);
	}

	@Test
	void Should_return_an_active_connection_When_it_is_well_configure() {
		final JsonObject jdbcConfiguration = new JsonObject().put("jdbcUrl", "postgresql://localhost/db_hatunmayu")
				.put("dbUser", "postgres")
				.put("dbPassword", "not24get");

		final Vertx vertx = Vertx.vertx();
		this.dbConfigurator.init(jdbcConfiguration, vertx);
		Assertions.assertThat(this.db.getConnection())
				.isNotNull();
	}

	@Test()
	void Should_thrown_exception_When_configuration_is_wrong() {
		org.junit.jupiter.api.Assertions.assertThrows(DbConfigurationException.class, () -> {
			final JsonObject jdbcConfiguration = new JsonObject().put("jdbcUrl", "postgresql://localhost_/db_hatunmayu")
					.put("dbUser", "postgres")
					.put("dbPassword", "not24get");

			final Vertx vertx = Vertx.vertx();
			this.dbConfigurator.init(jdbcConfiguration, vertx);
		});

	}

}
