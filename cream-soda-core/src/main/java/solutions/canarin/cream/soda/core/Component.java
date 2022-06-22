package solutions.canarin.cream.soda.core;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration instances can be annotated with classes to tell the injector it should scan those classes.
 *
 * @author Jakša Tomović
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * Classes to scan and add to the injector
     */
    Class<?>[] value() default {};
}
