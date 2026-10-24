package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import util.Format;

/**
 * Parent (abstract) class of TopUp, Transfer, Payment and Refund.
 * It holds what every transaction has in common and prints the receipt layout.
 * Each child class overrides getType() and getDetailLines() (polymorphism), so
 * calling getReceipt() on any Transaction gives the right receipt for that type.
 */
public abstract class Transaction {

    protected static final BigDecimal NO_FEE = new BigDecimal("0.00");
    private static final int RECEIPT_WIDTH = 56;

    private final String txnId;
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final LocalDateTime timestamp;
    private TransactionStatus status;

    protected Transaction(String txnId, BigDecimal amount, BigDecimal fee, TransactionStatus status) {
        this.txnId = txnId;
        this.amount = amount;
        this.fee = fee;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getTxnId() { return txnId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFee() { return fee; }

    /** Amount plus fee. */
    public BigDecimal getTotalAmount() { return amount.add(fee); }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    /** Short name such as "TOP-UP" or "TRANSFER". Every child class gives its own. */
    public abstract String getType();

    /** The receipt lines that are specific to this kind of transaction. */
    protected abstract List<String> getDetailLines();

    /** Builds the complete receipt text. */
    public String getReceipt() {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(Format.line('=', RECEIPT_WIDTH)).append(nl);
        sb.append(Format.center(getType() + " RECEIPT", RECEIPT_WIDTH)).append(nl);
        sb.append(Format.line('=', RECEIPT_WIDTH)).append(nl);
        sb.append(Format.row("Transaction ID", txnId)).append(nl);
        sb.append(Format.row("Type", getType())).append(nl);
        sb.append(Format.row("Date & Time", Format.dateTime(timestamp))).append(nl);
        sb.append(Format.row("Status", status)).append(nl);
        sb.append(Format.line('-', RECEIPT_WIDTH)).append(nl);
        for (String line : getDetailLines()) {
            sb.append(line).append(nl);
        }
        sb.append(Format.line('-', RECEIPT_WIDTH)).append(nl);
        sb.append(Format.center("Thank you for using Wallet++", RECEIPT_WIDTH)).append(nl);
        sb.append(Format.line('=', RECEIPT_WIDTH));
        return sb.toString();
    }
}
