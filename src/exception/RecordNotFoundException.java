package exception;

/** Thrown when a user, wallet or payment ID does not exist. */
public class RecordNotFoundException extends WalletException {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(String message) {
        super(message);
    }
}
