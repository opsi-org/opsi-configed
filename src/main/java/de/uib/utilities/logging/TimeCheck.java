package de.uib.utilities.logging;

public class TimeCheck {
	Object caller;
	String mesg;
	long startmillis;
	static int loglevel = logging.LEVEL_NOTICE;
	static final int maxShown = 500;

	private static String shorten(String s) {
		String result = "";
		if (s != null) {
			if (s.length() >= maxShown && loglevel < logging.LEVEL_DEBUG) {
				result = s.substring(0, maxShown);
				result = result + " ... ";
			} else
				result = s;
		}

		return result;
	}

	private TimeCheck(Object caller, int loglevel, String mesg) {
		this.caller = caller;
		this.mesg = shorten(mesg);
		this.loglevel = loglevel;
	}

	public TimeCheck(Object caller, String mesg) {
		this(caller, logging.LEVEL_NOTICE, mesg);
	}

	public TimeCheck start() {
		startmillis = System.currentTimeMillis();
		logging.log(caller, loglevel, " ------  started: " + mesg + " "); // + startmillis);
		return this;
	}

	public void stop() {
		stop(null);
	}

	public void stop(String stopMessage) {
		String info = stopMessage;
		if (stopMessage == null)
			info = mesg;
		long endmillis = System.currentTimeMillis();
		logging.log(caller, loglevel, " ------  stopped: " + info + " "); // + endmillis);
		logging.log(caller, loglevel, " ======  diff " + (endmillis - startmillis) + " ms  (" + info + ")");
	}
}
