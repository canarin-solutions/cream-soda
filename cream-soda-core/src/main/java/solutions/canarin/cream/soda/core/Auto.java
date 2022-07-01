package solutions.canarin.cream.soda.core;

import jakarta.inject.Singleton;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Configure the injector to automatically create the singleton instance
 *
 * @author Jakša Tomović
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE, CONSTRUCTOR})
@Singleton
public @interface Auto {

}
