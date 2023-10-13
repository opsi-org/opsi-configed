/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * Provides methods for working with log data on the server.
 * <p>
 * Classes ending in {@code DataService} represent somewhat of a layer between
 * server and the client. It enables to work with specific data, that is saved
 * on the server.
 * <p>
 * {@code DataService} classes only allow to retrieve and update data. Data may
 * be internally cached. The internally cached data is identified by a method
 * name. If a method name ends in {@code PD}, it means that method either
 * retrieves or it updates internally cached data. {@code PD} stands for
 * {@code Persistent Data}.
 */
public class LogDataService {
	private AbstractExecutioner exec;

	public LogDataService(AbstractExecutioner exec) {
		this.exec = exec;
	}

	public Map<String, String> getLogfile(String clientId, String logtype) {
		Map<String, String> logfiles = getEmptyLogfiles();
		int i = Arrays.asList(Utils.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			Logging.error("illegal logtype: " + logtype);
			return logfiles;
		}
		Logging.debug(this, "getLogfile logtype " + logtype);

		String[] logtypes = Utils.getLogTypes();
		Logging.debug(this, "OpsiMethodCall log_read " + logtypes[i] + " max size " + Utils.getMaxLogSize(i));
		String s = "";
		try {
			s = exec.getStringResult(new OpsiMethodCall(RPCMethodName.LOG_READ,
					new String[] { logtype, clientId, String.valueOf(Utils.getMaxLogSize(i)) }));
		} catch (OutOfMemoryError e) {
			s = "--- file too big for showing, enlarge java memory  ---";
			Logging.debug(this, "thrown exception: " + e);
		}

		logfiles.put(logtype, s);
		return logfiles;
	}

	private static Map<String, String> getEmptyLogfiles() {
		Map<String, String> logfiles = new HashMap<>();
		String[] logtypes = Utils.getLogTypes();
		for (String logtype : logtypes) {
			logfiles.put(logtype, "");
		}
		return logfiles;
	}
}
