package main;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import exception.RecordNotFoundException;
import exception.WalletException;
import model.Payment;
import model.Refund;
import model.StatementEntry;
import model.TopUp;
import model.Transfer;
import model.User;
import model.Wallet;
import service.WalletService;
import util.FeeCalculator;
import util.Format;
import util.InputUtil;

/**
 * Wallet++ - a menu-driven Digital Wallet console application.
 * Assignment II, Specification 29: Digital Wallet & Transfers.
 */
public class WalletPlusPlusApp {

    private static final String[] TOP_UP_SOURCES = {"UPI", "Debit Card", "Net Banking"};

    private final WalletService service = new WalletService();
    private final InputUtil input;

    public WalletPlusPlusApp(Scanner scanner) {
        this.input = new InputUtil(scanner);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        new WalletPlusPlusApp(scanner).run();
    }

    /** The main menu loop. Every error is caught here, so the program never crashes. */
    public void run() {
        boolean running = true;
        while (running) {
            try {
                printMenu();
                int choice = input.readMenuChoice("Enter your choice (1-8): ", 1, 8);
                running = handleChoice(choice);
            } catch (WalletException e) {
                System.out.println();
                System.out.println("  [ERROR] " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println();
                System.out.println("Input ended. Exiting Wallet++.");
                running = false;
            } catch (RuntimeException e) {
                System.out.println();
                System.out.println("  [ERROR] Something went wrong: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println(Format.line('=', 46));
        System.out.println(Format.center("WALLET++ - DIGITAL WALLET", 46));
        System.out.println(Format.line('=', 46));
        System.out.println("  1. Add User");
        System.out.println("  2. Create Wallet");
        System.out.println("  3. Top-up");
        System.out.println("  4. Transfer");
        System.out.println("  5. Pay Merchant");
        System.out.println("  6. Refund");
        System.out.println("  7. View Statement");
        System.out.println("  8. Exit");
        System.out.println(Format.line('-', 46));
    }

    /** Returns false when the user chooses Exit. */
    private boolean handleChoice(int choice) throws WalletException {
        switch (choice) {
            case 1:
                addUser();
                return true;
            case 2:
                createWallet();
                return true;
            case 3:
                topUp();
                return true;
            case 4:
                transfer();
                return true;
            case 5:
                payMerchant();
                return true;
            case 6:
                refund();
                return true;
            case 7:
                viewStatement();
                return true;
            default:
                System.out.println();
                System.out.println("Thank you for using Wallet++. Goodbye!");
                return false;
        }
    }

    // ------------------------------------------------------------ menu actions

    private void addUser() throws WalletException {
        System.out.println();
        System.out.println("--- Add User ---");
        String name = input.readName("Enter name: ");
        String phone = input.readPhone("Enter 10-digit phone number: ");
        String email = input.readEmail("Enter email: ");

        User user = service.addUser(name, phone, email);
        System.out.println();
        System.out.println("User added successfully.");
        System.out.println(Format.row("User ID", user.getUserId()));
        System.out.println(Format.row("Name", user.getName()));
        System.out.println(Format.row("Phone", user.getPhone()));
        System.out.println(Format.row("Email", user.getEmail()));
    }

    private void createWallet() throws WalletException {
        System.out.println();
        System.out.println("--- Create Wallet ---");
        boolean anyWithoutWallet = false;
        for (User user : service.getUsers()) {
            if (!user.hasWallet()) {
                if (!anyWithoutWallet) {
                    System.out.println("Users without a wallet:");
                    anyWithoutWallet = true;
                }
                System.out.println("  " + user.getUserId() + " - " + user.getName());
            }
        }
        if (!anyWithoutWallet) {
            throw new RecordNotFoundException("There is no user without a wallet. Add a user first.");
        }

        String userId = input.readId("Enter User ID (for example U101): ");
        Wallet wallet = service.createWallet(userId);
        System.out.println();
        System.out.println("Wallet created successfully.");
        System.out.println(Format.row("Wallet ID", wallet.getWalletId()));
        System.out.println(Format.row("Owner", wallet.getOwner().getName()));
        System.out.println(Format.row("Balance", Format.money(wallet.getBalance())));
    }

    private void topUp() throws WalletException {
        System.out.println();
        System.out.println("--- Top-up ---");
        showWallets();
        String walletId = input.readId("Enter Wallet ID: ");
        BigDecimal amount = input.readAmount("Enter top-up amount (Rs.): ");

        System.out.println("Payment source:");
        for (int i = 0; i < TOP_UP_SOURCES.length; i++) {
            System.out.println("  " + (i + 1) + ". " + TOP_UP_SOURCES[i]);
        }
        int sourceChoice = input.readMenuChoice("Choose source (1-3): ", 1, TOP_UP_SOURCES.length);

        // Step 1: create a pending top-up (balance unchanged)
        TopUp topUp = service.requestTopUp(walletId, amount, TOP_UP_SOURCES[sourceChoice - 1]);
        System.out.println();
        System.out.println("You are about to add " + Format.money(amount) + " to wallet "
                + topUp.getWallet() + " using " + topUp.getSource() + ".");

        // Step 2: add the money only after confirmation
        if (input.readYesNo("Confirm top-up? (Y/N): ")) {
            service.confirmTopUp(topUp);
            System.out.println();
            System.out.println(topUp.getReceipt());
        } else {
            service.cancelTopUp(topUp);
            System.out.println();
            System.out.println("Top-up cancelled. No money was added.");
        }
    }

    private void transfer() throws WalletException {
        System.out.println();
        System.out.println("--- Transfer ---");
        showWallets();
        System.out.println("Fee rule: transfers up to " + Format.money(FeeCalculator.FREE_TRANSFER_LIMIT)
                + " are free; above that a 1% fee is charged to the sender.");
        String fromId = input.readId("Enter SOURCE Wallet ID: ");
        String toId = input.readId("Enter DESTINATION Wallet ID: ");
        BigDecimal amount = input.readAmount("Enter transfer amount (Rs.): ");

        Transfer transfer = service.transfer(fromId, toId, amount);
        System.out.println();
        System.out.println(transfer.getReceipt());
    }

    private void payMerchant() throws WalletException {
        System.out.println();
        System.out.println("--- Pay Merchant ---");
        showWallets();
        String walletId = input.readId("Enter Wallet ID: ");
        String merchant = input.readMerchant("Enter merchant name: ");
        BigDecimal amount = input.readAmount("Enter payment amount (Rs.): ");

        Payment payment = service.payMerchant(walletId, merchant, amount);
        System.out.println();
        System.out.println(payment.getReceipt());
    }

    private void refund() throws WalletException {
        System.out.println();
        System.out.println("--- Refund ---");
        showWallets();
        String walletId = input.readId("Enter Wallet ID: ");

        List<Payment> payments = service.getPaymentsOf(walletId);
        if (payments.isEmpty()) {
            throw new RecordNotFoundException("Wallet " + walletId + " has no payments to refund.");
        }
        System.out.println("Payments made from this wallet:");
        for (Payment payment : payments) {
            System.out.println("  " + payment.getTxnId() + " | " + payment.getMerchant() + " | "
                    + Format.money(payment.getAmount()) + " | "
                    + (payment.isRefunded() ? "REFUNDED" : "refundable"));
        }
        String paymentId = input.readId("Enter Payment ID to refund (for example PAY1004): ");

        Refund refund = service.refund(walletId, paymentId);
        System.out.println();
        System.out.println(refund.getReceipt());
    }

    private void viewStatement() throws WalletException {
        System.out.println();
        System.out.println("--- View Statement ---");
        showWallets();
        String walletId = input.readId("Enter Wallet ID: ");
        Wallet wallet = service.getWallet(walletId);
        List<StatementEntry> entries = wallet.getStatement();

        String bar = Format.line('=', StatementEntry.TABLE_WIDTH);
        System.out.println();
        System.out.println(bar);
        System.out.println(Format.center("WALLET STATEMENT", StatementEntry.TABLE_WIDTH));
        System.out.println(bar);
        System.out.println(Format.row("Wallet", wallet));
        System.out.println(Format.row("Generated On", Format.dateTime(LocalDateTime.now())));
        System.out.println(Format.line('-', StatementEntry.TABLE_WIDTH));

        if (entries.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            System.out.println(StatementEntry.header());
            System.out.println(Format.line('-', StatementEntry.TABLE_WIDTH));
            BigDecimal totalCredit = new BigDecimal("0.00");
            BigDecimal totalDebit = new BigDecimal("0.00");
            for (StatementEntry entry : entries) {
                System.out.println(entry.toRow());
                totalCredit = totalCredit.add(entry.getCredit());
                totalDebit = totalDebit.add(entry.getDebit());
            }
            System.out.println(Format.line('-', StatementEntry.TABLE_WIDTH));
            System.out.println(Format.row("Total Credits", Format.money(totalCredit)));
            System.out.println(Format.row("Total Debits", Format.money(totalDebit)));
        }
        System.out.println(Format.row("Closing Balance", Format.money(wallet.getBalance())));
        System.out.println(bar);
    }

    /** Lists the wallets so the user knows which IDs can be typed. */
    private void showWallets() throws RecordNotFoundException {
        Collection<Wallet> wallets = service.getWallets();
        if (wallets.isEmpty()) {
            throw new RecordNotFoundException("No wallets exist yet. Add a user and create a wallet first.");
        }
        System.out.println("Available wallets:");
        for (Wallet wallet : wallets) {
            System.out.println("  " + wallet.getWalletId() + " - " + wallet.getOwner().getName()
                    + "  (Balance: " + Format.money(wallet.getBalance()) + ")");
        }
    }
}
