package test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import exception.DuplicateRecordException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import exception.InvalidInputException;
import exception.RecordNotFoundException;
import exception.RefundNotAllowedException;
import exception.WalletException;
import model.Payment;
import model.Refund;
import model.StatementEntry;
import model.TopUp;
import model.Transaction;
import model.TransactionStatus;
import model.Transfer;
import model.User;
import model.Wallet;
import service.WalletService;

/** Tests for the business rules in WalletService. */
public class WalletServiceTests {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    /** Creates a fresh service with two users (U101, U102) and wallets (W1001, W1002). */
    private static WalletService newServiceWithTwoWallets() throws WalletException {
        WalletService service = new WalletService();
        service.addUser("Asha Raman", "9876543210", "asha@example.com");
        service.addUser("Ravi Kumar", "9123456780", "ravi@example.com");
        service.createWallet("U101");
        service.createWallet("U102");
        return service;
    }

    /** Puts money into a wallet using the normal two-step top-up. */
    private static void fund(WalletService service, String walletId, String amount) throws WalletException {
        TopUp topUp = service.requestTopUp(walletId, new BigDecimal(amount), "UPI");
        service.confirmTopUp(topUp);
    }

    public static void run() {
        try {
            usersAndWallets();
            topUps();
            transfers();
            payments();
            refunds();
            statements();
            polymorphism();
        } catch (WalletException e) {
            SimpleTest.check("Unexpected exception in test setup: " + e.getMessage(), false);
        }
    }

    private static void usersAndWallets() throws WalletException {
        SimpleTest.section("Users and wallets");
        WalletService service = new WalletService();

        User asha = service.addUser("Asha Raman", "9876543210", "asha@example.com");
        SimpleTest.checkEquals("First user gets ID U101", "U101", asha.getUserId());
        SimpleTest.checkEquals("Second user gets ID U102", "U102",
                service.addUser("Ravi Kumar", "9123456780", "ravi@example.com").getUserId());
        SimpleTest.check("New user has no wallet yet", !asha.hasWallet());

        SimpleTest.checkThrows("Duplicate phone number is rejected", DuplicateRecordException.class,
                () -> service.addUser("Someone Else", "9876543210", "other@example.com"));
        SimpleTest.checkThrows("Duplicate email is rejected", DuplicateRecordException.class,
                () -> service.addUser("Someone Else", "9000000001", "ASHA@example.com"));
        SimpleTest.checkThrows("Invalid phone is rejected by the service", InvalidInputException.class,
                () -> service.addUser("Someone Else", "123", "x@example.com"));

        Wallet wallet = service.createWallet("U101");
        SimpleTest.checkEquals("First wallet gets ID W1001", "W1001", wallet.getWalletId());
        SimpleTest.checkMoney("New wallet starts with balance 0.00", "0.00", wallet.getBalance());
        SimpleTest.check("User is linked to the wallet (aggregation)", asha.getWallet() == wallet);
        SimpleTest.check("Wallet is linked to its owner", wallet.getOwner() == asha);
        SimpleTest.checkThrows("Second wallet for the same user is rejected", DuplicateRecordException.class,
                () -> service.createWallet("U101"));
        SimpleTest.checkThrows("Wallet for unknown user is rejected", RecordNotFoundException.class,
                () -> service.createWallet("U999"));
    }

    private static void topUps() throws WalletException {
        SimpleTest.section("Top-up (needs confirmation)");
        WalletService service = newServiceWithTwoWallets();
        Wallet wallet = service.getWallet("W1001");

        TopUp topUp = service.requestTopUp("W1001", new BigDecimal("1000"), "UPI");
        SimpleTest.checkEquals("Requested top-up is PENDING", TransactionStatus.PENDING, topUp.getStatus());
        SimpleTest.checkMoney("Balance is unchanged before confirmation", "0.00", wallet.getBalance());
        SimpleTest.checkEquals("No statement entry before confirmation", 0, wallet.getStatement().size());

        service.confirmTopUp(topUp);
        SimpleTest.checkEquals("Confirmed top-up is SUCCESS", TransactionStatus.SUCCESS, topUp.getStatus());
        SimpleTest.checkMoney("Balance increases after confirmation", "1000.00", wallet.getBalance());
        SimpleTest.checkEquals("Confirmation creates one statement entry", 1, wallet.getStatement().size());
        SimpleTest.checkMoney("Receipt shows resulting balance", "1000.00", topUp.getBalanceAfter());
        SimpleTest.checkThrows("Confirming twice is rejected", InvalidInputException.class,
                () -> service.confirmTopUp(topUp));

        TopUp cancelled = service.requestTopUp("W1001", new BigDecimal("500"), "Debit Card");
        service.cancelTopUp(cancelled);
        SimpleTest.checkEquals("Cancelled top-up is CANCELLED", TransactionStatus.CANCELLED, cancelled.getStatus());
        SimpleTest.checkMoney("Cancelled top-up does not change the balance", "1000.00", wallet.getBalance());
        SimpleTest.checkEquals("Cancelled top-up creates no statement entry", 1, wallet.getStatement().size());

        SimpleTest.checkThrows("Top-up of 0 is rejected", InvalidAmountException.class,
                () -> service.requestTopUp("W1001", ZERO, "UPI"));
        SimpleTest.checkThrows("Top-up above the limit is rejected", InvalidAmountException.class,
                () -> service.requestTopUp("W1001", new BigDecimal("50000.01"), "UPI"));
        SimpleTest.checkThrows("Top-up to unknown wallet is rejected", RecordNotFoundException.class,
                () -> service.requestTopUp("W9999", new BigDecimal("100"), "UPI"));
    }

    private static void transfers() throws WalletException {
        SimpleTest.section("Transfers");
        WalletService service = newServiceWithTwoWallets();
        Wallet asha = service.getWallet("W1001");
        Wallet ravi = service.getWallet("W1002");
        fund(service, "W1001", "5000");

        Transfer small = service.transfer("W1001", "W1002", new BigDecimal("800"));
        SimpleTest.checkMoney("Small transfer has no fee", "0.00", small.getFee());
        SimpleTest.checkMoney("Source balance reduced by the amount", "4200.00", asha.getBalance());
        SimpleTest.checkMoney("Destination balance increased by the amount", "800.00", ravi.getBalance());
        SimpleTest.checkMoney("Receipt shows sender balance", "4200.00", small.getSourceBalanceAfter());
        SimpleTest.checkMoney("Receipt shows receiver balance", "800.00", small.getDestinationBalanceAfter());
        SimpleTest.checkEquals("Source has top-up + transfer entries", 2, asha.getStatement().size());
        SimpleTest.checkEquals("Destination has one entry", 1, ravi.getStatement().size());

        Transfer big = service.transfer("W1001", "W1002", new BigDecimal("2000"));
        SimpleTest.checkMoney("Transfer of 2000 has a 20.00 fee", "20.00", big.getFee());
        SimpleTest.checkMoney("Source pays amount + fee", "2180.00", asha.getBalance());
        SimpleTest.checkMoney("Destination receives only the amount", "2800.00", ravi.getBalance());
        SimpleTest.checkMoney("Total debited is amount + fee", "2020.00", big.getTotalAmount());
        // entries so far: top-up, transfer 800 (no fee), transfer 2000, fee 20.00
        SimpleTest.checkEquals("Fee is recorded as its own statement entry", 4, asha.getStatement().size());
        SimpleTest.checkEquals("Last source entry is the FEE", "FEE",
                asha.getStatement().get(3).getType());

        int sourceEntries = asha.getStatement().size();
        SimpleTest.checkThrows("Transfer with insufficient funds is rejected", InsufficientFundsException.class,
                () -> service.transfer("W1001", "W1002", new BigDecimal("3000")));
        SimpleTest.checkMoney("Failed transfer leaves source balance unchanged", "2180.00", asha.getBalance());
        SimpleTest.checkMoney("Failed transfer leaves destination balance unchanged", "2800.00", ravi.getBalance());
        SimpleTest.checkEquals("Failed transfer adds no statement entry", sourceEntries, asha.getStatement().size());

        // Balance is 2180: it covers 2175 on its own, but 2175 + 1% fee (21.75) = 2196.75 does not fit.
        SimpleTest.checkThrows("Fee is included in the balance check", InsufficientFundsException.class,
                () -> service.transfer("W1001", "W1002", new BigDecimal("2175")));
        SimpleTest.checkMoney("Nothing is debited when the fee makes the transfer unaffordable", "2180.00",
                asha.getBalance());
        SimpleTest.checkMoney("Nothing is credited when the fee makes the transfer unaffordable", "2800.00",
                ravi.getBalance());
        SimpleTest.checkEquals("No partial statement entries are left behind", sourceEntries,
                asha.getStatement().size());

        SimpleTest.checkThrows("Transfer to the same wallet is rejected", InvalidInputException.class,
                () -> service.transfer("W1001", "W1001", new BigDecimal("100")));
        SimpleTest.checkThrows("Transfer to unknown wallet is rejected", RecordNotFoundException.class,
                () -> service.transfer("W1001", "W9999", new BigDecimal("100")));
        SimpleTest.checkThrows("Transfer of negative amount is rejected", InvalidAmountException.class,
                () -> service.transfer("W1001", "W1002", new BigDecimal("-5")));
    }

    private static void payments() throws WalletException {
        SimpleTest.section("Merchant payments");
        WalletService service = newServiceWithTwoWallets();
        Wallet asha = service.getWallet("W1001");
        fund(service, "W1001", "1000");

        Payment payment = service.payMerchant("W1001", "Campus Cafe", new BigDecimal("250.50"));
        SimpleTest.checkMoney("Payment reduces the balance", "749.50", asha.getBalance());
        SimpleTest.checkMoney("Receipt shows resulting balance", "749.50", payment.getBalanceAfter());
        SimpleTest.checkEquals("Payment creates a statement entry", 2, asha.getStatement().size());
        SimpleTest.checkEquals("Statement entry type is PAYMENT", "PAYMENT",
                asha.getStatement().get(1).getType());
        SimpleTest.check("Payment is not refunded yet", !payment.isRefunded());

        SimpleTest.checkThrows("Payment above balance is rejected", InsufficientFundsException.class,
                () -> service.payMerchant("W1001", "Campus Cafe", new BigDecimal("800")));
        SimpleTest.checkMoney("Rejected payment leaves balance unchanged", "749.50", asha.getBalance());
        SimpleTest.checkEquals("Rejected payment adds no statement entry", 2, asha.getStatement().size());
        SimpleTest.checkThrows("Payment with invalid merchant name is rejected", InvalidInputException.class,
                () -> service.payMerchant("W1001", "@@", new BigDecimal("10")));
    }

    private static void refunds() throws WalletException {
        SimpleTest.section("Refunds");
        WalletService service = newServiceWithTwoWallets();
        Wallet asha = service.getWallet("W1001");
        Wallet ravi = service.getWallet("W1002");
        fund(service, "W1001", "1000");
        Payment payment = service.payMerchant("W1001", "Campus Cafe", new BigDecimal("400"));

        SimpleTest.checkThrows("Refund of unknown payment is rejected", RecordNotFoundException.class,
                () -> service.refund("W1001", "PAY9999"));
        SimpleTest.checkThrows("Refund from a different wallet is rejected", RefundNotAllowedException.class,
                () -> service.refund("W1002", payment.getTxnId()));

        Refund refund = service.refund("W1001", payment.getTxnId());
        SimpleTest.checkMoney("Refund credits the full payment amount", "1000.00", asha.getBalance());
        SimpleTest.checkMoney("Refund receipt shows resulting balance", "1000.00", refund.getBalanceAfter());
        SimpleTest.check("Refund is linked to the original payment", refund.getPayment() == payment);
        SimpleTest.check("Payment is marked as refunded", payment.isRefunded());
        SimpleTest.checkEquals("Refund creates a statement entry (top-up, payment, refund)", 3,
                asha.getStatement().size());
        SimpleTest.checkThrows("Refunding the same payment twice is rejected", RefundNotAllowedException.class,
                () -> service.refund("W1001", payment.getTxnId()));
        SimpleTest.checkMoney("Other wallet is not affected by the refund", "0.00", ravi.getBalance());
    }

    private static void statements() throws WalletException {
        SimpleTest.section("Statements (every transaction creates an entry, running balance is correct)");
        WalletService service = newServiceWithTwoWallets();
        Wallet asha = service.getWallet("W1001");

        fund(service, "W1001", "5000");
        service.transfer("W1001", "W1002", new BigDecimal("2000"));
        Payment payment = service.payMerchant("W1001", "Book Store", new BigDecimal("300"));
        service.refund("W1001", payment.getTxnId());

        List<StatementEntry> entries = asha.getStatement();
        // top-up, transfer out, fee, payment, refund
        SimpleTest.checkEquals("Entries: top-up, transfer, fee, payment, refund", 5, entries.size());

        boolean runningBalanceOk = true;
        boolean inOrder = true;
        BigDecimal expected = new BigDecimal("0.00");
        for (int i = 0; i < entries.size(); i++) {
            StatementEntry entry = entries.get(i);
            expected = expected.add(entry.getCredit()).subtract(entry.getDebit());
            if (expected.compareTo(entry.getBalanceAfter()) != 0) {
                runningBalanceOk = false;
            }
            if (entry.getEntryNo() != i + 1) {
                inOrder = false;
            }
        }
        SimpleTest.check("Running balance equals previous balance + credit - debit on every line", runningBalanceOk);
        SimpleTest.check("Entries are numbered 1, 2, 3 ... in order", inOrder);
        SimpleTest.checkMoney("Last running balance equals the wallet balance", asha.getBalance().toPlainString(),
                entries.get(entries.size() - 1).getBalanceAfter());
        SimpleTest.checkMoney("Final wallet balance is correct (5000 - 2020 - 300 + 300)", "2980.00",
                asha.getBalance());

        boolean readOnly = false;
        try {
            entries.add(null);
        } catch (UnsupportedOperationException e) {
            readOnly = true;
        }
        SimpleTest.check("Statement list cannot be edited from outside (encapsulation)", readOnly);
    }

    private static void polymorphism() throws WalletException {
        SimpleTest.section("Polymorphism - each Transaction type prints its own receipt");
        WalletService service = newServiceWithTwoWallets();
        TopUp topUp = service.requestTopUp("W1001", new BigDecimal("1000"), "UPI");
        service.confirmTopUp(topUp);
        Transfer transfer = service.transfer("W1001", "W1002", new BigDecimal("100"));
        Payment payment = service.payMerchant("W1001", "Campus Cafe", new BigDecimal("50"));
        Refund refund = service.refund("W1001", payment.getTxnId());

        List<Transaction> all = new ArrayList<>();
        all.add(topUp);
        all.add(transfer);
        all.add(payment);
        all.add(refund);

        String[] expectedTypes = {"TOP-UP", "TRANSFER", "PAYMENT", "REFUND"};
        for (int i = 0; i < all.size(); i++) {
            Transaction txn = all.get(i);
            SimpleTest.checkEquals("getType() of " + txn.getTxnId(), expectedTypes[i], txn.getType());
            String receipt = txn.getReceipt();
            SimpleTest.check("Receipt of " + txn.getTxnId() + " has title, ID and a fee line",
                    receipt.contains(expectedTypes[i] + " RECEIPT")
                            && receipt.contains(txn.getTxnId())
                            && receipt.contains("Fee"));
        }
    }
}
