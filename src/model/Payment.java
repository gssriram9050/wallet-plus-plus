package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.Format;

/** A payment from a wallet to a merchant. It can be refunded once. */
public class Payment extends Transaction {

    private final Wallet wallet;
    private final String merchant;
    private boolean refunded;
    private BigDecimal balanceAfter;

    public Payment(String txnId, Wallet wallet, String merchant, BigDecimal amount) {
        super(txnId, amount, NO_FEE, TransactionStatus.SUCCESS);
        this.wallet = wallet;
        this.merchant = merchant;
    }

    public Wallet getWallet() { return wallet; }
    public String getMerchant() { return merchant; }
    public boolean isRefunded() { return refunded; }
    public void markRefunded() { this.refunded = true; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    @Override
    public String getType() { return "PAYMENT"; }

    @Override
    protected List<String> getDetailLines() {
        List<String> lines = new ArrayList<>();
        lines.add(Format.row("Wallet", wallet));
        lines.add(Format.row("Merchant", merchant));
        lines.add(Format.row("Amount Paid", Format.money(getAmount())));
        lines.add(Format.row("Fee", Format.money(getFee())));
        lines.add(Format.row("New Balance", balanceAfter == null ? "-" : Format.money(balanceAfter)));
        return lines;
    }
}
