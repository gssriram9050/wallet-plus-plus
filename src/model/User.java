package model;

/** A person who uses Wallet++. A user may own one Wallet (aggregation). */
public class User {

    private final String userId;
    private String name;
    private String phone;
    private String email;
    private Wallet wallet; // null until the user creates a wallet

    public User(String userId, String name, String phone, String email) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
    public boolean hasWallet() { return wallet != null; }

    @Override
    public String toString() { return userId + " (" + name + ")"; }
}
