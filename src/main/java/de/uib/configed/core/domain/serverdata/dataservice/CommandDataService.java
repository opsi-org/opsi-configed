/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.infrastructure.AbstractPOJOExecutioner;
import de.uib.configed.core.infrastructure.OpsiMethodCall;
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
public class CommandDataService {
	private AbstractPOJOExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;

	private CacheManager cacheManager = CacheManager.getInstance();

	public CommandDataService(AbstractPOJOExecutioner exec) {
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public List<Map<String, Object>> getCommandList() {
		retrieveCommandList();

		return cacheManager.getCachedData(CacheIdentifier.SSH_COMMAND_LIST, List.class);
	}

	public void retrieveCommandList() {
		if (cacheManager.isDataCached(CacheIdentifier.SSH_COMMAND_LIST)) {
			return;
		}

		Logging.info(this, "retrieveCommandList ");
		List<Map<String, Object>> commands = exec
				.getListOfMaps(new OpsiMethodCall(RPCMethodName.SSH_COMMAND_GET_OBJECTS, new Object[] {}));
		Logging.debug(this, "retrieveCommandList commands ", commands);

		cacheManager.setCachedData(CacheIdentifier.SSH_COMMAND_LIST, commands);
	}

	public boolean deleteCommand(List<String> jsonObjects) {
		Logging.info(this, "deleteSSHCommand ");
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SSH_COMMAND_DELETE_OBJECTS, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
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
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(method, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
		Logging.info(this, "doActionSSHCommand method ", method, " result ", result);
		return result;
	}
}
