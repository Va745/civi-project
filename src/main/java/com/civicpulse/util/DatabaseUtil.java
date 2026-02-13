package com.civicpulse.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    static {
        try {
            loadProperties();
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private static void loadProperties() throws IOException {
        Properties props = new Properties();
        InputStream input = DatabaseUtil.class.getClassLoader()
                .getResourceAsStream("config.properties");
        
        if (input == null) {
            throw new IOException("Unable to find config.properties");
        }
        
        props.load(input);
        DB_URL = props.getProperty("db.url");
        DB_USERNAME = props.getProperty("db.username");
        DB_PASSWORD = props.getProperty("db.password");
        DB_DRIVER = props.getProperty("db.driver");
        input.close();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
