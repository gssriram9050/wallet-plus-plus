package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exception.InsufficientFundsException;
import util.Format;

/**
 * A wallet holds a balance and its statement (composition: the statement entries
 * belong to the wallet). The balance can only change through credit() and debit(),
 * and both of them always add a StatementEntry, so no transaction can be missed.
 */
public class Wallet {

    private final String walletId;
    private final User owner;
    private BigDecimal balance = new BigDecimal("0.00");
    private final List<StatementEntry> statement = new ArrayList<>();

    public Wallet(String walletId, User owner) {
        this.walletId = walletId;
        this.owner = owner;
    }

    public String getWalletId() { return walletId; }
    public User getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }

    /** Read-only view, so other classes cannot edit the statement. */
    public List<StatementEntry> getStatement() { return Collections.unmodifiableList(statement); }
    public boolean hasSufficientBalance(BigDecimal amount) { return balance.compareTo(amount) >= 0; }

    /** Adds money to the wallet and records a statement entry. */
    public StatementEntry credit(String txnId, String type, String description, BigDecimal amount) {
        balance = balance.add(amount);
        return addEntry(txnId, type, description, amount, new BigDecimal("0.00"));
    }

    /** Takes money out of the wallet (if the balance allows) and records a statement entry. */
    public StatementEntry debit(String txnId, String type, String description, BigDecimal amount)
            throws InsufficientFundsException {
        if (!hasSufficientBalance(amount)) {
            throw new InsufficientFundsException("Insufficient funds in wallet " + walletId
                    + ". Available: " + Format.money(balance) + ", required: " + Format.money(amount) + ".",
                    balance, amount);
        }
        balance = balance.subtract(amount);
        return addEntry(txnId, type, description, new BigDecimal("0.00"), amount);
    }

    private StatementEntry addEntry(String txnId, String type, String description,
                                    BigDecimal credit, BigDecimal debit) {
        StatementEntry entry = new StatementEntry(statement.size() + 1, txnId, type, description,
                credit, debit, balance);
        statement.add(entry);
        return entry;
    }

    @Override
    public String toString() { return walletId + " (" + owner.getName() + ")"; }
}
