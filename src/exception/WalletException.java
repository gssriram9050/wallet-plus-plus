package exception;

/**
 * Base class for every business-rule or validation error in Wallet++.
 * All other custom exceptions extend this class, so one catch block in
 * the menu loop can handle them all.
 */
public class WalletException extends Exception {

    private static final long serialVersionUID = 1L;

    public WalletException(String message) {
        super(message);
    }
}
