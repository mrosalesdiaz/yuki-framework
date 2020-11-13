package yuki.framework.dataschema;

import org.flywaydb.core.Flyway;

// date +"V3_%y%m%d%H%M%S__"
/***
 * 
 * SELECT n.nspname as "Schema", p.proname as "Name",
 * pg_catalog.pg_get_function_result(p.oid) as "Result data type",
 * pg_catalog.pg_get_function_arguments(p.oid) as "Argument data types", CASE
 * p.prokind WHEN 'a' THEN 'agg' WHEN 'w' THEN 'window' WHEN 'p' THEN 'proc'
 * ELSE 'func' END as "Type" FROM pg_catalog.pg_proc p LEFT JOIN
 * pg_catalog.pg_namespace n ON n.oid = p.pronamespace where n.nspname =
 * 'products' ORDER BY 1, 2, 4, 5;
 * 
 * select * from v_categories limit 0
 * 
 * 
 ****/
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
