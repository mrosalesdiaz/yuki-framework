package yuki.common.dto;

import java.lang.reflect.Proxy;

import io.vertx.core.json.JsonObject;

public interface JsonDto {
    static <T> T cast(JsonObject object, Class<? extends JsonDto> testDtoClass) {
        if (object == null) {
            return null;
        }
        return (T)testDtoClass.cast(Proxy.newProxyInstance(testDtoClass.getClassLoader(),
                new Class<?>[]{testDtoClass}, new ProxyInvoker(object)));
    }
    @ToJson
    JsonObject getJsonObject();
}
