package solutions.canarin.cream.soda.core;

import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SingletonTest {
    @Test
    void nonSingleton() {
        try (CreamSoda injector = CreamSoda.start()) {
            assertNotEquals(injector.instance(ProtoPlain.class), injector.instance(ProtoPlain.class));
        }
    }

    @Test
    void nonSingletonConfig() {
        try (CreamSoda injector = CreamSoda.start(new Config())) {
            assertNotEquals(injector.instance(Plain.class), injector.instance(Plain.class));
        }
    }

    @Test
    void singletonByDefault() {
        try (CreamSoda injector = CreamSoda.start()) {
            assertEquals(injector.instance(Plain.class), injector.instance(Plain.class));
        }
    }

    @Test
    void singletonThroughProvider() {
        try (CreamSoda injector = CreamSoda.start()) {
            Provider<Plain> provider = injector.provider(Plain.class);
            assertEquals(provider.get(), provider.get());
        }
    }

    @Prototype
    public static class ProtoPlain {

    }

    public static class Plain {

    }

    public class Config {
        @Provides
        @Prototype
        Plain plain() {
            return new Plain();
        }
    }
}
