package yuki.framework.verticle.common;

import io.vertx.core.Handler;

public class ExceptionUtil {
    public static void catchException(ExecutionBlock executionBlock, Handler<Throwable> throwableHandler) {
        try {
            executionBlock.apply();
        } catch (Throwable e) {
            throwableHandler.handle(e);
        }
    }
}
