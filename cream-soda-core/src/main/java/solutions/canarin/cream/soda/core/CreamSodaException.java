package solutions.canarin.cream.soda.core;

public class CreamSodaException extends RuntimeException {
    CreamSodaException(String message) {
        super(message);
    }

    CreamSodaException(String message, Throwable cause) {
        super(message, cause);
    }
}
