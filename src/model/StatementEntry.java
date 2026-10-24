package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import util.Format;

/**
 * One line of a wallet statement. Every balance change creates exactly one entry,
 * and each entry remembers the balance after it (the "running balance").
 */
public class StatementEntry {

    /** Total character width of the statement table (fits the default 120-column CMD window). */
    public static final int TABLE_WIDTH = 113;

    private final int entryNo;
    private final LocalDateTime timestamp;
    private final String txnId;
    private final String type;
    private final String description;
    private final BigDecimal credit;
    private final BigDecimal debit;
    private final BigDecimal balanceAfter;

    public StatementEntry(int entryNo, String txnId, String type, String description,
                          BigDecimal credit, BigDecimal debit, BigDecimal balanceAfter) {
        this.entryNo = entryNo;
        this.timestamp = LocalDateTime.now();
        this.txnId = txnId;
        this.type = type;
        this.description = description;
        this.credit = credit;
        this.debit = debit;
        this.balanceAfter = balanceAfter;
    }

    public int getEntryNo() { return entryNo; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getTxnId() { return txnId; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public BigDecimal getCredit() { return credit; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }

    /** Column headings for the statement table. */
    public static String header() {
        return String.format("%-3s %-17s %-8s %-12s %-32s %11s %11s %12s",
                "No", "Date & Time", "Txn ID", "Type", "Description", "Credit", "Debit", "Balance");
    }

    /** This entry formatted as one table row (zero amounts are shown as "-"). */
    public String toRow() {
        return String.format("%-3d %-17s %-8s %-12s %-32s %11s %11s %12s",
                entryNo,
                Format.shortDateTime(timestamp),
                txnId,
                type,
                Format.truncate(description, 32),
                showAmount(credit),
                showAmount(debit),
                Format.number(balanceAfter));
    }

    private static String showAmount(BigDecimal value) { return value.signum() == 0 ? "-" : Format.number(value); }
}
