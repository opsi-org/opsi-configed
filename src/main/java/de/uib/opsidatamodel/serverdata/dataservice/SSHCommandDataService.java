/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.List;
import java.util.Map;

import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utils.logging.Logging;

/**
 * Provides methods for working with SSH command data on the server.
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
public class SSHCommandDataService {
	private AbstractExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;

	public SSHCommandDataService(AbstractExecutioner exec) {
		this.exec = exec;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	/**
	 * Execute the python-opsi command {@code SSHCommand_getObjects}.
	 *
	 * @return list of commands available for executing with SSH
	 */
	public List<Map<String, Object>> retrieveCommandList() {
		Logging.info(this, "retrieveCommandList ");
		List<Map<String, Object>> sshCommands = exec
				.getListOfMaps(new OpsiMethodCall(RPCMethodName.SSH_COMMAND_GET_OBJECTS, new Object[] {}));
		Logging.debug(this, "retrieveCommandList commands " + sshCommands);
		return sshCommands;
	}

	/**
	 * Exec a python-opsi command
	 *
	 * @param method      name
	 * @param jsonObjects to do sth
	 * @return result true if everything is ok
	 */
	private boolean doActionSSHCommand(RPCMethodName method, List<Object> jsonObjects) {
		Logging.info(this, "doActionSSHCommand method " + method);
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(method, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
		Logging.info(this, "doActionSSHCommand method " + method + " result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_deleteObjects"
	 *
	 * @param jsonObjects to remove
	 * @return result true if successfull
	 */
	public boolean deleteSSHCommand(List<String> jsonObjects) {
		Logging.info(this, "deleteSSHCommand ");
		if (Boolean.TRUE.equals(userRolesConfigDataService.isGlobalReadOnly())) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SSH_COMMAND_DELETE_OBJECTS, new Object[] { jsonObjects });
		boolean result = exec.doCall(omc);
		Logging.info(this, "deleteSSHCommand result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_createObjects"
	 *
	 * @param jsonObjects to create
	 * @return result true if successfull
	 */
	public boolean createSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand(RPCMethodName.SSH_COMMAND_CREATE_OBJECTS, jsonObjects);
	}

	/**
	 * Exec the python-opsi command "SSHCommand_updateObjects"
	 *
	 * @param jsonObjects to update
	 * @return result true if successfull
	 */
	public boolean updateSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand(RPCMethodName.SSH_COMMAND_UPDATE_OBJECTS, jsonObjects);
	}
}
