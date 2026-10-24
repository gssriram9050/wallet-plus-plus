package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.Format;

/** Moving money from one wallet to another. The sender pays the amount plus any fee. */
public class Transfer extends Transaction {

    private final Wallet source;
    private final Wallet destination;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal destinationBalanceAfter;

    public Transfer(String txnId, Wallet source, Wallet destination, BigDecimal amount, BigDecimal fee) {
        super(txnId, amount, fee, TransactionStatus.SUCCESS);
        this.source = source;
        this.destination = destination;
    }

    public Wallet getSource() { return source; }
    public Wallet getDestination() { return destination; }
    public BigDecimal getSourceBalanceAfter() { return sourceBalanceAfter; }
    public BigDecimal getDestinationBalanceAfter() { return destinationBalanceAfter; }

    public void setBalancesAfter(BigDecimal sourceBalanceAfter, BigDecimal destinationBalanceAfter) {
        this.sourceBalanceAfter = sourceBalanceAfter;
        this.destinationBalanceAfter = destinationBalanceAfter;
    }

    @Override
    public String getType() { return "TRANSFER"; }

    @Override
    protected List<String> getDetailLines() {
        List<String> lines = new ArrayList<>();
        lines.add(Format.row("From Wallet", source));
        lines.add(Format.row("To Wallet", destination));
        lines.add(Format.row("Amount", Format.money(getAmount())));
        lines.add(Format.row("Fee", Format.money(getFee())));
        lines.add(Format.row("Total Debited", Format.money(getTotalAmount())));
        lines.add(Format.row("Sender Balance", sourceBalanceAfter == null ? "-" : Format.money(sourceBalanceAfter)));
        lines.add(Format.row("Receiver Balance",
                destinationBalanceAfter == null ? "-" : Format.money(destinationBalanceAfter)));
        return lines;
    }
}
