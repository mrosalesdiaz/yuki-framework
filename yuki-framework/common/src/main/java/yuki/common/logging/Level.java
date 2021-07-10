package yuki.common.logging;

public interface Level {

  java.util.logging.Level CONFIG = java.util.logging.Level.CONFIG;
  java.util.logging.Level INFO = java.util.logging.Level.INFO;
  java.util.logging.Level DEBUG = java.util.logging.Level.FINER;
  java.util.logging.Level TRACE = java.util.logging.Level.FINEST;
  java.util.logging.Level DEPLOY = java.util.logging.Level.CONFIG;
  java.util.logging.Level ERROR = java.util.logging.Level.SEVERE;

}
