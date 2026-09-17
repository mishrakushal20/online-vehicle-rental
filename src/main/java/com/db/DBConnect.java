package com.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides database connections for the app.
 *
 * The original version of this class kept a single static Connection shared
 * by every request. That is not safe: two requests running on two threads
 * at the same time would run their queries on the very same Connection and
 * corrupt each other's results (or throw SQLExceptions once the connection
 * was closed by one of them).
 *
 * This version uses a HikariCP connection pool, and hands out one pooled
 * connection per request thread via a ThreadLocal. Different requests never
 * share a Connection anymore, but code that calls getConnection() several
 * times within the same request (several DAOs are commonly constructed in
 * one servlet/JSP) still gets back the same, already-open connection, so no
 * other code needed to change.
 *
 * DBConnectionFilter (mapped to /*) returns the thread's connection to the
 * pool at the end of every request by calling closeThreadConnection().
 */
public class DBConnect {

	private static final DataSource dataSource;

	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

	static {
		Properties props = loadProperties();

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(props.getProperty("db.url"));
		config.setUsername(props.getProperty("db.username"));
		config.setPassword(props.getProperty("db.password"));
		config.setDriverClassName("com.mysql.cj.jdbc.Driver");
		config.setMaximumPoolSize(20);
		config.setMinimumIdle(2);
		config.setConnectionTimeout(10_000);
		config.setPoolName("VehicleRentalPool");

		dataSource = new HikariDataSource(config);
	}

	private DBConnect() {
	}

	private static Properties loadProperties() {
		Properties props = new Properties();

		try (InputStream in = DBConnect.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Environment variables (or -D system properties) take priority over
		// db.properties, so real credentials never have to live in source control.
		overrideFromEnv(props, "db.url", "DB_URL");
		overrideFromEnv(props, "db.username", "DB_USERNAME");
		overrideFromEnv(props, "db.password", "DB_PASSWORD");

		return props;
	}

	private static void overrideFromEnv(Properties props, String propertyKey, String envVarName) {
		String value = System.getProperty(propertyKey, System.getenv(envVarName));
		if (value != null) {
			props.setProperty(propertyKey, value);
		}
	}

	/**
	 * Returns the current request's connection, borrowing a fresh one from the
	 * pool the first time it's called during a request.
	 */
	public static Connection getConnection() {
		Connection conn = threadConnection.get();
		try {
			if (conn == null || conn.isClosed()) {
				conn = dataSource.getConnection();
				threadConnection.set(conn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * Returns the current request's connection to the pool. Called by
	 * DBConnectionFilter after every request completes; not meant to be called
	 * directly from servlets/JSPs.
	 */
	public static void closeThreadConnection() {
		Connection conn = threadConnection.get();
		if (conn != null) {
			try {
				conn.close(); // Hikari's close() returns it to the pool, it doesn't drop the socket.
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				threadConnection.remove();
			}
		}
	}
}
