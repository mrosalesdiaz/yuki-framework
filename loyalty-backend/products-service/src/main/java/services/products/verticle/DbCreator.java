package services.products.verticle;

import yuki.framework.dataschema.FlywayMain;

public class DbCreator {
	public static void main(final String[] args) {
		FlywayMain.executeFlayway("migrate", "jdbc:postgresql://localhost/db_loyalty", "loyalty", "moresecure",
				"products");
	}
}
