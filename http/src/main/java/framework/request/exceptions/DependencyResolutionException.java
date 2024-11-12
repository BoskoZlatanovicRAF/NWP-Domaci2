package framework.request.exceptions;

/**
 * Thrown when there's an error during the dependency resolution process
 */
public class DependencyResolutionException extends RuntimeException {
    public DependencyResolutionException(String message) {
        super(message);
    }

    public DependencyResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}