package yuki.framework.dataaccess.dto;

import io.vertx.core.json.JsonObject;

public class DbConfig {

  private JsonObject config = new JsonObject();

  public static DbConfig from(JsonObject config) {
    if (config == null) {
      return new DbConfig();
    }

    DbConfig newInstance = new DbConfig();

    newInstance.config = config.copy();

    return newInstance;
  }


  public String getJdbcUrl() {
    return config.getString("jdbcUrl","");
  }

  public String getUser() {
    return config.getString("dbUser","");
  }

  public String getPassword() {
    return config.getString("dbPassword","");
  }

  public int getMaxSize() {
    return config.getInteger("poolMaxSize", 4);
  }

  public int getMaxWaitQueueSize() {
    return config.getInteger("poolMaxWaitQueueSize", -1);
  }
}
