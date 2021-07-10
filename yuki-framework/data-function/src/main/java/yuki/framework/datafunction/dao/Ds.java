package yuki.framework.datafunction.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

@Singleton
public class Ds {

  @Inject
  private HikariConfig hikariConfig;

  private HikariDataSource hikariDataSource;

  public Connection getConnection() throws SQLException {
    return getDataSource().getConnection();
  }

  public DataSource getDataSource() {
    if (hikariDataSource == null) {
      hikariDataSource = new HikariDataSource(this.hikariConfig);
    }
    return hikariDataSource;
  }

  public void close() {
    try {
      hikariDataSource.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    hikariDataSource = null;
  }

}
