package yuki.common.stream;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.stream.Collector;

public class JsonCollector {

  public static Collector<JsonObject, JsonArray, JsonArray> toJsonArray() {
    return Collector.of(
        JsonArray::new,
        (jsonArray, jsonObject) -> jsonArray.add(jsonObject),
        (jsonArray1, jsonArray2) -> {
          throw new RuntimeException(
              String.format("Function not implemented in class %s", JsonCollector.class.getName()));
        }
    );
  }

}

