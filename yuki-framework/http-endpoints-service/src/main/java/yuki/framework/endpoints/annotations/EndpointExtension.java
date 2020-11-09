package yuki.framework.endpoints.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import yuki.framework.enpoints.YukiEndpoint;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EndpointExtension {

	Class<? extends YukiEndpoint> value();

}
