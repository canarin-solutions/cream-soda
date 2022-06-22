package solutions.canarin.cream.soda.core;

import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyTest {
    @Test
    void dependencyInstance() {
        try (CreamSoda injector = CreamSoda.start()) {
            assertNotNull(injector.instance(Plain.class));
        }
    }

    @Test
    void provider() {
        try (CreamSoda injector = CreamSoda.start()) {
            Provider<Plain> plainProvider = injector.provider(Plain.class);
            assertNotNull(plainProvider.get());
        }
    }

    @Test
    void unknown() {
        assertThrows(CreamSodaException.class, () -> {
            try (CreamSoda injector = CreamSoda.start()) {
                injector.instance(Unknown.class);
            }
        });
    }

    public static class Plain {

    }

    public static class Unknown {
        public Unknown(String noSuitableConstructor) {

        }
    }
}
