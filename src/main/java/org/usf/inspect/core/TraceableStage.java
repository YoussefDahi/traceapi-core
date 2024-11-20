package org.usf.inspect.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author u$f
 *
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceableStage { 

	String value() default ""; // stage name
	
	/**
	 * require default constructor
	 * 
	 */
	Class<? extends StageUpdater> sessionUpdater() default StageUpdater.class;
	
	//boolean enabled() default true
}
