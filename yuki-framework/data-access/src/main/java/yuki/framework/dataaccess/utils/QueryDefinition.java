package yuki.framework.dataaccess.utils;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;

public interface QueryDefinition {

	Future<JsonArray> execute();

	MultiMap getHeaders();
}
