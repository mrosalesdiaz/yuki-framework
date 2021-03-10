package yuki.framework.dataaccess.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.vertx.core.json.JsonArray;

/**
 * Annotation for database function execution.
 *
 * @author mrosalesdiaz
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryDefinitionMetadata {

	/**
	 * @return executing query example: " SELECT * FROM fn_create_lobby( name:= $1 )
	 */
	String sql();

}
