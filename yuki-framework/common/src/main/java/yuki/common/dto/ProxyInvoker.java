package yuki.common.dto;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import io.vertx.core.json.JsonObject;

public class ProxyInvoker implements InvocationHandler {

    private final JsonObject object;

    ProxyInvoker(JsonObject object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getAnnotation(Set.class) != null) {
            Set annotationName = method.getAnnotation(Set.class);
            object.put(annotationName.value(), args[0]);
            return proxy;
        } else if (method.getAnnotation(Get.class) != null) {
            Get annotationName = method.getAnnotation(Get.class);
            return object.getValue(annotationName.value());
        }else if(method.getAnnotation(ToJson.class) != null){
            return object;
        }

        throw new NoSuchMethodException(String.format("The method: %s is not supported or not was annotated with @Set or @Set", method.getName()));
    }
}
