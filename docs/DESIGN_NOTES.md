# Design Notes and Viva Quick Reference

## Packages and classes

| Package | Class | Responsibility |
|---|---|---|
| `main` | `WalletPlusPlusApp` | Shows the menu, reads choices, calls the service, prints receipts and statements. Catches all exceptions so the program never crashes. |
| `model` | `User` | Name, phone, email, optional `Wallet` (aggregation). |
| `model` | `Wallet` | Balance + statement list. Only `credit()` / `debit()` change the balance, and both add a `StatementEntry`. |
| `model` | `Transaction` (abstract) | Common data of all transactions (ID, amount, fee, time, status) and the receipt layout. |
| `model` | `TopUp`, `Transfer`, `Payment`, `Refund` | Child classes of `Transaction`; each gives its own `getType()` and receipt details. |
| `model` | `StatementEntry` | One statement line: credit, debit and running balance. |
| `service` | `WalletService` | All business rules (confirmation, balance checks, fees, valid-payment check for refunds). |
| `util` | `Validator`, `InputUtil` | Input rules; reading input and asking again on errors. |
| `util` | `FeeCalculator`, `Format` | Fee rule; money/date/table formatting. |
| `exception` | 7 custom exceptions | `WalletException` is the parent of the other six. |

## What happens in a transfer (flow)

1. `WalletPlusPlusApp.transfer()` reads source ID, destination ID and amount (invalid text is asked again).
2. `WalletService.transfer()` checks: amount range -> both wallets exist -> wallets are different -> `FeeCalculator` computes the fee -> balance is enough for **amount + fee**. Any failure throws a custom exception and **nothing is changed**.
3. Source wallet `debit()` (amount), then `debit()` again for the fee (a separate `FEE` entry), destination wallet `credit()`.
4. A `Transfer` object is created with the resulting balances; its `getReceipt()` is printed.

## Viva - short answers

| Question | Answer |
|---|---|
| Why is `Transaction` abstract? | A plain "transaction" never exists on its own; only top-ups, transfers, payments and refunds do. The abstract class holds the shared parts and forces each child to define `getType()` and `getDetailLines()`. |
| Where is polymorphism? | `getReceipt()` calls the overridden `getType()` and `getDetailLines()` on a `Transaction` reference, so the receipt matches the real type (the tests keep all four in one `List<Transaction>`). |
| Where is encapsulation? | Private fields with getters/setters; the wallet balance is changed only by `credit()` / `debit()`; `getStatement()` returns an unmodifiable list. |
| Where is aggregation? | `User` has a `Wallet`; `Refund` refers to a `Payment`; `Transfer` refers to two wallets. A `Wallet` owns its `StatementEntry` list (composition). |
| Why `BigDecimal` and not `double`? | `double` cannot store values such as 0.1 exactly, so balances could drift. `BigDecimal` keeps exact 2-decimal money values. |
| How is "top-up after confirmation" done? | `requestTopUp()` creates a `PENDING` top-up without touching the balance; `confirmTopUp()` credits the wallet only after the user answers `Y`. |
| How do you guarantee every transaction creates a statement entry? | The balance can only change through `Wallet.credit()` / `Wallet.debit()`, and both add an entry. There is no other way to change it. |
| What makes a refund "valid"? | The payment exists, was made from that wallet, and has not been refunded before. |
| Which custom exceptions did you make? | `WalletException` (parent), `InvalidInputException`, `InvalidAmountException`, `InsufficientFundsException`, `RecordNotFoundException`, `DuplicateRecordException`, `RefundNotAllowedException`. |
| How does the program avoid crashing on bad input? | `InputUtil` catches format errors and asks again; the menu loop catches every `WalletException`; end of input is handled too. |
| What is the transfer fee? | Up to Rs. 1,000 free; above that 1% paid by the sender (my own assumption, because the specification only says "fees (if any)"). |
| What would you add next? | Saving data to a file, a PIN for each wallet, and partial refunds. |
