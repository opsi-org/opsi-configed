/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.logging;

public class TimeCheck {
	private static final int MAX_SHOWN = 500;

	private Object caller;
	private String mesg;
	private long startmillis;

	public TimeCheck(Object caller, String mesg) {
		this.caller = caller;
		this.mesg = shorten(mesg);
	}

	private static String shorten(String s) {
		String result = "";
		if (s != null) {
			if (s.length() >= MAX_SHOWN) {
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
		Logging.notice(caller, "started: ", mesg, " ");
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
		Logging.notice(caller, "ended (", endmillis - startmillis, " ms): ", info);
	}
}
