package yuki.framework.dataaccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import java.util.logging.Logger;
import yuki.common.logging.Level;
import yuki.framework.dataaccess.dto.DbConfig;

/**
 * Singleton class to access database connection.
 * <p>
 *
 * @author mrosalesdiaz
 */
@Singleton
public class Db {

  public static final Logger LOGGER = Logger.getLogger(Db.class.getName());

  private static final String TEST_SQL = " select 'chanchito' ";

  private PgPool pgPool;

  @Inject
  private DbConfig dbConfig;

  @Inject
  private Vertx vertx;

  public PgPool getPgPool() {
    if (pgPool == null) {
      LOGGER.log(Level.DEBUG, "Creating pool");

      LOGGER.log(Level.CONFIG, () -> String.format("\tJdbcUrl: %s", dbConfig.getJdbcUrl()));
      LOGGER.log(Level.CONFIG, () -> String.format("\tdbUser: %s", dbConfig.getUser()));
      LOGGER.log(Level.CONFIG, () -> "\tdbPassword: ****");
      LOGGER.log(Level.CONFIG, () -> String.format("\tpoolMaxSize: %s", dbConfig.getMaxSize()));
      LOGGER.log(Level.CONFIG,
          () -> String.format("\tpoolMaxWaitQueueSize: %s", dbConfig.getMaxWaitQueueSize()));

      final PgConnectOptions connectOptions = PgConnectOptions.fromUri(dbConfig.getJdbcUrl())
          .setUser(dbConfig.getUser())
          .setPassword(dbConfig.getPassword());

      final PoolOptions poolOptions = new PoolOptions()
          .setMaxSize(dbConfig.getMaxSize())
          .setMaxWaitQueueSize(dbConfig.getMaxWaitQueueSize());

      this.pgPool = PgPool.pool(vertx, connectOptions, poolOptions);

      this.pgPool.query(TEST_SQL)
          .execute()
          .onComplete(d -> LOGGER.log(Level.INFO, () -> String.format("Test Query Executed.")));
    }

    return this.pgPool;
  }

}
