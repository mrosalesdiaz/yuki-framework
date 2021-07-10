package yuki.framework.dataaccess.common;

public class DbConfigException extends RuntimeException {

  public DbConfigException(String message) {
    super(message);
  }

  public DbConfigException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
