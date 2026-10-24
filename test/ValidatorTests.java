package test;

import java.math.BigDecimal;

import exception.InvalidAmountException;
import exception.InvalidInputException;
import util.FeeCalculator;
import util.Validator;

/** Tests for the input rules and the fee rule. */
public class ValidatorTests {

    public static void run() {
        SimpleTest.section("Validator - names, phone, email, merchant, IDs");

        SimpleTest.checkThrows("Blank name is rejected", InvalidInputException.class,
                () -> Validator.validateName("   "));
        SimpleTest.checkThrows("Name with digits is rejected", InvalidInputException.class,
                () -> Validator.validateName("Asha123"));
        SimpleTest.checkThrows("Phone with 9 digits is rejected", InvalidInputException.class,
                () -> Validator.validatePhone("987654321"));
        SimpleTest.checkThrows("Phone starting with 5 is rejected", InvalidInputException.class,
                () -> Validator.validatePhone("5876543210"));
        SimpleTest.checkThrows("Phone with letters is rejected", InvalidInputException.class,
                () -> Validator.validatePhone("98765abcde"));
        SimpleTest.checkThrows("Email without @ is rejected", InvalidInputException.class,
                () -> Validator.validateEmail("asha.example.com"));
        SimpleTest.checkThrows("Blank merchant is rejected", InvalidInputException.class,
                () -> Validator.validateMerchant(""));
        SimpleTest.checkThrows("ID with symbols is rejected", InvalidInputException.class,
                () -> Validator.validateId("W10$1"));

        try {
            SimpleTest.checkEquals("Valid name is accepted (trimmed)", "Asha Raman",
                    Validator.validateName("  Asha Raman "));
            SimpleTest.checkEquals("Valid phone is accepted", "9876543210", Validator.validatePhone("9876543210"));
            SimpleTest.checkEquals("Valid email is accepted", "asha@example.com",
                    Validator.validateEmail("asha@example.com"));
            SimpleTest.checkEquals("Valid merchant is accepted", "Campus Cafe",
                    Validator.validateMerchant("Campus Cafe"));
            SimpleTest.checkEquals("ID is converted to upper case", "W1001", Validator.validateId("w1001"));
        } catch (InvalidInputException e) {
            SimpleTest.check("Valid inputs should not throw: " + e.getMessage(), false);
        }

        SimpleTest.section("Validator - amounts");
        SimpleTest.checkThrows("Text amount is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("abc"));
        SimpleTest.checkThrows("Negative amount is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("-50"));
        SimpleTest.checkThrows("Zero amount is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("0"));
        SimpleTest.checkThrows("Amount below Rs. 1 is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("0.50"));
        SimpleTest.checkThrows("More than 2 decimals is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("10.555"));
        SimpleTest.checkThrows("Amount above Rs. 50,000 is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("50000.01"));
        SimpleTest.checkThrows("Scientific notation is rejected", InvalidAmountException.class,
                () -> Validator.parseAmount("1e3"));

        try {
            SimpleTest.checkMoney("250 is accepted", "250.00", Validator.parseAmount("250"));
            SimpleTest.checkMoney("250.5 is accepted", "250.50", Validator.parseAmount("250.5"));
            SimpleTest.checkMoney("Maximum 50000 is accepted", "50000.00", Validator.parseAmount("50000"));
        } catch (InvalidAmountException e) {
            SimpleTest.check("Valid amounts should not throw: " + e.getMessage(), false);
        }

        SimpleTest.section("FeeCalculator - transfer fee");
        SimpleTest.checkMoney("Rs. 250.50 transfer is free", "0.00",
                FeeCalculator.transferFee(new BigDecimal("250.50")));
        SimpleTest.checkMoney("Rs. 1000.00 transfer is free (limit is inclusive)", "0.00",
                FeeCalculator.transferFee(new BigDecimal("1000.00")));
        SimpleTest.checkMoney("Rs. 1000.01 transfer pays 10.00 (1%, rounded)", "10.00",
                FeeCalculator.transferFee(new BigDecimal("1000.01")));
        SimpleTest.checkMoney("Rs. 5000.00 transfer pays 50.00", "50.00",
                FeeCalculator.transferFee(new BigDecimal("5000.00")));
    }
}
