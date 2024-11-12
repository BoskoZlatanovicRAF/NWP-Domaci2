package framework.request.exceptions;

/**
 * Thrown when a requested dependency cannot be found in the container
 */
public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(String message) {
        super(message);
    }
}
