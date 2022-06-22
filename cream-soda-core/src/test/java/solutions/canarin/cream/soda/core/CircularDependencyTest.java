package solutions.canarin.cream.soda.core;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CircularDependencyTest {

    @Test
    void circularDependencyCaught() {
        assertThrows(CreamSodaException.class, () -> {
            try (CreamSoda injector = CreamSoda.start()) {
                injector.instance(Circle1.class);
            }
        });

    }

    @Test
    void circularDependencyWithProviderAllowed() {
        try (CreamSoda injector = CreamSoda.start()) {
            CircleWithProvider1 circle1 = injector.instance(CircleWithProvider1.class);
            assertNotNull(circle1.circleWithProvider2.circleWithProvider1.get());
        }
    }

    public static class Circle1 {
        private final Circle2 circle2;

        @Inject
        public Circle1(Circle2 circle2) {
            this.circle2 = circle2;
        }
    }

    public static class Circle2 {
        private final Circle1 circle1;

        @Inject
        public Circle2(Circle1 circle1) {
            this.circle1 = circle1;
        }
    }

    public static class CircleWithProvider1 {
        private final CircleWithProvider2 circleWithProvider2;

        @Inject
        public CircleWithProvider1(CircleWithProvider2 circleWithProvider2) {
            this.circleWithProvider2 = circleWithProvider2;
        }
    }

    public static class CircleWithProvider2 {
        private final Provider<CircleWithProvider1> circleWithProvider1;

        @Inject
        public CircleWithProvider2(Provider<CircleWithProvider1> circleWithProvider1) {
            this.circleWithProvider1 = circleWithProvider1;
        }
    }
}
