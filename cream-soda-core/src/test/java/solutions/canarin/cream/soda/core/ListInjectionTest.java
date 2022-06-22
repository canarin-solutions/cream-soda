package solutions.canarin.cream.soda.core;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListInjectionTest {
    @Test
    void listOfInjected() {
        try (CreamSoda injector = CreamSoda.start(new Config())) {
            assertEquals(2, injector.instance(A.class).list.size());
        }
    }

    @Test
    void instancesOfType() {
        try (CreamSoda injector = CreamSoda.start(new Config())) {
            assertEquals(2, injector.instancesOfType(Base.class).size());
        }
    }

    @Test
    void invalidType() {
        assertThrows(CreamSodaException.class, () -> {
            try (CreamSoda injector = CreamSoda.start(new Config())) {
                injector.instance(B.class).list.size();
            }
        });
    }

    @Test
    void qualifiedListOfInjected() {
        try (CreamSoda injector = CreamSoda.start(new Config())) {
            assertEquals(4, injector.instance(Key.of(List.class, "aList")).size());
        }
    }

    public static class A {
        private final List<Base> list;

        @Inject
        public A(List<Base> list) {
            this.list = list;
        }
    }

    public static class B {
        private final List<? extends Base> list;

        @Inject
        public B(List<? extends Base> list) {
            this.list = list;
        }
    }

    public static class Config {

        @Provides
        Sub1 sub1() {
            return new Sub1();
        }

        @Provides
        Sub2 sub2() {
            return new Sub2();
        }

        @Provides
        @Named("aList")
        List<String> qualifiedList() {
            return Arrays.asList("a", "b", "c", "d");
        }
    }

    public static class Base {

    }

    public static class Sub1 extends Base {

    }

    public static class Sub2 extends Base {

    }
}
