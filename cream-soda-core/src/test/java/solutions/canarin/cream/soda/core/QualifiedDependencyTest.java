package solutions.canarin.cream.soda.core;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualifiedDependencyTest {
    @Test
    void qualifiedInstances() {
        try (CreamSoda injector = CreamSoda.start(new Module())) {
            assertEquals(FooA.class, injector.instance(Key.of(Foo.class, A.class)).getClass());
            assertEquals(FooB.class, injector.instance(Key.of(Foo.class, B.class)).getClass());
        }
    }

    @Test
    void injectedQualified() {
        try (CreamSoda injector = CreamSoda.start(new Module())) {
            Dummy dummy = injector.instance(Dummy.class);
            assertEquals(FooB.class, dummy.foo.getClass());
        }
    }

    interface Foo {

    }

    public static class FooA implements Foo {

    }

    public static class FooB implements Foo {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface A {

    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface B {

    }

    public static class Module {
        @Provides
        @A
        Foo a(FooA fooA) {
            return fooA;
        }

        @Provides
        @B
        Foo b(FooB fooB) {
            return fooB;
        }
    }

    public static class Dummy {
        private final Foo foo;

        @Inject
        public Dummy(@B Foo foo) {
            this.foo = foo;
        }
    }

}
