package yuki.framework.verticle.common;

import java.util.Map;

public class RoutesRegisterException extends RuntimeException {
    private final Throwable[] internalCauses;

    public RoutesRegisterException(String message, Map<String, Throwable> exceptionList) {
        super(message);
        internalCauses = exceptionList.values().toArray(new Throwable[0]);
        message = String.format("%s\n%s", exceptionList.entrySet().stream()
                .map(e -> String.format("\t%s -> %s\n", e.getKey(), e.getValue().getMessage())));

    }
}
