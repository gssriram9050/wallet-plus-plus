package test;

/** Runs every test class and exits with code 1 if any check failed. */
public class RunAllTests {

    public static void main(String[] args) {
        ValidatorTests.run();
        WalletServiceTests.run();
        ConsoleFlowTests.run();

        int failed = SimpleTest.summary();
        if (failed > 0) {
            System.exit(1);
        }
    }
}
