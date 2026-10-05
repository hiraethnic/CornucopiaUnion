package com.mycompany.cornucopiabankqueuesystem;

/**
 * What a teller is allowed to do. A teller can ONLY perform the functions
 * ticked for their account.
 * @author Rara
 */
public class TellerPermissions {

    private final boolean accountCreation;
    private final boolean cashDeposits;
    private final boolean billPayments;
    private final boolean fundTransfers;
    private final boolean cashWithdrawals;
    private final boolean foreignExchange;

    /** Who is logged in (set by QueueDatabase.authenticate). */
    private String username = "";
    private String fullName = "";

    public TellerPermissions(boolean accountCreation, boolean cashDeposits,
            boolean billPayments, boolean fundTransfers,
            boolean cashWithdrawals, boolean foreignExchange) {
        this.accountCreation = accountCreation;
        this.cashDeposits = cashDeposits;
        this.billPayments = billPayments;
        this.fundTransfers = fundTransfers;
        this.cashWithdrawals = cashWithdrawals;
        this.foreignExchange = foreignExchange;
    }

    /** Used when testing Tellerframe directly (no login). */
    public static TellerPermissions allGranted() {
        return new TellerPermissions(true, true, true, true, true, true);
    }

    /** Remembers which teller account these permissions belong to. */
    public TellerPermissions withIdentity(String username, String fullName) {
        this.username = username == null ? "" : username;
        this.fullName = fullName == null ? "" : fullName;
        return this;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }

    /** Text saved in the records, e.g. "Juan Dela Cruz (juan01)". */
    public String getDisplayName() {
        if (username.isEmpty()) return "Unknown teller";
        return fullName.isEmpty() ? username : fullName + " (" + username + ")";
    }

    public boolean canAccountCreation() { return accountCreation; }
    public boolean canCashDeposits()    { return cashDeposits; }
    public boolean canBillPayments()    { return billPayments; }
    public boolean canFundTransfers()   { return fundTransfers; }
    public boolean canCashWithdrawals() { return cashWithdrawals; }
    public boolean canForeignExchange() { return foreignExchange; }

    /**
     * True if this teller may serve a ticket of the given category
     * (e.g. "Deposit", "Foreign Exchange"). Unknown categories are denied.
     */
    public boolean canHandleTicket(String ticketNo, String category) {
        if (ticketNo != null) {
            String t = ticketNo.toUpperCase();
            if (t.startsWith("AC-")) return accountCreation;
            if (t.startsWith("FX-")) return foreignExchange;
            if (t.startsWith("WD-")) return cashWithdrawals;
            if (t.startsWith("DP-")) return cashDeposits;
            if (t.startsWith("TR-")) return fundTransfers;
            if (t.startsWith("BP-")) return billPayments;
        }
        return canHandle(category);
    }

    public boolean canHandle(String category) {
        if (category == null) return false;
        String c = category.toLowerCase();
        if (c.contains("account") || c.contains("opening") || c.contains("creation")) return accountCreation;
        if (c.contains("exchange") || c.contains("forex")) return foreignExchange;
        if (c.contains("withdraw")) return cashWithdrawals;
        if (c.contains("deposit")) return cashDeposits;
        if (c.contains("transfer")) return fundTransfers;
        if (c.contains("bill")) return billPayments;
        return false;
    }
}