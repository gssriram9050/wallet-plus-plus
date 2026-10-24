package exception;

/** Thrown when an amount is not a valid number or is outside the allowed range. */
public class InvalidAmountException extends InvalidInputException {

    private static final long serialVersionUID = 1L;

    public InvalidAmountException(String message) {
        super(message);
    }
}
