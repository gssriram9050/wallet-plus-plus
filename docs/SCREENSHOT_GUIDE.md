# How to Capture Genuine Execution Screenshots

The college submission needs **Eclipse Console** screenshots of the real program running. Take them from your own run and never edit them.

## 1. Run Wallet++ in Eclipse

1. Install **Eclipse IDE for Java Developers** (with a JDK 8 or newer).
2. **File -> Import -> General -> Existing Projects into Workspace** -> **Browse** -> select the `wallet-plus-plus` folder -> **Finish**.
3. Open `src/main/WalletPlusPlusApp.java`, right-click -> **Run As -> Java Application**.
4. Type your input in the **Console** view (Eclipse shows what you type). Double-click the **Console** tab to maximise it, so the 113-character statement table does not wrap.
5. To start again with a clean run, click the red square (stop) and run again; the data is cleared every time.

Without Eclipse you can run `compile.bat` and `run.bat` in Command Prompt and capture that window instead, but the template asks for Eclipse Console screenshots, so use Eclipse for the report.

## 2. Type these values and capture each screen

Use **Win + Shift + S** (Snipping Tool) to select the area of the **Console** view, and save as PNG in the `screenshots` folder with the file name shown below. Crop so that the typed input **and** the output are visible. The report has 14 numbered frames (Figure 1 to 14) in the same order as this table.

Type the values in this order (they match `docs/TEST_CASES.md`). IDs are shown as the program will print them when you start from a fresh run.

| # | File name | What to type | Capture |
|---|---|---|---|
| 01 | `01_menu.png` | (just start the program) | The main menu |
| 02 | `02_add_user.png` | `1` / `Asha Raman` / `9876543210` / `asha@example.com`, then `1` / `Ravi Kumar` / `9123456780` / `ravi@example.com` | Both "User added successfully" blocks (U101, U102) |
| 03 | `03_create_wallet.png` | `2` / `U101`, then `2` / `U102` | Both "Wallet created successfully" blocks (W1001, W1002) |
| 04 | `04_topup_receipt.png` | `3` / `W1001` / `5000` / `1` / `Y` | Confirmation question and the **TOP-UP RECEIPT** (TOP1001, balance 5,000.00) |
| 05 | `05_topup_cancelled.png` | `3` / `W1001` / `100` / `2` / `N` | "Top-up cancelled. No money was added." |
| 06 | `06_transfer_receipt.png` | `4` / `W1001` / `W1002` / `2000` | **TRANSFER RECEIPT** with fee Rs. 20.00 and both resulting balances |
| 07 | `07_insufficient_funds.png` | `4` / `W1002` / `W1001` / `3000` | The `[ERROR] Insufficient funds ...` message |
| 08 | `08_payment_receipt.png` | `5` / `W1002` / `Campus Cafe` / `450` | **PAYMENT RECEIPT** (PAY1004, balance 1,550.00) |
| 09 | `09_refund_receipt.png` | `6` / `W1002` / `PAY1004` | **REFUND RECEIPT** (REF1005, balance 2,000.00) |
| 10 | `10_refund_rejected.png` | `6` / `W1002` / `PAY1004` | The payment list showing REFUNDED and "has already been refunded" |
| 11 | `11_statement_w1001.png` | `7` / `W1001` | **WALLET STATEMENT** - chronological entries, running balance, totals |
| 12 | `12_statement_w1002.png` | `7` / `W1002` | Statement showing transfer in, payment and refund |
| 13 | `13_invalid_input.png` | at the menu type `abc`; then `1` and type `A1` as the name, `12345` as the phone | The `[INVALID INPUT]` messages (the program asks again, it does not crash) |
| 14 | `14_exit.png` | `8` | "Thank you for using Wallet++. Goodbye!" |

Note: transaction numbers (TOP1001, TRF1003, PAY1004, REF1005) come from one shared counter, and the cancelled top-up in step 05 uses TOP1002. If you type extra actions, your numbers will differ - that is fine; just use the numbers shown on your own screen.

## 3. What each expected output of the specification maps to

| Expected output in the specification | Screenshots |
|---|---|
| Transaction receipts with amounts, fees (if any) and resulting balances | 04, 06, 08, 09 |
| Statement with chronological entries and running balance | 11, 12 |
| Validation errors for insufficient funds | 07 (and 13 for other validation) |
| Menu display / validation example (required by the submission template) | 01, 13 |

## 4. Add the screenshots to GitHub

Copy the PNG files into the `screenshots` folder using **exactly** the file names above (the README shows them), then:

```bat
git add screenshots
git commit -m "Add execution screenshots"
git push
```
