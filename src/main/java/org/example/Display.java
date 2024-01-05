package org.example;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Display {
    private final Connection dbConnection;
    private static User authenticatedUser;
    private Account acc;
    private Scanner s;

    public Display(Connection dbConnection) {
        this.dbConnection = dbConnection;
        this.s = new Scanner(System.in);
        this.acc=new Account();
    }
    public void start() {
        System.out.println("+--------------------------+");
        System.out.println("|       Choose an Option   |");
        System.out.println("+--------------------------+");
        System.out.println("| 1. Log in                |");
        System.out.println("| 2. Create a new account  |");
        System.out.println("+--------------------------+");
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
            acc.setAuthenticatedUser(authenticatedUser);
            acc.setAccountno(authenticatedUser.getAccountno());
            showOptions();
        } else {
            System.out.println("Invalid username or password. Exiting...");
            start();
        }
    }
    private void createAccount() {
        System.out.print("Enter a new username: ");
        String newUsername = s.next();
        System.out.print("Enter a new password: ");
        String newPassword = s.next();
        System.out.print("Enter your full name: ");
        String fullname = s.next();
        System.out.print("Enter your mail: ");
        String mail = s.next();
        System.out.print("Enter your phoneno: ");
        String phoneno = s.next();
        System.out.print("Enter your account number: ");
        String accountno = s.next();
        if (insertNewUser(newUsername, newPassword, fullname, mail, phoneno, accountno)) {
            System.out.println("Account created successfully! You can now log in.");
            start();
        }
        else {
            System.out.println("Failed to create an account. Exiting...");
        }
    }
    private boolean insertNewUser(String username, String password, String fullname, String mail, String phoneno, String accountno) {
        String query = "INSERT INTO user (username, encrypted_password, full_name, email, phoneno, account_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptPassword(password));
            preparedStatement.setString(3, fullname);
            preparedStatement.setString(4, mail);
            preparedStatement.setString(5, phoneno);
            preparedStatement.setString(6, accountno);
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
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            resultSet = executeQuery(query, username, connection);

            if (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setEncryptedPassword(resultSet.getString("encrypted_password"));
                user.setAccountno(resultSet.getString("account_number"));
                String decryptedPassword = decryptPassword(user.getEncryptedPassword());
                if (decryptedPassword.equals(password)) {
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            closeResultSet(resultSet);
            closeConnection(connection);
        }

        return null;
    }


    private ResultSet executeQuery(String query, String username, Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, username);
        return preparedStatement.executeQuery();
    }

    private void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
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

    private void displaymenu(){
        System.out.println("+-------------------------------------+");
        System.out.println("|               MENU                  |");
        System.out.println("+-------------------------------------+");
        System.out.println("| 1. Check Account Balance            |");
        System.out.println("| 2. Withdraw Amount                  |");
        System.out.println("| 3. Deposit Amount                   |");
        System.out.println("| 4. Initiate Fund Transfer           |");
        System.out.println("| 5. Review Recent Transactions       |");
        System.out.println("| 6. Change Password                  |");
        System.out.println("| 7. Logout                           |");
        System.out.println("| 8. Exit                             |");
        System.out.println("+-------------------------------------+");
    }
    private void showOptions() {
        displaymenu();
        Account acc = new Account();
        boolean flag = true;
        while (flag) {
            System.out.println("Enter your choice:");
            int ch = s.nextInt();
            switch (ch) {
                case 1:
                    System.out.println("Your account balance is " + acc.getBalance());
                    break;
                case 2:
                    System.out.println("Enter the amount to be withdrawn:");
                    double amoun=s.nextDouble();
                    acc.withdraw(amoun);
                    System.out.println("Amount withdrawn successfully");
                    break;
                case 3:
                    System.out.println("Enter the account number to deposit:");
                    String deponu=s.next();
                    System.out.println("Enter the amount to be deposited:");
                    double amnt=s.nextDouble();
                    acc.deposit(amnt,deponu);
                    System.out.println("Amount deposited successfully");
                    break;
                case 4:
                    System.out.println("Enter the account no:");
                    String receiver = s.next();
                    System.out.println("Re-enter the account no:");
                    String receiverc = s.next();
                    if (receiver.equals(receiverc)) {
                        System.out.println("Enter the amount to be transferred: ");
                        double amount = s.nextDouble();
                        acc.transfer(receiver, amount);
                    } else {
                        System.out.println("!! ACCOUNT NUMBER NOT MATCHED !!");
                    }
                    break;
                case 5:
                    acc.displaytransaction(authenticatedUser.getAccountno());
                    break;
                case 6:
                    System.out.println("Enter your username to change password:");
                    String changepassuser = s.next();
                    System.out.println("Enter your current password:");
                    String currpass = s.next();
                    System.out.print("Enter your new password: ");
                    String newPassword = s.next();
                    s.nextLine();

                    changePassword(changepassuser, currpass, newPassword);
                    break;
                case 7:
                    logOut();
                    flag = false;
                    break;
                case 8:
                    System.out.println("EXITING!!");
                    flag = false;
                    break;
                default:
                    System.out.println("INVALID CHOICE!");
            }
        }
    }

    private void logOut() {
        authenticatedUser = null;
        System.out.println("Logout successful. Returning to the login screen.");
        start();
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

    private String encryptPassword(String password) {
        int key = 3;
        StringBuilder encryptedPassword = new StringBuilder();

        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                char shifted = (char) (ch + key);
                if ((Character.isUpperCase(ch) && shifted > 'Z') || (Character.isLowerCase(ch) && shifted > 'z')) {
                    shifted = (char) (ch - (26 - key));
                }
                encryptedPassword.append(shifted);
            } else {
                encryptedPassword.append(ch);
            }
        }
        return encryptedPassword.toString();
    }

    private String decryptPassword(String encryptedPassword) {
        int key = 3;
        StringBuilder decryptedPassword = new StringBuilder();

        for (char ch : encryptedPassword.toCharArray()) {
            if (Character.isLetter(ch)) {
                char shifted = (char) (ch - key);
                if ((Character.isUpperCase(ch) && shifted < 'A') || (Character.isLowerCase(ch) && shifted < 'a')) {
                    shifted = (char) (ch + (26 - key));
                }
                decryptedPassword.append(shifted);
            } else {
                decryptedPassword.append(ch);
            }
        }
        return decryptedPassword.toString();
    }
    public void changePassword(String username, String currpass, String newpass) {
        if (checkpass(username, currpass)) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE user SET encrypted_password = ? WHERE username = ? ";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, encryptPassword(newpass));
                    preparedStatement.setString(2, username);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Password changed successfully!");
                    } else {
                        System.out.println("Failed to change password. Please try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Invalid current password. Password not changed.");
        }
    }

    private boolean checkpass(String username, String password) {
        String query = "SELECT * FROM user WHERE username = ? AND encrypted_password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, encryptPassword(password));
            System.out.println(" *****"+encryptPassword(password)+"*******");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }



}