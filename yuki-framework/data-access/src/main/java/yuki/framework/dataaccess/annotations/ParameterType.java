package yuki.framework.dataaccess.annotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum ParameterType {
  STRING(String.class, "varchar","text","character varying"),

  BOOLEAN(Boolean.class, "bool"),

  INTEGER(Integer.class, "int"),
  DOUBLE(Double.class, "double"),

  BYTEA(Byte[].class, "bytea"),

  DATE(LocalDate.class, "date"),
  DATETIME(LocalDateTime.class, "timestamp"),
  TIME(LocalTime.class, "time");

  private final String[] dbType;
  private final Class<?> javaType;

  ParameterType(Class<?> javaType, String... dbType) {
    this.dbType = dbType;
    this.javaType = javaType;
  }


  public String[] dbType() {
    return dbType;
  }

  public Class<?> javaType() {
    return javaType;
  }
}
