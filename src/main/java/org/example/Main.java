package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("****WELCOME TO THE BANKIFY CONSOLE APPLICATION!****");
        try (Connection dbConnection = DatabaseConnection.getConnection()) {
            Display display = new Display(dbConnection);
            display.start();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
