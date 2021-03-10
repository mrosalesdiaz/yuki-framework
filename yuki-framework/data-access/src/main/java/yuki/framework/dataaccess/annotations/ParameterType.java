package yuki.framework.dataaccess.annotations;

public enum ParameterType {
    BOOLEAN("boolean"),
    NUMERIC("numeric"),
    BYTEA("bytea"),
    DATE("date"),
    INTEGER("integer"),
    VARCHAR("varchar"),
    TEXT("text"),
    TIMESTAMP_WITHOUT_TIME_ZONE("timestamp without time zone"),
    TIME("time");

    private final String dbType;

    ParameterType(String dbType) {
        this.dbType = dbType;
    }
}
