package yuki.framework.dataschema;

import org.flywaydb.core.Flyway;

// date +"V3_%y%m%d%H%M%S__"
public class FlywayMain {
	public static void executeFlayway(final String action, final String url, final String user, final String password,
			final String schema, final boolean cleanOnValidationError) {
		final Flyway flyway = Flyway.configure().dataSource(url, user, password).schemas(schema)
				.cleanOnValidationError(cleanOnValidationError).load();

		if ("migrate".equalsIgnoreCase(action)) {
			flyway.migrate();
		} else if ("clean".equalsIgnoreCase(action)) {
			flyway.clean();
		} else {
			System.out.println("action not supported.");
		}
	}

	public static void executeFlayway(final String action, final String url, final String user, final String password,
			final String schema) {
		FlywayMain.executeFlayway(action, url, user, password, schema, false);

	}
}
