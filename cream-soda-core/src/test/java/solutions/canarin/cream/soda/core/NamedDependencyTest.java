package solutions.canarin.cream.soda.core;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamedDependencyTest {
    @Test
    void namedInstanceWithModule() {
        try (CreamSoda injector = CreamSoda.start(new HelloWorldModule())) {
            assertEquals("Hello!", injector.instance(Key.of(String.class, "hello")));
            assertEquals("Hi!", injector.instance(Key.of(String.class, "hi")));
            assertEquals("Hello!", injector.instance(Bean.class).s);
        }
    }

    @Test
    void failingName() {
        assertThrows(CreamSodaException.class, () -> {
            try (CreamSoda injector = CreamSoda.start(new HelloWorldModule())) {
                injector.instance(Key.of(String.class, "ChuckNorris"));
            }
        });
    }

    public static class HelloWorldModule {
        @Provides
        @Named("hello")
        String hello() {
            return "Hello!";
        }

        @Provides
        @Named("hi")
        String hi() {
            return "Hi!";
        }
    }

    public static class Bean {
        private final String s;

        @Inject
        public Bean(@Named("hello") String s) {
            this.s = s;
        }
    }
}
