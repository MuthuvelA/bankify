package org.example;

import org.example.DatabaseConnection;

import java.sql.*;
import java.util.Date;

public class Account {
    private String username;  // Assume you have a method to get the username
    private String accountno;
    private User authenticatedUser;
    private double balance;

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        this.username = authenticatedUser.getUsername();
    }

    public void setAccountno(String accountno) {
        this.accountno = accountno;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            updateDatabaseBalance();
            logTransaction("DEPOSIT", amount);
            System.out.println("Deposit of $" + amount + " successful. New balance: $" + balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            updateDatabaseBalance();
            logTransaction("WITHDRAWAL", amount);
            System.out.println("Withdrawal of $" + amount + " successful. New balance: $" + balance);
        } else {
            System.out.println("Invalid withdrawal amount or insufficient funds.");
        }
    }

    public void transfer(String recipientAccountNo, double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            depositToAccount(recipientAccountNo, amount);
            updateDatabaseBalance();
            logTransaction("TRANSFER", amount);
            System.out.println("Transfer of $" + amount + " to account " + recipientAccountNo +
                    " successful. New balance: $" + balance);
        } else {
            System.out.println("Invalid transfer amount or insufficient funds.");
        }
    }

    private void depositToAccount(String recipientAccountNo, double amount) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, recipientAccountNo);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private void updateDatabaseBalance() {
        if (authenticatedUser != null && authenticatedUser.getAccountno() != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE accounts SET balance = ? WHERE account_number = ? AND username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setDouble(1, balance);
                    preparedStatement.setString(2, authenticatedUser.getAccountno());
                    preparedStatement.setString(3, authenticatedUser.getUsername());
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Error: Authenticated user or account number not set.");
        }
    }

    private void logTransaction(String transactionType, double amount) {
        if (authenticatedUser != null && authenticatedUser.getAccountno() != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String insertQuery = "INSERT INTO transactions (transaction_type, amount, transaction_date,username,accountno) " +
                        "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, transactionType);
                    preparedStatement.setDouble(2, amount);
                    preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
                    preparedStatement.setString(4, username);
                    preparedStatement.setString(5, accountno);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Error: Authenticated user or account number not set.");
        }
    }


    public void transaction() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectQuery = "SELECT * FROM transactions WHERE accountno = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, accountno);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    System.out.println("Transaction History for Account " + accountno + ":");
                    System.out.println("-------------------------------------------------------");
                    System.out.printf("%-20s%-20s%-20s%-30s\n", "Transaction ID", "Type", "Amount", "Date");
                    System.out.println("-------------------------------------------------------");

                    while (resultSet.next()) {
                        int transactionId = resultSet.getInt("transaction_id");
                        String transactionType = resultSet.getString("transaction_type");
                        double amount = resultSet.getDouble("amount");
                        Timestamp transactionDate = resultSet.getTimestamp("transaction_date");

                        System.out.printf("%-20d%-20s%-20.2f%-30s\n", transactionId, transactionType, amount, transactionDate);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
