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
		String state = null;

		switch (myState) {
		case UNDEFINED:
			state = "Undefined state";
			break;
		case INTERRUPTED:
			state = "Interrupted ";
			break;
		case NOT_CONNECTED:
			state = "Not connected";
			break;
		case STARTED_CONNECTING:
			state = "Started connecting";
			break;
		case CONNECTED:
			state = "Connected";
			break;
		case RETRY_CONNECTION:
			state = "Reconnecting";
			break;
		case UNAUTHORIZED:
			state = "Unauthorized";
			break;
		case CLOSING:
			state = "Closing";
			break;
		case ERROR:
			state = "Error";
			break;
		default:
			state = "UNKNOWN State";
			break;
		}

		return state;
	}
}
