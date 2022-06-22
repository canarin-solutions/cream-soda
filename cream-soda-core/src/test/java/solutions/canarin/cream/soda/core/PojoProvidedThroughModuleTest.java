package solutions.canarin.cream.soda.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PojoProvidedThroughModuleTest {
    @Test
    void pojoNotProvided() {
        assertThrows(CreamSodaException.class, () -> {
            try (CreamSoda injector = CreamSoda.start()) {
                injector.instance(Pojo.class);
            }
        });
    }

    @Test
    void pojoProvided() {
        try (CreamSoda injector = CreamSoda.start(new Module())) {
            assertNotNull(injector.instance(Pojo.class));
        }
    }

    @Test
    void dependencyInjected() {
        try (CreamSoda injector = CreamSoda.start(new Module())) {
            assertEquals("foo", injector.instance(String.class));
        }
    }

    public static class Module {
        @Provides
        Pojo pojo() {
            return new Pojo("foo");
        }

        @Provides
        String myString(Pojo pojo) {
            return pojo.foo;
        }
    }

    public static class Pojo {
        private final String foo;

        public Pojo(String foo) {
            this.foo = foo;
        }
    }
}
