package com.example.bankapp.Model;

import java.math.BigDecimal;

public class Bill {

    private String name;
    private BigDecimal amount;
    private Long accountNumber;

    public Bill() {
    }

    public Bill(String accountNumber, String amount, String name) {
        this.name = name;
        this.amount = new BigDecimal(amount);
        this.accountNumber = Long.getLong(accountNumber);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }
}