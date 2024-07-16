/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.List;
import java.util.Map;

import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.logging.Logging;

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
public class CommandDataService {
	private AbstractPOJOExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;

	public CommandDataService(AbstractPOJOExecutioner exec) {
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public List<Map<String, Object>> retrieveCommandList() {
		Logging.info(this, "retrieveCommandList ");
		List<Map<String, Object>> commands = exec
				.getListOfMaps(new OpsiMethodCall(RPCMethodName.SSH_COMMAND_GET_OBJECTS, new Object[] {}));
		Logging.debug(this, "retrieveCommandList commands " + commands);
		return commands;
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
