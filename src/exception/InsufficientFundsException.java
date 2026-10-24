package exception;

import java.math.BigDecimal;

/** Thrown when a wallet does not have enough balance for a debit. */
public class InsufficientFundsException extends WalletException {

    private static final long serialVersionUID = 1L;

    private final BigDecimal available;
    private final BigDecimal required;

    public InsufficientFundsException(String message, BigDecimal available, BigDecimal required) {
        super(message);
        this.available = available;
        this.required = required;
    }

    public BigDecimal getAvailable() { return available; }
    public BigDecimal getRequired() { return required; }
}
