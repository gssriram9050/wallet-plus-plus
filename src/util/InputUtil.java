package util;

import java.math.BigDecimal;
import java.util.Scanner;

import exception.InvalidInputException;

/**
 * Reads and validates everything the user types.
 * If the text is in the wrong format, the error is shown and the same
 * question is asked again, so the program never crashes on bad input.
 */
public class InputUtil {

    /** One validation rule: turns typed text into a value or throws InvalidInputException. */
    private interface Checker<T> {
        T check(String input) throws InvalidInputException;
    }

    private final Scanner scanner;

    public InputUtil(Scanner scanner) { this.scanner = scanner; }

    private <T> T read(String prompt, Checker<T> checker) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return checker.check(line);
            } catch (InvalidInputException e) {
                System.out.println("  [INVALID INPUT] " + e.getMessage());
            }
        }
    }

    public int readMenuChoice(String prompt, int min, int max) { return read(prompt, text -> parseChoice(text, min, max)); }

    private static Integer parseChoice(String text, int min, int max) throws InvalidInputException {
        try {
            int choice = Integer.parseInt(text.trim());
            if (choice < min || choice > max) {
                throw new InvalidInputException("Please enter a number between " + min + " and " + max + ".");
            }
            return choice;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public String readName(String prompt) { return read(prompt, Validator::validateName); }
    public String readPhone(String prompt) { return read(prompt, Validator::validatePhone); }
    public String readEmail(String prompt) { return read(prompt, Validator::validateEmail); }
    public String readMerchant(String prompt) { return read(prompt, Validator::validateMerchant); }
    public String readId(String prompt) { return read(prompt, Validator::validateId); }
    public BigDecimal readAmount(String prompt) { return read(prompt, Validator::parseAmount); }

    public boolean readYesNo(String prompt) {
        return read(prompt, text -> {
            String answer = text.trim().toUpperCase();
            if (answer.equals("Y") || answer.equals("YES")) {
                return Boolean.TRUE;
            }
            if (answer.equals("N") || answer.equals("NO")) {
                return Boolean.FALSE;
            }
            throw new InvalidInputException("Please enter Y or N.");
        });
    }
}
