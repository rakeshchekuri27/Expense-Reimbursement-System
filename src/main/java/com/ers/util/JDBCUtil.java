package com.ers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCUtil {
    private static final Logger log = LoggerFactory.getLogger(JDBCUtil.class);
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                log.error("db.properties was not found on the classpath");
                throw new RuntimeException("db.properties not found on the classpath");
            }
            props.load(in);
        } catch (IOException e) {
            log.error("Could not load db.properties", e);
            throw new RuntimeException("Could not load db.properties", e);
        }
        URL = props.getProperty("jdbc.url");
        USER = props.getProperty("jdbc.user");
        PASSWORD = props.getProperty("jdbc.password");
        if (URL == null || URL.isBlank() || USER == null || USER.isBlank()) {
            log.error("db.properties is missing jdbc.url or jdbc.user");
            throw new RuntimeException("db.properties is missing jdbc.url or jdbc.user");
        }
        log.info("Database configuration loaded for {}", URL);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
