package solutions.canarin.cream.soda.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreamSodaTest {

    @Test
    void classAsConfig() {
        assertThrows(CreamSodaException.class, () -> {
            CreamSoda.addConfig(String.class);
            CreamSoda.start();
        });
    }

    @Test
    void closed() {
        assertThrows(CreamSodaException.class, () -> {
            CreamSoda.addConfig(new Config());
            CreamSoda injector = CreamSoda.start();
            assertEquals("ChuckNorris", injector.instance(Obj.class).val);
            injector.close();
            injector.instance(Obj.class);
        });
    }

    public static class Config {
        @Provides
        public Obj obj() {
            return new Obj("ChuckNorris");
        }
    }

    private static class Obj {
        final String val;

        Obj(String val) {
            this.val = val;
        }
    }
}
