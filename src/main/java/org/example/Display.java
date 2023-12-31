// Display.java
package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Display {
    private Connection dbConnection;
    private User authenticatedUser;
    private Scanner s;

    public Display(Connection dbConnection) {
        this.dbConnection = dbConnection;
        this.s = new Scanner(System.in);
    }

    public void start() {
        // Display menu
        System.out.println("1. Log in");
        System.out.println("2. Create a new account");
        System.out.print("Choose an option: ");
        int option = s.nextInt();

        switch (option) {
            case 1:
                logIn();
                break;
            case 2:
                createAccount();
                break;
            default:
                System.out.println("Invalid option. Exiting...");
        }

        closeConnection();
        s.close();
    }

    private void logIn() {

        System.out.print("Enter your username: ");
        String username = s.next();

        System.out.print("Enter your password: ");
        String password = s.next();

        authenticatedUser = authenticateUser(username, password);

        if (authenticatedUser != null) {
            System.out.println("Login successful! Welcome, " + authenticatedUser.getUsername() + ".");
            showOptions();
        } else {
            System.out.println("Invalid username or password. Exiting...");
            start();
        }
    }

    private void createAccount() {
        // Get new user details
        System.out.print("Enter a new username: ");
        String newUsername = s.next();

        System.out.print("Enter a new password: ");
        String newPassword = s.next();

        System.out.print("Enter your full name: ");
        String fullname = s.next();

        System.out.print("Enter your mail: ");
        String mail = s.next();

        if (insertNewUser(newUsername, newPassword, fullname, mail)) {
            System.out.println("Account created successfully! You can now log in.");
            System.out.println("For login page press 1:");
            int n=s.nextInt();
            if(n==1){
                logIn();
            }
        } else {
            System.out.println("Failed to create an account. Exiting...");
        }
    }

    private boolean insertNewUser(String username, String password, String fullname, String mail) {
        String query = "INSERT INTO user (username, encrypted_password, full_name, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = dbConnection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptPassword(password));
            preparedStatement.setString(3, fullname);
            preparedStatement.setString(4, mail);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    private User authenticateUser(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ?";
        ResultSet resultSet = null;

        try {
            resultSet = executeQuery(query, username);

            if (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setEncryptedPassword(resultSet.getString("encrypted_password"));

                //System.out.println("username: " + user.getUsername());
                //System.out.println("encrypted :" + user.getEncryptedPassword());
                if (decryptPassword(user.getEncryptedPassword()).equals(password)) {
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeResultSet(resultSet);
        }
        return null;
    }

    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                if (!resultSet.isClosed()) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing ResultSet: " + e.getMessage());
            }
        }
    }

    private void showOptions() {
        System.out.println("MENU");

        System.out.println("1. Check Account Balance");
        System.out.println("2. Initiate Fund Transfer");
        System.out.println("3. Review Recent Transactions");
        System.out.println("4. Change Password");
        System.out.println("5. Logout");
        System.out.println("6. Exit");
    }


    private void closeConnection() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private ResultSet executeQuery(String query, String username) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(query);
        preparedStatement.setString(1, username);
        return preparedStatement.executeQuery();
    }


    private String encryptPassword(String password) {
        int shift = 3;
        StringBuilder encryptedPassword = new StringBuilder();

        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                char shifted = (char) (ch + shift);
                if ((Character.isUpperCase(ch) && shifted > 'Z') || (Character.isLowerCase(ch) && shifted > 'z')) {
                    shifted = (char) (ch - (26 - shift));
                }
                encryptedPassword.append(shifted);
            } else {
                encryptedPassword.append(ch);
            }
        }
        return encryptedPassword.toString();
    }

    private String decryptPassword(String encryptedPassword) {
        int shift = 3;
        StringBuilder decryptedPassword = new StringBuilder();

        for (char ch : encryptedPassword.toCharArray()) {
            if (Character.isLetter(ch)) {
                char shifted = (char) (ch - shift);
                if ((Character.isUpperCase(ch) && shifted < 'A') || (Character.isLowerCase(ch) && shifted < 'a')) {
                    shifted = (char) (ch + (26 - shift));
                }
                decryptedPassword.append(shifted);
            } else {
                decryptedPassword.append(ch);
            }
        }
        //System.out.println(decryptedPassword.toString());
        return decryptedPassword.toString();
    }


}