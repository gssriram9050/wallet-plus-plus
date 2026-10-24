# Manual Test Cases - Wallet++

Run these **in order** in one program session (start the program fresh; data is cleared every time it starts).
Write what you actually see in the last column and tick it. The expected results below match the program's behaviour.

How to start: `run.bat` (or `java -cp out main.WalletPlusPlusApp`).

Test data used:

| User ID | Name | Phone | Email |
|---|---|---|---|
| U101 | Asha Raman | 9876543210 | asha@example.com |
| U102 | Ravi Kumar | 9123456780 | ravi@example.com |
| U103 | Meena Iyer | 9000000002 | meena@example.com |

## A. Menu and menu input

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-01 | Start the program | Menu shows the 8 options: Add User, Create Wallet, Top-up, Transfer, Pay Merchant, Refund, View Statement, Exit | |
| TC-02 | Menu choice: `abc` | `[INVALID INPUT] Please enter a number between 1 and 8.` and the choice is asked again | |
| TC-03 | Menu choice: `9` | Same message as TC-02 | |

## B. Users and wallets

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-04 | `1` -> Asha Raman / 9876543210 / asha@example.com | `User added successfully.` with User ID `U101` | |
| TC-05 | `1` -> name `A1` | `[INVALID INPUT] Name must be ...` and the name is asked again (enter `Ravi Kumar` to continue) | |
| TC-06 | Phone `12345`, then `1234567890` | Both rejected with `[INVALID INPUT] Phone number must be 10 digits and start with 6, 7, 8 or 9.` (enter `9123456780` to continue) | |
| TC-07 | Email `not-an-email` | `[INVALID INPUT] Enter a valid email address ...` (enter `ravi@example.com` to continue); Ravi gets ID `U102` | |
| TC-08 | `1` -> Meena Iyer / 9000000002 / meena@example.com | User ID `U103` | |
| TC-09 | `1` -> any name / phone `9876543210` / any email | `[ERROR] A user with phone number 9876543210 already exists (U101).` | |
| TC-10 | `3` (Top-up) before any wallet exists | `[ERROR] No wallets exist yet. Add a user and create a wallet first.` | |
| TC-11 | `2` -> `U101` | `Wallet created successfully.` Wallet ID `W1001`, Balance `Rs. 0.00` | |
| TC-12 | `2` -> `U101` again | `[ERROR] Asha Raman already has wallet W1001.` | |
| TC-13 | `2` -> `U999` | `[ERROR] User U999 was not found.` | |
| TC-14 | `2` -> `U102`, then `2` -> `U103` | Wallets `W1002` and `W1003` created | |

## C. Top-up

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-15 | `3` -> `W1001` / `5000` / source `1` / confirm `Y` | Top-up receipt `TOP1001`: Amount Added `Rs. 5,000.00`, Fee `Rs. 0.00`, New Balance `Rs. 5,000.00` | |
| TC-16 | `3` -> `W1001` / `100` / source `2` / confirm `N` | `Top-up cancelled. No money was added.` Wallet list still shows W1001 with `Rs. 5,000.00` | |
| TC-17 | Amount `abc`, `-50`, `0`, `50001`, `250.555` | Each is rejected with an `[INVALID INPUT]` message (limits: Rs. 1.00 to Rs. 50,000.00, at most 2 decimals) and the amount is asked again | |

## D. Transfers

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-18 | `4` -> `W1001` -> `W1002` / `2000` | Receipt: Fee `Rs. 20.00`, Total Debited `Rs. 2,020.00`, Sender Balance `Rs. 2,980.00`, Receiver Balance `Rs. 2,000.00` | |
| TC-19 | `4` -> `W1002` -> `W1001` / `3000` | `[ERROR] Insufficient funds in wallet W1002. Available: Rs. 2,000.00, required: Rs. 3,030.00 (amount Rs. 3,000.00 + fee Rs. 30.00).` Balances unchanged | |
| TC-20 | `4` -> `W1001` -> `W1001` / `100` | `[ERROR] Source and destination wallets must be different.` | |
| TC-21 | `4` -> `W1001` -> `W9999` / `100` | `[ERROR] Wallet W9999 was not found.` | |

## E. Merchant payments

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-22 | `5` -> `W1002` / `Campus Cafe` / `450` | Payment receipt `PAY1004`: Amount Paid `Rs. 450.00`, New Balance `Rs. 1,550.00` | |
| TC-23 | `5` -> `W1002` / `Campus Cafe` / `5000` | `[ERROR] Insufficient funds in wallet W1002. Available: Rs. 1,550.00, required: Rs. 5,000.00.` | |

## F. Refunds

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-24 | `6` -> `W1002` -> `PAY1004` | Refund receipt `REF1005`: Original Payment `PAY1004`, Refund Amount `Rs. 450.00`, New Balance `Rs. 2,000.00` | |
| TC-25 | `6` -> `W1002` -> `PAY1004` again | The list shows `REFUNDED`; then `[ERROR] Payment PAY1004 has already been refunded.` | |
| TC-26 | `6` -> `W1002` -> `PAY9999` | `[ERROR] Payment PAY9999 was not found.` | |
| TC-27 | `6` -> `W1001` | `[ERROR] Wallet W1001 has no payments to refund.` | |

## G. Statements

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-28 | `7` -> `W1001` | 3 entries in time order: TOP-UP credit 5,000.00 (balance 5,000.00); TRANSFER OUT debit 2,000.00 (balance 3,000.00); FEE debit 20.00 (balance 2,980.00). Total Credits `Rs. 5,000.00`, Total Debits `Rs. 2,020.00`, Closing Balance `Rs. 2,980.00` | |
| TC-29 | `7` -> `W1002` | 3 entries: TRANSFER IN credit 2,000.00 (balance 2,000.00); PAYMENT debit 450.00 (balance 1,550.00); REFUND credit 450.00 (balance 2,000.00). Closing Balance `Rs. 2,000.00` | |
| TC-30 | `7` -> `W1003` | `No transactions yet.` and Closing Balance `Rs. 0.00` | |

## H. Small transfer and exit

| ID | Steps / input | Expected result | Actual result |
|---|---|---|---|
| TC-31 | `4` -> `W1002` -> `W1001` / `500` | Fee `Rs. 0.00` (up to Rs. 1,000 is free), Sender Balance `Rs. 1,500.00`, Receiver Balance `Rs. 3,480.00` | |
| TC-32 | `8` | `Thank you for using Wallet++. Goodbye!` and the program ends | |
| TC-33 | (optional) At the menu press `Ctrl+Z`, then Enter | `Input ended. Exiting Wallet++.` - no crash | |

## Automated tests that cover the same rules

| Rule | Automated test (run `test.bat`) |
|---|---|
| Top-up only after confirmation / cancel changes nothing | `WalletServiceTests` - "Top-up (needs confirmation)" |
| Transfer fee, both wallets updated, insufficient funds changes nothing | `WalletServiceTests` - "Transfers" |
| Payments reduce balance and create entries | `WalletServiceTests` - "Merchant payments" |
| Refund only for a valid, not-yet-refunded payment of the same wallet | `WalletServiceTests` - "Refunds" |
| Every transaction creates an entry; running balance is correct | `WalletServiceTests` - "Statements" |
| Input rules and limits | `ValidatorTests` |
| Menu, validation re-prompts, clean exit | `ConsoleFlowTests` |
