package com.example.bankapp.Model;

import java.math.BigDecimal;

public class Account {

    private String accountType;
    private BigDecimal balance;
    private  boolean accountActive;


    public Account() {
    }

    public Account(String accountType, BigDecimal balance, boolean accountActive) {
        this.accountType = accountType;
        this.balance = balance;
        this.accountActive = accountActive;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isAccountActive() {
        return accountActive;
    }

    public void setAccountActive(boolean accountActive) {
        this.accountActive = accountActive;
    }
}
