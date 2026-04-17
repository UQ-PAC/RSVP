package uq.pac.childclinic.cedar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CedarAuthorizations.class)
public @interface CedarAuthorization {

	String action();

	String resourceType();

	String resourceId() default "";

	boolean validate() default true;

}
