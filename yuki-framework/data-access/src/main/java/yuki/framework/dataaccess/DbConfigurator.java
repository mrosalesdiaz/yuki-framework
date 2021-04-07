package yuki.framework.dataaccess;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * Database configuration object it configures the connections and also test the
 * singleton {@link Db}.
 *
 * @author mrosalesdiaz
 */
public class DbConfigurator {
    @Inject
    private Db db;

    /**
     * @param vertx Instance to use at time pool is created.
     *              Configurations must be passed with following
     *              properties in the Json object configuration:<br>
     *              <ul>
     *              <li>dbUser: database user name</li>
     *              <li>dbPassword: database user password</li>
     *              <li>jdbcUrl: url to connect the PostgresSQL database.
     *              ex:
     *              postgresql://localhost/db_levi?search_path=authentication</li>
     *              </ul>
     * @return Future completion for wait for pre-initialization execution.
     */
    public Future<Void> init(final Vertx vertx) {
        try {
            this.db.init(vertx.getOrCreateContext().config(), vertx);
            return this.preInitialize();
        } catch (final Throwable e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Test connection by executing a simple dummy query
     *
     * @return
     */
    private Future<Void> preInitialize() {
        Promise<Void> promise = Promise.promise();

        this.db.getConnection().query(" select 'chanchito' ").execute(r -> {
            if (r.failed()) {
                promise.fail(r.cause());
                return;
            }

            promise.complete();
        });

        return promise.future();
    }
}
