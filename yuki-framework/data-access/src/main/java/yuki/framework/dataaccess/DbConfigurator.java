package yuki.framework.dataaccess;

import javax.inject.Inject;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Database configuration object it configures the connections and also test the
 * singleton {@link Db}.
 *
 * @author mrosalesdiaz
 *
 */
public class DbConfigurator {
    @Inject
    private Db db;

    /**
     * @param vertx             Instance to use at time pool is created.
     *                          Configurations must be passed with following
     *                          properties in the Json object configuration:<br>
     *                          <ul>
     *                          <li>dbUser: database user name</li>
     *                          <li>dbPassword: database user password</li>
     *                          <li>jdbcUrl: url to connect the Postgresql database.
     *                          ex:
     *                          postgresql://localhost/db_levi?search_path=authentication</li>
     *                          </ul>
     * @param completionHandler handler for async completion
     */
    public void init(final Vertx vertx,
            final Handler<AsyncResult<Void>> completionHandler) {
        try {
            this.db.init(vertx.getOrCreateContext().config(), vertx);

            this.preInitialize(completionHandler);
        } catch (final Throwable e) {
            completionHandler.handle(Future.failedFuture(e));
            return;
        }
    }

    /**
     * Test connection by executing a simple dummy query
     *
     * @param completionHandler to notify when completed
     *
     */
    private void preInitialize(final Handler<AsyncResult<Void>> completionHandler) {
        this.db.getConnection().query(" select 'chanchito' ").execute(r -> {
            if (r.failed()) {
                completionHandler.handle(Future.failedFuture(r.cause()));
                return;
            }

            completionHandler.handle(Future.succeededFuture());
        });
    }
}
