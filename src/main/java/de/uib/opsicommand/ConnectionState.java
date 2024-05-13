/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

public class ConnectionState {
	public static final int UNDEFINED = 0;
	public static final int NOT_CONNECTED = 1;
	public static final int STARTED_CONNECTING = 2;
	public static final int CONNECTED = 3;
	public static final int RETRY_CONNECTION = 4;
	public static final int CLOSING = 5;
	public static final int INTERRUPTED = 6;
	public static final int UNAUTHORIZED = 7;
	public static final int ERROR = 10;

	public static final ConnectionState ConnectionUndefined = new ConnectionState(UNDEFINED, "not initialized");
	private static int instancesCount;

	private int myState = NOT_CONNECTED;

	private String message = "";

	/**
	 * constructor
	 */
	public ConnectionState(int state, String message) {
		myState = state;
		this.message = message;
		instancesCount++;
	}

	/**
	 * constructor
	 */
	public ConnectionState(int state) {
		this(state, "");
	}

	/**
	 * constructor
	 */
	public ConnectionState() {
		this(UNDEFINED, "");
	}

	/**
	 * get count of instances
	 */
	public static int getInstancesCount() {
		return instancesCount;
	}

	/**
	 * Get Connection state
	 *
	 * @return Connection state
	 */
	public int getState() {
		return myState;
	}

	/**
	 * Get Connection state
	 *
	 * @return Message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the Connection State String Value
	 * 
	 * @return Connection State
	 */
	@Override
	public String toString() {
		return switch (myState) {
		case UNDEFINED -> "Undefined state";
		case INTERRUPTED -> "Interrupted ";
		case NOT_CONNECTED -> "Not connected";
		case STARTED_CONNECTING -> "Started connecting";
		case CONNECTED -> "Connected";
		case RETRY_CONNECTION -> "Reconnecting";
		case UNAUTHORIZED -> "Unauthorized";
		case CLOSING -> "Closing";
		case ERROR -> "Error";
		default -> "UNKNOWN State";
		};
	}
}
