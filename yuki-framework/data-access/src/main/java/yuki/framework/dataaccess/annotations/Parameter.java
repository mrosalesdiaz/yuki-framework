package yuki.framework.dataaccess.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    /**
     * @return The PostgresSQL data type
     */
    ParameterType value();
    int  length() default Integer.MIN_VALUE;
}
