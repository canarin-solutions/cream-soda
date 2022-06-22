package solutions.canarin.cream.soda.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Singleton;

/**
 * Configure the injector to automatically create the singleton instance
 *
 * @author Jakša Tomović
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Singleton
public @interface Autowired {

}
