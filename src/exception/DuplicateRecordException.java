package exception;

/** Thrown when a user (same phone/email) or a wallet (one per user) already exists. */
public class DuplicateRecordException extends WalletException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }
}
