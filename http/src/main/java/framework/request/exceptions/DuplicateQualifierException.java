package framework.request.exceptions;

/**
 * Thrown when multiple implementations are registered with the same qualifier
 */
public class DuplicateQualifierException extends RuntimeException {
    public DuplicateQualifierException(String message) {
        super(message);
    }
}