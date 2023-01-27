package de.uib.opsicommand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.uib.utilities.logging.Logging;

public class DbConnect {
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	private String url = "jdbc:mysql://%s";

	// TODO Default values go away!
	private static final String defaultDB = "opsi";
	private static final String defaultUser = "opsi";
	private static final String defaultPassword = "linux123";
	private static Connection con = null;

	private static String server;
	private static String user;
	private static String password;

	private DbConnect() {
		try {
			// TODO kann weg!
			Class.forName(DRIVER).getDeclaredConstructor().newInstance();

		}

		catch (Exception e) {
			Logging.error("Error", e);
			return;
		}

		if (server != null && server.indexOf("/") == -1)
			server = server + "/" + defaultDB;

		url = String.format(url, server);
		Logging.info("db url " + url);

		try {
			con = DriverManager.getConnection(url, user, password);
			con.setAutoCommit(true);
		} catch (SQLException e) {
			Logging.error(this, e.getMessage(), e);
			con = null;

			try {
				con = DriverManager.getConnection(url, defaultUser, defaultPassword);
				con.setAutoCommit(true);
			} catch (SQLException e1) {
				Logging.error(this, e1.getMessage(), e1);
				con = null;
			}
		}
	}

	public static Connection getConnection(String serverParameter, String userParameter, String passwordParameter) {
		if (con == null) {
			if (serverParameter == null)
				server = "localhost";
			else
				server = serverParameter;

			if (userParameter == null)
				user = defaultUser;
			else
				user = userParameter;

			if (passwordParameter == null)
				password = defaultPassword;
			else
				password = passwordParameter;

			new DbConnect();
		}
		return con;
	}

	public static Connection getConnection() {
		if (con == null) {
			Logging.warning("try default connections parameters");

			return getConnection(null, null, null);
		}
		return con;
	}

	public static void closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			Logging.error("DbConnect: " + e.getMessage(), e);
		}
		con = null;
	}

	public static boolean checkForExistence(String sql) {
		Logging.debug("DbConnect: " + sql);
		try (ResultSet reply = getConnection().createStatement().executeQuery(sql)) {
			if (reply.next())
				return true;
		} catch (Exception e) {
			Logging.error("DbConnect: " + e.getMessage(), e);
		}
		return false;
	}
}