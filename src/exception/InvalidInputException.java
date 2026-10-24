package exception;

/** Thrown when text typed by the user is not in the expected format. */
public class InvalidInputException extends WalletException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
