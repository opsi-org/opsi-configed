package de.uib.opsicommand;

import java.sql.Connection;

import de.uib.utilities.logging.Logging;

// TODO This method shall be removed since we don't want the configed to connect to the database
public final class DbConnect {

	private DbConnect() {
		Logging.critical(this, "DATABASE NOT SUPPORTED ANY MORE...");
	}

	public static Connection getConnection(String serverParameter, String userParameter, String passwordParameter) {
		Logging.critical("DATABASE NOT SUPPORTED ANY MORE... method getConnection(arg1, arg2, arg3) called");

		return null;
	}

	public static Connection getConnection() {
		Logging.critical("DATABASE NOT SUPPORTED ANY MORE... method getConnection() called");

		return null;
	}
}
