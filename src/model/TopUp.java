package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.Format;

/** Adding money to a wallet. A top-up starts as PENDING and only changes the balance once confirmed. */
public class TopUp extends Transaction {

    private final Wallet wallet;
    private final String source; // UPI / Debit Card / Net Banking
    private BigDecimal balanceAfter;

    public TopUp(String txnId, Wallet wallet, BigDecimal amount, String source) {
        super(txnId, amount, NO_FEE, TransactionStatus.PENDING);
        this.wallet = wallet;
        this.source = source;
    }

    public Wallet getWallet() { return wallet; }
    public String getSource() { return source; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    @Override
    public String getType() { return "TOP-UP"; }

    @Override
    protected List<String> getDetailLines() {
        List<String> lines = new ArrayList<>();
        lines.add(Format.row("Wallet", wallet));
        lines.add(Format.row("Payment Source", source));
        lines.add(Format.row("Amount Added", Format.money(getAmount())));
        lines.add(Format.row("Fee", Format.money(getFee())));
        lines.add(Format.row("New Balance", balanceAfter == null ? "-" : Format.money(balanceAfter)));
        return lines;
    }
}
