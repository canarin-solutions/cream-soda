package solutions.canarin.cream.soda.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleOverrideTest {
    @Test
    void dependencyOverridenByModule() {
        try (CreamSoda injector = CreamSoda.start(new PlainStubOverrideModule())) {
            assertEquals(PlainStub.class, injector.instance(Plain.class).getClass());
        }
    }

    @Test
    void moduleOverwrittenBySubClass() {
        assertEquals("foo", CreamSoda.start(new FooModule()).instance(String.class));
        assertEquals("bar", CreamSoda.start(new FooOverrideModule()).instance(String.class));
    }

    public static class Plain {
    }

    public static class PlainStub extends Plain {

    }

    public static class PlainStubOverrideModule {
        @Provides
        public Plain plain(PlainStub plainStub) {
            return plainStub;
        }

    }

    public static class FooModule {
        @Provides
        String foo() {
            return "foo";
        }
    }

    public static class FooOverrideModule extends FooModule {
        @Provides
        @Override
        String foo() {
            return "bar";
        }
    }

}
