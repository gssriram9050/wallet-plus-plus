package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import exception.DuplicateRecordException;
import exception.InsufficientFundsException;
import exception.InvalidInputException;
import exception.RecordNotFoundException;
import exception.RefundNotAllowedException;
import exception.WalletException;
import model.Payment;
import model.Refund;
import model.StatementEntry;
import model.TopUp;
import model.TransactionStatus;
import model.Transfer;
import model.User;
import model.Wallet;
import util.FeeCalculator;
import util.Format;
import util.Validator;

/**
 * All business rules of Wallet++ are enforced here.
 * Data is kept in memory only (no files or database), so it is cleared when the program exits.
 */
public class WalletService {

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Wallet> wallets = new LinkedHashMap<>();
    private final Map<String, Payment> payments = new LinkedHashMap<>();

    private int nextUserNo = 101;
    private int nextWalletNo = 1001;
    private int nextTxnNo = 1001;

    // ------------------------------------------------------------ users and wallets

    public User addUser(String name, String phone, String email) throws WalletException {
        String cleanName = Validator.validateName(name);
        String cleanPhone = Validator.validatePhone(phone);
        String cleanEmail = Validator.validateEmail(email);

        for (User existing : users.values()) {
            if (existing.getPhone().equals(cleanPhone)) {
                throw new DuplicateRecordException("A user with phone number " + cleanPhone
                        + " already exists (" + existing.getUserId() + ").");
            }
            if (existing.getEmail().equalsIgnoreCase(cleanEmail)) {
                throw new DuplicateRecordException("A user with email " + cleanEmail
                        + " already exists (" + existing.getUserId() + ").");
            }
        }

        User user = new User("U" + nextUserNo++, cleanName, cleanPhone, cleanEmail);
        users.put(user.getUserId(), user);
        return user;
    }

    /** Opens a wallet with Rs. 0.00 for a user. A user can have only one wallet. */
    public Wallet createWallet(String userId) throws WalletException {
        User user = findUser(userId);
        if (user.hasWallet()) {
            throw new DuplicateRecordException(
                    user.getName() + " already has wallet " + user.getWallet().getWalletId() + ".");
        }
        Wallet wallet = new Wallet("W" + nextWalletNo++, user);
        user.setWallet(wallet);
        wallets.put(wallet.getWalletId(), wallet);
        return wallet;
    }

    public User findUser(String userId) throws WalletException {
        String id = Validator.validateId(userId);
        User user = users.get(id);
        if (user == null) {
            throw new RecordNotFoundException("User " + id + " was not found.");
        }
        return user;
    }

    public Wallet getWallet(String walletId) throws WalletException {
        String id = Validator.validateId(walletId);
        Wallet wallet = wallets.get(id);
        if (wallet == null) {
            throw new RecordNotFoundException("Wallet " + id + " was not found.");
        }
        return wallet;
    }

    public Collection<User> getUsers() { return Collections.unmodifiableCollection(users.values()); }
    public Collection<Wallet> getWallets() { return Collections.unmodifiableCollection(wallets.values()); }

    /** All payments made from one wallet (used to choose a payment to refund). */
    public List<Payment> getPaymentsOf(String walletId) throws WalletException {
        Wallet wallet = getWallet(walletId);
        List<Payment> result = new ArrayList<>();
        for (Payment payment : payments.values()) {
            if (payment.getWallet() == wallet) {
                result.add(payment);
            }
        }
        return result;
    }

    // ------------------------------------------------------------ top-up (two steps)

    /** Step 1: creates a PENDING top-up. The balance does NOT change yet. */
    public TopUp requestTopUp(String walletId, BigDecimal amount, String source) throws WalletException {
        Validator.checkAmount(amount);
        Wallet wallet = getWallet(walletId);
        return new TopUp(nextTxnId("TOP"), wallet, amount, source);
    }

    /** Step 2 (after the user confirms): adds the money and writes the statement entry. */
    public void confirmTopUp(TopUp topUp) throws InvalidInputException {
        if (topUp.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidInputException("This top-up has already been processed.");
        }
        StatementEntry entry = topUp.getWallet().credit(topUp.getTxnId(), "TOP-UP",
                "Top-up via " + topUp.getSource(), topUp.getAmount());
        topUp.setBalanceAfter(entry.getBalanceAfter());
        topUp.setStatus(TransactionStatus.SUCCESS);
    }

    /** The user said "No": nothing is added and no statement entry is created. */
    public void cancelTopUp(TopUp topUp) throws InvalidInputException {
        if (topUp.getStatus() != TransactionStatus.PENDING) {
            throw new InvalidInputException("This top-up has already been processed.");
        }
        topUp.setStatus(TransactionStatus.CANCELLED);
    }

    // ------------------------------------------------------------ transfer

    /**
     * Moves money between two wallets. The sender pays amount + fee. Both wallets
     * get a statement entry (the sender also gets a separate FEE entry when a fee applies).
     */
    public Transfer transfer(String fromWalletId, String toWalletId, BigDecimal amount) throws WalletException {
        Validator.checkAmount(amount);
        Wallet from = getWallet(fromWalletId);
        Wallet to = getWallet(toWalletId);
        if (from == to) {
            throw new InvalidInputException("Source and destination wallets must be different.");
        }

        BigDecimal fee = FeeCalculator.transferFee(amount);
        BigDecimal total = amount.add(fee);
        if (!from.hasSufficientBalance(total)) {
            String detail = fee.signum() > 0
                    ? " (amount " + Format.money(amount) + " + fee " + Format.money(fee) + ")"
                    : "";
            throw new InsufficientFundsException("Insufficient funds in wallet " + from.getWalletId()
                    + ". Available: " + Format.money(from.getBalance())
                    + ", required: " + Format.money(total) + detail + ".", from.getBalance(), total);
        }

        String txnId = nextTxnId("TRF");
        from.debit(txnId, "TRANSFER OUT", "Transfer to " + to, amount);
        if (fee.signum() > 0) {
            from.debit(txnId, "FEE", "Transfer fee", fee);
        }
        to.credit(txnId, "TRANSFER IN", "Transfer from " + from, amount);

        Transfer transfer = new Transfer(txnId, from, to, amount, fee);
        transfer.setBalancesAfter(from.getBalance(), to.getBalance());
        return transfer;
    }

    // ------------------------------------------------------------ merchant payment

    public Payment payMerchant(String walletId, String merchant, BigDecimal amount) throws WalletException {
        Validator.checkAmount(amount);
        String merchantName = Validator.validateMerchant(merchant);
        Wallet wallet = getWallet(walletId);

        if (!wallet.hasSufficientBalance(amount)) {
            throw new InsufficientFundsException("Insufficient funds in wallet " + wallet.getWalletId()
                    + ". Available: " + Format.money(wallet.getBalance())
                    + ", required: " + Format.money(amount) + ".", wallet.getBalance(), amount);
        }

        String txnId = nextTxnId("PAY");
        StatementEntry entry = wallet.debit(txnId, "PAYMENT", "Paid to " + merchantName, amount);
        Payment payment = new Payment(txnId, wallet, merchantName, amount);
        payment.setBalanceAfter(entry.getBalanceAfter());
        payments.put(txnId, payment);
        return payment;
    }

    // ------------------------------------------------------------ refund

    /** Credits the full payment amount back, but only for a valid, not-yet-refunded payment of that wallet. */
    public Refund refund(String walletId, String paymentId) throws WalletException {
        Wallet wallet = getWallet(walletId);
        String id = Validator.validateId(paymentId);

        Payment payment = payments.get(id);
        if (payment == null) {
            throw new RecordNotFoundException("Payment " + id + " was not found.");
        }
        if (payment.getWallet() != wallet) {
            throw new RefundNotAllowedException(
                    "Payment " + id + " was not made from wallet " + wallet.getWalletId() + ".");
        }
        if (payment.isRefunded()) {
            throw new RefundNotAllowedException("Payment " + id + " has already been refunded.");
        }

        String txnId = nextTxnId("REF");
        StatementEntry entry = wallet.credit(txnId, "REFUND",
                "Refund of " + id + " (" + payment.getMerchant() + ")", payment.getAmount());
        payment.markRefunded();

        Refund refund = new Refund(txnId, payment);
        refund.setBalanceAfter(entry.getBalanceAfter());
        return refund;
    }

    // ------------------------------------------------------------ helpers

    private String nextTxnId(String prefix) { return prefix + nextTxnNo++; }
}
