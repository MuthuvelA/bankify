package org.example;

import java.sql.*;
import java.util.Date;



public class Transaction {
    private static String username;
    private static User authenticatedUser;
    private static String recipient = "NULL";
    public static String temp;


    public static void logTransaction(String transactionType, double amount, String accountNo) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (accountNo != null) {
                String insertQuery = "INSERT INTO transactions (transaction_type, amount, transaction_date, username, accountno, recipientaccountno) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, transactionType);
                    preparedStatement.setDouble(2, amount);
                    preparedStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
                    preparedStatement.setString(4, username);
                    preparedStatement.setString(5, accountNo);
                    preparedStatement.setString(6, recipient);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Error: Account number not set.");
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void handleSQLException(SQLException e) {
        System.out.println("An error occurred: " + e.getMessage());
    }
    public static void displaytrans(String accountno) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String selectQuery = "SELECT * FROM transactions WHERE accountno = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, accountno);
                temp=accountno;
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    print();
                    while (resultSet.next()) {
                        int transactionId = resultSet.getInt("transaction_id");
                        String transactionType = resultSet.getString("transaction_type");
                        double amount = resultSet.getDouble("amount");
                        Timestamp transactionDate = resultSet.getTimestamp("transaction_date");
                        System.out.printf("| %-20d | %-20s | %-20.2f | %-30s |\n",
                                transactionId, transactionType, amount, transactionDate);
                    }
                    System.out.println("------------------------------------------------------------------------------------------------------");}
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
    public static void print(){
        System.out.println("Transaction History for Account " + temp + ":");
        System.out.println("------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-20s | %-20s | %-20s | %-30s |\n", "Transaction ID", "Type", "Amount", "Date");
        System.out.println("------------------------------------------------------------------------------------------------------");
    }



}
