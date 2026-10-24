package test;

import java.math.BigDecimal;

/**
 * A tiny test helper (JUnit is not used because the project must run with only a JDK).
 * Each check prints PASS or FAIL and the totals are shown at the end.
 */
public final class SimpleTest {

    /** A piece of code that may throw an exception. */
    public interface Action {
        void run() throws Exception;
    }

    private static int passed = 0;
    private static int failed = 0;

    private SimpleTest() {
    }

    public static void section(String title) {
        System.out.println();
        System.out.println("== " + title + " ==");
    }

    public static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    public static void checkEquals(String name, Object expected, Object actual) {
        boolean same = expected == null ? actual == null : expected.equals(actual);
        check(name + (same ? "" : "  (expected " + expected + " but was " + actual + ")"), same);
    }

    /** Compares money values by value (so 5.0 and 5.00 are equal). */
    public static void checkMoney(String name, String expected, BigDecimal actual) {
        boolean same = actual != null && new BigDecimal(expected).compareTo(actual) == 0;
        check(name + (same ? "" : "  (expected " + expected + " but was " + actual + ")"), same);
    }

    /** Passes only if the action throws an exception of the given type. */
    public static void checkThrows(String name, Class<? extends Exception> expected, Action action) {
        try {
            action.run();
            check(name + "  (no exception was thrown)", false);
        } catch (Exception e) {
            boolean right = expected.isInstance(e);
            check(name + (right ? "" : "  (got " + e.getClass().getSimpleName() + ")"), right);
        }
    }

    /** Prints the totals and returns the number of failed checks. */
    public static int summary() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("Result: " + passed + " passed, " + failed + " failed");
        System.out.println("==============================================");
        return failed;
    }
}
