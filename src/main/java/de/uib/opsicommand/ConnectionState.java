package de.uib.opsicommand;

import de.uib.utilities.logging.Logging;

public class ConnectionState {
	public static final int UNDEFINED = 0;
	public static final int NOT_CONNECTED = 1;
	public static final int STARTED_CONNECTING = 2;
	public static final int CONNECTED = 3;
	public static final int RETRY_CONNECTION = 4;
	public static final int CLOSING = 5;
	public static final int INTERRUPTED = 6;
	public static final int ERROR = 10;
	protected int myState = NOT_CONNECTED;

	public static final ConnectionState ConnectionUndefined = new ConnectionState(UNDEFINED, "not initialized");

	protected String message = "";

	private static int instancesCount = 0;

	/**
	 * constructor
	 */
	public ConnectionState(int state, String message) // throws UnknownStateException, to implement
	{
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

	@Override
	public boolean equals(Object state) {
		if (state instanceof Integer)
			return (myState == (Integer) state);

		else if (state instanceof ConnectionState)
			return (myState == ((ConnectionState) state).getState());

		else
			return false;
	}

	/**
	 * Returns the Connection State String Value
	 * 
	 * @return Connection State
	 */
	@Override
	public String toString() {
		switch (myState) {
		case UNDEFINED:
			return "Undefined state";
		case INTERRUPTED:
			return "Interrupted ";
		case NOT_CONNECTED:
			return "Not connected";
		case STARTED_CONNECTING:
			return "Started connecting";
		case CONNECTED:
			return "Connected";
		case RETRY_CONNECTION:
			return "Reconnecting";
		case CLOSING:
			return "Closing";
		case ERROR:
			return "Error";
		default:
			return "UNKNOWN State";
		}
	}

	public void waitForConnection(int timeoutSecs) {

		int waitMs = 200;
		int divisorSeconds = 5;
		int countWait = 0;

		int secsWaited = 0;

		while (getState() != CONNECTED && (timeoutSecs == 0 || secsWaited < timeoutSecs)) {
			try {
				countWait++;
				Thread.sleep(waitMs);
				Logging.debug(this, "countWait " + countWait + " waited, thread " + Thread.currentThread().getName()
						+ " " + this.toString());
				secsWaited = countWait / divisorSeconds;
			} catch (InterruptedException ex) {
				Logging.info(this, "Thread interrupted exception: " + ex);
				Thread.currentThread().interrupt();
			}
		}
	}

}
