package yuki.framework.verticle;

import com.google.inject.Injector;

import java.util.List;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VerticleHelpers {
    public static final Logger LOGGER = LoggerFactory.getLogger(VerticleHelpers.class);

    public static Future<Void> registerConsumer(EventBus eventBus, String messageAddress, Class<? extends Handler<Message<JsonObject>>> handlerClass, Injector injector) {
        Promise<Void> promise = Promise.promise();

        eventBus
                .consumer(messageAddress, injector.getInstance(handlerClass))
                .exceptionHandler(err -> LOGGER.error("Generic error fount in eventBus consumer", err))
                .completionHandler(ar -> {
                    if (ar.failed()) {
                        promise.fail(ar.cause());
                        return;
                    }
                    promise.complete();
                });

        return promise.future();
    }

    public static Future<CompositeFuture> initializeMessageConsumers(Future<Void>... messageConsumers) {
        return CompositeFuture
                .all(List.of(messageConsumers));

    }
}
