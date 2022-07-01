package solutions.canarin.cream.soda.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

class AutoProviderTest {
    private static Consumer<String> HIT;

    @BeforeEach
    void setUp() {
        HIT = Mockito.mock(Consumer.class);
    }

    @Test
    void autoCreatedAnnotatedClass() {
        try (CreamSoda injector = CreamSoda.start(new Config())) {
            verify(HIT).accept("hit");
        }
    }

    @Test
    void autoCreatedAnnotatedConfig() {
        try (CreamSoda injector = CreamSoda.start(new ConfigComponent())) {
            verify(HIT).accept("hit");
        }
    }

    class Config {

        @Provides
        public AnnotatedClass sing() {
            return new AnnotatedClass();
        }
    }

    @Components({AnnotatedClass.class})
    class ConfigComponent {

    }

    @Auto
    public static class AnnotatedClass {
        AnnotatedClass() {
            HIT.accept("hit");
        }
    }
}