/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.sql.Date;
import java.util.Map;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.utilities.logging.Logging;

public abstract class AbstractOpsiDataDateMatcher {

	private String map;
	private String key;
	private String data;

	protected AbstractOpsiDataDateMatcher(String map, String key, String data) {
		Logging.debug(this, "created:  maptype, key, data: " + map + ", " + key + ", " + data);

		this.map = map;
		this.key = key;
		this.data = data;
	}

	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		Logging.debug(this, "doesMatch client " + oClient);

		Map<String, Object> realMap = oClient.getMap(map);

		if (!realMap.containsKey(key) || realMap.get(key) == null) {

			return false;
		}

		String realData = realMap.get(key).toString();

		return checkData(realData);
	}

	private boolean checkData(final String realdata) {

		Date date = null;

		try {
			date = Date.valueOf(data);
		} catch (Exception ex) {
			Logging.debug(this, "OpsiDataDateMatcher data is not a date! " + date + " " + ex);
			return false;
		}

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

		// check if we have to interpret variables
		Date realdate = null;

		try {
			realdate = Date.valueOf(realD);
			return compare(date, realdate);
		} catch (Exception ex) {
			Logging.debug(this, "data is not a date! " + realdata + " " + ex);
			return false;
		}

	}

	protected abstract boolean compare(Date date, Date realdate);
}
