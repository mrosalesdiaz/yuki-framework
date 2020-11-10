package yuki.framework.dataaccess;

import io.vertx.core.MultiMap;

public interface QueryDef {

	<T> T execute(Class<T> returnType);

	MultiMap getHeaders();
}
