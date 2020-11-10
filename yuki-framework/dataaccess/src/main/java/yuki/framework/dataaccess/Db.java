package yuki.framework.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.vertx.core.json.JsonObject;

public class Db {
	private final HikariConfig config = new HikariConfig();
	private HikariDataSource ds;

	void init(final JsonObject configuration) {
		this.config.setJdbcUrl(configuration.getString("jdbcUrl"));
		this.config.setUsername(configuration.getString("dbUser"));
		this.config.setPassword(configuration.getString("dbPassword"));
		this.config.addDataSourceProperty("cachePrepStmts", "true");
		this.config.addDataSourceProperty("prepStmtCacheSize", "250");
		this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		this.ds = new HikariDataSource(this.config);

	}

	public Connection getConnection() throws SQLException {
		return this.ds.getConnection();
	}
}
