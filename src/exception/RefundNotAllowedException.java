package exception;

/** Thrown when a refund is requested for a payment that is not eligible. */
public class RefundNotAllowedException extends WalletException {

    private static final long serialVersionUID = 1L;

    public RefundNotAllowedException(String message) {
        super(message);
    }
}
