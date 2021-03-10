package yuki.framework.dataaccess.annotations;

public @interface Parameter {
    /**
     * @return The PostgresSQL data type
     */
    ParameterType value();
    int  length() default Integer.MIN_VALUE;
}
