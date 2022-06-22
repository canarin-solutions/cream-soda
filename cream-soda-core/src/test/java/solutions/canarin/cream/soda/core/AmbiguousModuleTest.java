package solutions.canarin.cream.soda.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AmbiguousModuleTest {

    @Test
    void ambiguousModule() {
        assertThrows(CreamSodaException.class, () -> CreamSoda.start(new Module()));
    }

    public static class Module {
        @Provides
        String foo() {
            return "foo";
        }

        @Provides
        String bar() {
            return "bar";
        }
    }
}
