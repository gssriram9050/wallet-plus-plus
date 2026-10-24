package util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import exception.InvalidAmountException;
import exception.InvalidInputException;

/** All input checks live here so the same rules are used by the menu and the service. */
public final class Validator {

    public static final BigDecimal MIN_AMOUNT = new BigDecimal("1.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("50000.00");

    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,29}");
    private static final Pattern PHONE = Pattern.compile("[6-9][0-9]{9}");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern MERCHANT = Pattern.compile("[A-Za-z0-9][A-Za-z0-9 &.'-]{1,29}");
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9]{2,12}");
    private static final Pattern AMOUNT = Pattern.compile("[0-9]+(\\.[0-9]{1,2})?");

    private Validator() {
        // utility class - no objects needed
    }

    private static String clean(String input) { return input == null ? "" : input.trim(); }

    public static String validateName(String input) throws InvalidInputException {
        String value = clean(input);
        if (!NAME.matcher(value).matches()) {
            throw new InvalidInputException(
                    "Name must be 2-30 characters long and contain only letters, spaces, '.', '-' or an apostrophe.");
        }
        return value;
    }

    public static String validatePhone(String input) throws InvalidInputException {
        String value = clean(input);
        if (!PHONE.matcher(value).matches()) {
            throw new InvalidInputException("Phone number must be 10 digits and start with 6, 7, 8 or 9.");
        }
        return value;
    }

    public static String validateEmail(String input) throws InvalidInputException {
        String value = clean(input);
        if (!EMAIL.matcher(value).matches()) {
            throw new InvalidInputException("Enter a valid email address (for example name@example.com).");
        }
        return value;
    }

    public static String validateMerchant(String input) throws InvalidInputException {
        String value = clean(input);
        if (!MERCHANT.matcher(value).matches()) {
            throw new InvalidInputException(
                    "Merchant name must be 2-30 characters (letters, digits, spaces, & . ' -).");
        }
        return value;
    }

    /** Checks an ID such as U101 / W1001 / PAY1004 and returns it in upper case. */
    public static String validateId(String input) throws InvalidInputException {
        String value = clean(input);
        if (!ID.matcher(value).matches()) {
            throw new InvalidInputException("Enter a valid ID (letters and digits only, for example W1001).");
        }
        return value.toUpperCase();
    }

    /** Converts typed text such as "250" or "250.50" to a BigDecimal with 2 decimals. */
    public static BigDecimal parseAmount(String input) throws InvalidAmountException {
        String value = clean(input);
        if (!AMOUNT.matcher(value).matches()) {
            throw new InvalidAmountException(
                    "Enter the amount as a number with up to 2 decimals (for example 250 or 250.50).");
        }
        BigDecimal amount = new BigDecimal(value).setScale(2);
        checkAmount(amount);
        return amount;
    }

    /** Amount must be between Rs. 1.00 and Rs. 50,000.00 per transaction. */
    public static void checkAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException("Amount must be at least " + Format.money(MIN_AMOUNT) + ".");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                    "Amount cannot exceed " + Format.money(MAX_AMOUNT) + " per transaction.");
        }
    }
}
