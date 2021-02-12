package yuki.framework.dataaccess;

import com.google.inject.Singleton;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

@Singleton
public class Db {
    private PgPool client;
    
    void init(final JsonObject configuration, Vertx vertx) {
        
        PgConnectOptions connectOptions = PgConnectOptions.fromUri(configuration.getString("jdbcUrl"))
                .setUser(configuration.getString("dbUser")).setPassword(configuration.getString("dbPassword"));
        
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        this.client = PgPool.pool( connectOptions, poolOptions);
    }
    
    public PgPool getConnection() {
        return this.client;
    }
}
