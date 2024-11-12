package framework.request.exceptions;

/**
 * Thrown when @Autowired is used on an interface type without @Qualifier
 */
public class MissingQualifierException extends RuntimeException {
    public MissingQualifierException(String message) {
        super(message);
    }
}
