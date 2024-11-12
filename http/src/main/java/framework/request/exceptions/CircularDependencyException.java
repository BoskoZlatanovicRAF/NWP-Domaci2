package framework.request.exceptions;

/**
 * Thrown when a circular dependency is detected during injection
 */
public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
