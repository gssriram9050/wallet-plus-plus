package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.Format;

/** Money returned to a wallet against an earlier Payment (the refund keeps a link to that payment). */
public class Refund extends Transaction {

    private final Payment payment;
    private BigDecimal balanceAfter;

    public Refund(String txnId, Payment payment) {
        super(txnId, payment.getAmount(), NO_FEE, TransactionStatus.SUCCESS);
        this.payment = payment;
    }

    public Payment getPayment() { return payment; }
    public Wallet getWallet() { return payment.getWallet(); }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    @Override
    public String getType() { return "REFUND"; }

    @Override
    protected List<String> getDetailLines() {
        List<String> lines = new ArrayList<>();
        lines.add(Format.row("Wallet", payment.getWallet()));
        lines.add(Format.row("Original Payment", payment.getTxnId()));
        lines.add(Format.row("Merchant", payment.getMerchant()));
        lines.add(Format.row("Refund Amount", Format.money(getAmount())));
        lines.add(Format.row("Fee", Format.money(getFee())));
        lines.add(Format.row("New Balance", balanceAfter == null ? "-" : Format.money(balanceAfter)));
        return lines;
    }
}
