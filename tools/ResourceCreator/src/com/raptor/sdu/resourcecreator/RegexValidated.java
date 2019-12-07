package com.raptor.sdu.resourcecreator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface RegexValidated {
	String value();
	boolean global() default true;
}
