package solutions.canarin.cream.soda.core;

import jakarta.inject.Scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies a type that the injector instantiates every time the type is requested.
 *
 * @see jakarta.inject.Scope @Scope
 */
@Scope
@Documented
@Retention(RUNTIME)
public @interface Prototype {

}
