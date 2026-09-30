/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;

/**
 * Simple holder for what a teller is allowed to do.
 * @author Rara
 */





public class TellerPermissions {

    private final boolean accountCreation;
    private final boolean cashDeposits;
    private final boolean accountTermination;
    private final boolean fundTransfers;
    private final boolean cashWithdrawals;

    public TellerPermissions(boolean accountCreation, boolean cashDeposits,
            boolean accountTermination, boolean fundTransfers, boolean cashWithdrawals) {
        this.accountCreation = accountCreation;
        this.cashDeposits = cashDeposits;
        this.accountTermination = accountTermination;
        this.fundTransfers = fundTransfers;
        this.cashWithdrawals = cashWithdrawals;
    }

    /** Used when testing Tellerframe directly (no login). */
    public static TellerPermissions allGranted() {
        return new TellerPermissions(true, true, true, true, true);
    }

    public boolean canAccountCreation()    { return accountCreation; }
    public boolean canCashDeposits()       { return cashDeposits; }
    public boolean canAccountTermination() { return accountTermination; }
    public boolean canFundTransfers()      { return fundTransfers; }
    public boolean canCashWithdrawals()    { return cashWithdrawals; }
    
    
}
