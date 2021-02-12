package yuki.framework.dataaccess;

import javax.inject.Inject;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class DbConfigurator {
    @Inject
    private Db db;
    
    public void init(final JsonObject configuration, final Vertx vertx) {
        this.db.init(configuration, vertx);
        try {
            this.preInitialize();
        } catch (final Throwable e) {
            throw new RuntimeException("Error trying to pre-initialize the connection", e);
        }
    }
    
    private void preInitialize() throws Throwable {
        final Promise<Void> promise = Promise.promise();
        
        this.db.getConnection().query(" select 'chanchito' ").execute(r -> {
            if (r.failed()) {
                promise.fail(r.cause());
                return;
            }
            
            promise.complete();
        });
        
        Future<Void> future = promise.future();
        
        while (!future.isComplete()) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        
        if (future.failed()) {
            throw future.cause();
        }
    }
}
