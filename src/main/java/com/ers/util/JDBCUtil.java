package com.ers.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtil {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found on the classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not load db.properties", e);
        }
        URL = props.getProperty("jdbc.url");
        USER = props.getProperty("jdbc.user");
        PASSWORD = props.getProperty("jdbc.password");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
