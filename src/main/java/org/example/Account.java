package org.example;

import org.example.DatabaseConnection;

import java.sql.*;
import java.util.Date;

public class Account {
    private static String username;
    private static String accountno;
    private static User authenticatedUser;
    private static User us;
    private static double balance;
    public static String receiptent="NULL";

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        //System.out.println("setauthenticateduser!!!!!"+authenticatedUser);
        this.username = authenticatedUser.getUsername();
    }

    public void setAccountno(String accountno) {
        this.accountno = accountno;
    }

    public double getBalance() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (authenticatedUser != null && authenticatedUser.getAccountno() != null) {
                String selectQuery = "SELECT balance FROM user WHERE account_number = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                    preparedStatement.setString(1, authenticatedUser.getAccountno());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            balance = resultSet.getDouble("balance");
                            return balance;
                        } else {
                            System.out.println("Error: User not found in the database.");
                        }
                    }
                }
            } else {
                System.out.println("Error: Authenticated user or account number not set.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return 0.0;
    }
    public void deposit(double amount, String depono) {
        System.out.println("DEBUG: Authenticated User: " + authenticatedUser);
        System.out.println("DEBUG: Account Number: " + authenticatedUser.getAccountno());

        if (authenticatedUser != null && authenticatedUser.getAccountno() != null) {
            if (amount > 0) {
                balance += amount;
                depositToAccount(depono, amount);
                updateDatabaseBalance();
                logTransaction("DEPOSIT", amount, depono);
                System.out.println("Deposit of $" + amount + " to account " + depono + " successful. New balance: $" + balance);
            } else {
                System.out.println("Invalid deposit amount.");
            }
        } else {
            System.out.println("Error: DEPOSIT METHOD Authenticated user or account number not set.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            updateDatabaseBalance();
            logTransaction("WITHDRAWAL", amount, authenticatedUser.getAccountno());
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
            logTransaction("TRANSFER", amount, recipientAccountNo);
            System.out.println("Transfer of $" + amount + " to account " + recipientAccountNo +
                    " successful. New balance: $" + balance);
        } else {
            System.out.println("Invalid transfer amount or insufficient funds.");
        }
    }

    private void depositToAccount(String recipientAccountNo, double amount) {
        receiptent=recipientAccountNo;
        try (Connection connection = DatabaseConnection.getConnection()) {
            String updateQuery = "UPDATE user SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, recipientAccountNo);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private void updateDatabaseBalance() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (authenticatedUser != null && authenticatedUser.getAccountno() != null) {
                String updateQuery = "UPDATE user SET balance = ? WHERE account_number = ? AND username = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setDouble(1, balance);
                    preparedStatement.setString(2, authenticatedUser.getAccountno());
                    preparedStatement.setString(3, authenticatedUser.getUsername());
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Balance updated successfully.");
                    } else {
                        System.out.println("Failed to update balance. No rows affected.");
                    }
                }
            } else {
                System.out.println("Error: Authenticated user or account number not set.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }


    private void logTransaction(String transactionType, double amount, String accountNo) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (accountNo != null) {
                String insertQuery = "INSERT INTO transactions (transaction_type, amount, transaction_date, username, accountno,recipientaccountno) " +
                        "VALUES (?, ?, ?, ?, ?,?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, transactionType);
                    preparedStatement.setDouble(2, amount);
                    preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
                    preparedStatement.setString(4, username);
                    preparedStatement.setString(5, authenticatedUser.getAccountno());
                    preparedStatement.setString(6, receiptent);
                    preparedStatement.executeUpdate();
                }
            } else {
                System.out.println("Error: Account number not set.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private void handleSQLException(SQLException e) {
        System.out.println("An error occurred: " + e.getMessage());
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
            handleSQLException(e);
        }
    }
}
