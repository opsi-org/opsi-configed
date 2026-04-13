/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.share.logging.Logging;

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
public class LogDataService extends DataService {
	public LogDataService(DataServices dataServices) {
		super(dataServices);
	}

	public String getLogfile(String clientId, String logtype) {
		Logging.debug(this, "OpsiMethodCall log_read ", logtype, "for client ", clientId);
		String logtext;
		try {
			logtext = dataServices.exec.getStringResult(RPCMethodName.LOG_READ, logtype, clientId);
		} catch (OutOfMemoryError e) {
			logtext = "--- file too big for showing, enlarge java memory  ---";
			Logging.error(this, e, "file too big for showing ", logtype);
		}

		return logtext;
	}
}
