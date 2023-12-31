package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String user = "root";
    private static final String password = "5476902";
    private static final String url = "jdbc:mysql://localhost:3306/bankify";
    private static final String driver = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {}

    private static Connection dbConnection;

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            dbConnection = DriverManager.getConnection(url, user, password);
        }
        return dbConnection;
    }

    public static void closeConnection() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
