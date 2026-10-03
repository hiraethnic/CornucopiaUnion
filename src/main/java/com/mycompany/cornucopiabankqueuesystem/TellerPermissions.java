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