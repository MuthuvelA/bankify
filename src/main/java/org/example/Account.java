package org.example;

public class Account {
    private int id;
    private int userId;
    private double balance;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        // Update balance and log transaction
    }

    public void withdraw(double amount) {
        // ...
    }

    public void transfer(Account recipient, double amount) {
        // ...
    }
}

