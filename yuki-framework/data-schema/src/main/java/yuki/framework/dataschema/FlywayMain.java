package yuki.framework.dataschema;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
            final String schema) {
        FlywayMain.executeFlayway(action, url, user, password, schema, false);

    }

    public static void executeFlayway(final String action, final String url, final String user, final String password,
            final String schema, final boolean cleanOnValidationError) {
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .schemas(schema)
                .cleanOnValidationError(cleanOnValidationError)
                .load();

        if ("migrate".equalsIgnoreCase(action)) {
            flyway.migrate();
        } else if ("clean".equalsIgnoreCase(action)) {
            flyway.clean();
        } else {
            System.out.println("action not supported.");
        }

    }

    private static CommandLine getCommanLine(final String[] args) throws ParseException {
        final Options options = new Options();

        options.addOption(new Option("s", "server", true, "JDBC Url to connect database"));
        options.addOption(new Option("u", "user", true, "Database user"));
        options.addOption(new Option("p", "password", true, "Database password"));
        options.addOption(new Option("sch", "schemas", true, "Schema names separated by coma"));
        options.addOption(new Option("a", "action", true, "The action to execute (migrate|clean)"));

        final CommandLineParser commandLineParser = new DefaultParser();
        final CommandLine commandLine = commandLineParser.parse(options, args);
        System.out.println(commandLine.getOptionValue("server"));
        return commandLine;
    }

    public static void main(final String[] args) throws ParseException {
        System.out.println(args.length);
        System.out.println(args);
        System.out.println(":" + String.join(",", args) + ":");

        final CommandLine commandLine = FlywayMain.getCommanLine(args);

        final String action = commandLine.getOptionValue("a");
        final String url = commandLine.getOptionValue("s");
        final String user = commandLine.getOptionValue("u");
        final String password = commandLine.getOptionValue("p");
        final String schema = commandLine.getOptionValue("sch");
        System.out.println("action: " + action);
        FlywayMain.executeFlayway(action, url, user, password, schema);
    }
}
