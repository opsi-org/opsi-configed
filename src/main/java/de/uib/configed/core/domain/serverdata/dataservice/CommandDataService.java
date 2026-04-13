/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.share.logging.Logging;

/**
 * Provides methods for working with command's data on the server.
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
@SuppressWarnings({ "unchecked" })
public class CommandDataService extends DataService {
	public CommandDataService(DataServices dataServices) {
		super(dataServices);
	}

	public List<Map<String, Object>> getCommandList() {
		retrieveCommandList();

		return dataServices.cacheManager.getCachedData(CacheIdentifier.SSH_COMMAND_LIST, List.class);
	}

	public void retrieveCommandList() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.SSH_COMMAND_LIST)) {
			return;
		}

		Logging.info(this, "retrieveCommandList ");
		List<Map<String, Object>> commands = dataServices.exec.getListOfMaps(RPCMethodName.SSH_COMMAND_GET_OBJECTS);
		Logging.debug(this, "retrieveCommandList commands ", commands);

		dataServices.cacheManager.setCachedData(CacheIdentifier.SSH_COMMAND_LIST, commands);
	}

	public boolean deleteCommand(List<String> jsonObjects) {
		Logging.info(this, "deleteSSHCommand ");
		if (Boolean.TRUE.equals(dataServices.userRoles.isGlobalReadOnly())) {
			return false;
		}
		boolean result = dataServices.exec.doCall(RPCMethodName.SSH_COMMAND_DELETE_OBJECTS, jsonObjects);
		Logging.info(this, "deleteSSHCommand result ", result);
		return result;
	}

	public boolean createCommand(List<Object> jsonObjects) {
		return doActionCommand(RPCMethodName.SSH_COMMAND_CREATE_OBJECTS, jsonObjects);
	}

	public boolean updateCommand(List<Object> jsonObjects) {
		return doActionCommand(RPCMethodName.SSH_COMMAND_UPDATE_OBJECTS, jsonObjects);
	}

	private boolean doActionCommand(RPCMethodName method, List<Object> jsonObjects) {
		Logging.info(this, "doActionSSHCommand method ", method);
		if (Boolean.TRUE.equals(dataServices.userRoles.isGlobalReadOnly())) {
			return false;
		}
		boolean result = dataServices.exec.doCall(method, jsonObjects);
		Logging.info(this, "doActionSSHCommand method ", method, " result ", result);
		return result;
	}
}
