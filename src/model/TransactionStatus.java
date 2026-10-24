package model;

/** The state of a transaction. Only top-ups are PENDING (waiting for the user's confirmation). */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    CANCELLED
}
