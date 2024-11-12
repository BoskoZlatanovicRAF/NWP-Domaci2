package framework.request.exceptions;

/**
 * Thrown when @Autowired is used on a field that isn't a valid injectable type
 * (not a @Bean, @Service, @Component, or interface)
 */
public class InvalidAutowiredTargetException extends RuntimeException {
    public InvalidAutowiredTargetException(String message) {
        super(message);
    }
}
