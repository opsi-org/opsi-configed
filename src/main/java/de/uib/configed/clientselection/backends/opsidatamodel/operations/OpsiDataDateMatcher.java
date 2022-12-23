package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.SelectElement;
import de.uib.utilities.logging.logging;

public abstract class OpsiDataDateMatcher extends OpsiDataMatcher {

	public OpsiDataDateMatcher(String map, String key, String data, SelectElement element) {
		super(map, key, data, element);
	}

	@Override
	protected boolean checkData(final String realdata) {
		
		// to " +data);

		java.sql.Date date = null;
		java.sql.Date realdate = null;

		try {
			date = java.sql.Date.valueOf(data);
		} catch (Exception ex) {
			logging.debug(this, "OpsiDataDateMatcher data is not a date! " + date + " " + ex);
			return false;
		}

		if (realdata == null) {
			logging.debug(this, "OpsiDataDateMatcher no data found");
			return false;
		}

		if (!(realdata instanceof String)) {
			logging.debug(this, "OpsiDataDateMatcher data not a string " + realdata);
			return false;
		}

		if (realdata.equals("")) {
			return false;
		}

		String realD = ((String) realdata).trim();

		int posBlank = realD.indexOf(' ');
		if (posBlank > 0) {
			realD = realD.substring(0, posBlank);
		}

		
		

		// check if we have to interpret variables

		try {
			realdate = java.sql.Date.valueOf(realD);
			return compare(date, realdate);
		} catch (Exception ex) {
			logging.debug(this, "data is not a date! " + realdata + " " + ex);
			return false;
		}

	}

	protected abstract boolean compare(java.sql.Date date, java.sql.Date realdate);
}
