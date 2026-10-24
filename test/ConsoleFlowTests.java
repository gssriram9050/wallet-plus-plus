package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import main.WalletPlusPlusApp;

/**
 * Runs the real menu program with typed input fed from a string and checks the printed text.
 * This tests the menu, validation re-prompts and the receipts end to end.
 */
public class ConsoleFlowTests {

    /** Runs WalletPlusPlusApp with the given input lines and returns everything it printed. */
    private static String runApp(String... lines) {
        StringBuilder script = new StringBuilder();
        for (String line : lines) {
            script.append(line).append("\n");
        }

        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(script.toString().getBytes("UTF-8")));
            System.setOut(new PrintStream(captured, true, "UTF-8"));
            WalletPlusPlusApp.main(new String[0]);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        try {
            return captured.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void run() {
        SimpleTest.section("Console flow - complete session through the menu");
        String full = runApp(
                "1", "Asha Raman", "9876543210", "asha@example.com",
                "1", "Ravi Kumar", "9123456780", "ravi@example.com",
                "2", "U101",
                "2", "U102",
                "3", "W1001", "5000", "1", "Y",
                "3", "W1001", "100", "2", "N",
                "4", "W1001", "W1002", "2000",
                "4", "W1002", "W1001", "3000",
                "5", "W1002", "Campus Cafe", "450",
                "6", "W1002", "PAY1004",
                "6", "W1002", "PAY1004",
                "7", "W1001",
                "8");

        SimpleTest.check("Menu shows all 8 options", full.contains("1. Add User")
                && full.contains("2. Create Wallet") && full.contains("3. Top-up")
                && full.contains("4. Transfer") && full.contains("5. Pay Merchant")
                && full.contains("6. Refund") && full.contains("7. View Statement")
                && full.contains("8. Exit"));
        SimpleTest.check("User and wallet creation messages are shown",
                full.contains("User added successfully.") && full.contains("Wallet created successfully."));
        SimpleTest.check("Top-up receipt is shown after confirmation", full.contains("TOP-UP RECEIPT")
                && full.contains("Rs. 5,000.00"));
        SimpleTest.check("Cancelled top-up message is shown", full.contains("Top-up cancelled. No money was added."));
        SimpleTest.check("Transfer receipt shows fee and both balances", full.contains("TRANSFER RECEIPT")
                && full.contains("Rs. 20.00") && full.contains("Sender Balance")
                && full.contains("Receiver Balance"));
        SimpleTest.check("Insufficient funds error is shown", full.contains("[ERROR] Insufficient funds"));
        SimpleTest.check("Payment receipt is shown", full.contains("PAYMENT RECEIPT")
                && full.contains("Campus Cafe"));
        SimpleTest.check("Refund receipt is shown", full.contains("REFUND RECEIPT"));
        SimpleTest.check("Second refund is rejected with a clear message",
                full.contains("has already been refunded"));
        SimpleTest.check("Statement shows entries, running balance and closing balance",
                full.contains("WALLET STATEMENT") && full.contains("Balance")
                        && full.contains("Closing Balance : Rs. 2,980.00"));
        SimpleTest.check("Program says goodbye on Exit", full.contains("Goodbye!"));

        SimpleTest.section("Console flow - invalid input never crashes the program");
        String bad = runApp(
                "abc", "9", "",            // bad menu choices
                "1", "", "A1", "Asha Raman",      // bad names, then a good one
                "12345", "1234567890", "9876543210",   // bad phones, then a good one
                "not-an-email", "asha@example.com",
                "3",                       // top-up before any wallet exists
                "2", "U999",               // wallet for unknown user
                "8");
        SimpleTest.check("Non-numeric menu choice is rejected and asked again",
                bad.contains("[INVALID INPUT] Please enter a number between 1 and 8."));
        SimpleTest.check("Invalid name is rejected", bad.contains("[INVALID INPUT] Name must be"));
        SimpleTest.check("Invalid phone is rejected", bad.contains("[INVALID INPUT] Phone number must be"));
        SimpleTest.check("Invalid email is rejected", bad.contains("[INVALID INPUT] Enter a valid email"));
        SimpleTest.check("Top-up without a wallet gives a clear error",
                bad.contains("[ERROR] No wallets exist yet"));
        SimpleTest.check("Wallet for unknown user gives a clear error", bad.contains("[ERROR] User U999 was not found."));
        SimpleTest.check("Program still exits normally after all the errors", bad.contains("Goodbye!"));

        SimpleTest.section("Console flow - input ending suddenly (Ctrl+Z / end of file)");
        String ended = runApp("1", "Asha Raman");
        SimpleTest.check("Program exits cleanly when input ends", ended.contains("Input ended. Exiting Wallet++."));
    }
}
