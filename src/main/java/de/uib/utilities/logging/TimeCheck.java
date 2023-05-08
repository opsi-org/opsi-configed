package de.uib.utilities.logging;

public class TimeCheck {

	private static int loglevel = Logging.LEVEL_NOTICE;
	private static final int MAX_SHOWN = 500;

	private Object caller;
	private String mesg;
	private long startmillis;

	private TimeCheck(Object caller, int loglevel, String mesg) {
		this.caller = caller;
		this.mesg = shorten(mesg);
		TimeCheck.loglevel = loglevel;
	}

	public TimeCheck(Object caller, String mesg) {
		this(caller, Logging.LEVEL_NOTICE, mesg);
	}

	private static String shorten(String s) {
		String result = "";
		if (s != null) {
			if (s.length() >= MAX_SHOWN && loglevel < Logging.LEVEL_DEBUG) {
				result = s.substring(0, MAX_SHOWN);
				result = result + " ... ";
			} else {
				result = s;
			}
		}

		return result;
	}

	public TimeCheck start() {
		startmillis = System.currentTimeMillis();
		Logging.log(caller, loglevel, "started: " + mesg + " ");
		return this;
	}

	public void stop() {
		stop(null);
	}

	public void stop(String stopMessage) {
		String info = stopMessage;
		if (stopMessage == null) {
			info = mesg;
		}
		long endmillis = System.currentTimeMillis();
		Logging.log(caller, loglevel, "ended (" + (endmillis - startmillis) + " ms): " + info);
	}
}
