package com.example.bankapp.Model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Account {

    private String accountType;
    private BigDecimal balance;
    private  boolean accountActive;


    public Account() {
    }



    public Account( boolean accountActive, String accountType, String balance) {
        this.accountType = accountType;
        this.balance =  new BigDecimal(balance);
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

    public Double getBalanceAsDouble() {

        Double d = balance.doubleValue();

        return  d;
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
