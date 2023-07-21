/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.sql.Date;
import java.util.Map;

import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.Logging;

public abstract class AbstractOpsiDataDateMatcher {

	private String map;
	private String key;
	private String data;

	protected AbstractOpsiDataDateMatcher(String map, String key, String data) {
		Logging.debug(this.getClass(), "created:  maptype, key, data: " + map + ", " + key + ", " + data);

		this.map = map;
		this.key = key;
		this.data = data;
	}

	public boolean doesMatch(OpsiDataClient client) {
		Logging.debug(this, "doesMatch client " + client);

		Map<String, Object> realMap = client.getMap(map);

		if (!realMap.containsKey(key) || realMap.get(key) == null) {

			return false;
		}

		String realData = realMap.get(key).toString();

		return checkData(realData);
	}

	private boolean checkData(final String realdata) {

		if (realdata == null) {
			Logging.debug(this, "OpsiDataDateMatcher no data found");
			return false;
		}

		if (!(realdata instanceof String)) {
			Logging.debug(this, "OpsiDataDateMatcher data not a string " + realdata);
			return false;
		}

		if ("".equals(realdata)) {
			return false;
		}

		String realD = realdata.trim();

		int posBlank = realD.indexOf(' ');
		if (posBlank > 0) {
			realD = realD.substring(0, posBlank);
		}

		Date date = Date.valueOf(data);

		// check if we have to interpret variables
		Date realdate = Date.valueOf(realD);
		return compare(date, realdate);
	}

	protected abstract boolean compare(Date date, Date realdate);
}
