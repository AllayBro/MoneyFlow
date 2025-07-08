package com.moneyflow.models;

public class Account {
    private int id;
    private int userId;
    private double balance;
    private String currency;
    private String accountNumber; // NEW

    public Account() {}

    public Account(int id, int userId, double balance, String currency, String accountNumber) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    // геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
