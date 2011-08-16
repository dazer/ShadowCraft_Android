package core;

/**
 * Base class for all our exceptions. All exceptions we generate should either
 * use or subclass this. These exceptions are unchecked and don't propagate.
 */
@SuppressWarnings("serial")
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
