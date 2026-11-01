/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owner: Hữu Lộc.
 * Role: JDBC connection configuration used by DAOs.
 * Source: src/main/java/db/DBContext.java.
 */
public class DBContext {

    private Connection conn;
    private final String DB_URL = setting(
        "CINEMA_DB_URL",
        "jdbc:sqlserver://127.0.0.1:1433;databaseName=CinemaStreamingDB;encrypt=false"
    );
    private final String DB_USER = requiredSetting("CINEMA_DB_USER");
    private final String DB_PWD = requiredSetting("CINEMA_DB_PASSWORD");

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String requiredSetting(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Thiếu cấu hình biến môi trường " + name
            );
        }
        return value.trim();
    }

    public DBContext() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
    }

    /** Owner: Hữu Lộc. Purpose: retrieve connection. */
    public Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                this.conn = DriverManager.getConnection(
                    DB_URL,
                    DB_USER,
                    DB_PWD
                );
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(
                Level.SEVERE,
                null,
                ex
            );
        }
        return conn;
    }
}
