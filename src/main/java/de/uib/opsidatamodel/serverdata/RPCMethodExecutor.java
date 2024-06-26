/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.dataservice.HostDataService;
import de.uib.utils.logging.Logging;

/**
 * Provides methods to handle RPC methods, that don't directly deal with data
 * update/retrieval. Instead they deal with actions, i.e. installing package on
 * depot or firing an event on a host.
 */
public class RPCMethodExecutor {
	AbstractPOJOExecutioner exec;
	OpsiServiceNOMPersistenceController persistenceController;
	HostDataService hostDataService;
	HostInfoCollections hostInfoCollections;

	public RPCMethodExecutor(AbstractPOJOExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setHostDataService(HostDataService hostDataService) {
		this.hostDataService = hostDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public boolean setRights(String path) {
		Logging.info(this, "setRights for path " + path);
		String[] args = new String[] { path };
		if (path == null) {
			args = new String[] {};
		}
		return exec.doCall(new OpsiMethodCall(RPCMethodName.SET_RIGHTS, args));
	}

	public List<String> wakeOnLanOpsi43(Collection<String> hostIds) {

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START, new Object[] { hostIds });

		Map<String, Object> response = persistenceController.getExecutioner().getMapResult(omc);

		return collectErrorsFromResponsesByHost(response, "wakeOnLan");
	}

	public List<String> fireOpsiclientdEventOnClients(String event, List<String> clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_FIRE_EVENT,
				new Object[] { event, clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public List<String> processActionRequests(List<String> clientIds, Set<String> productIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_PROCESS_ACTION_REQUESTS,
				new Object[] { clientIds, productIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "processActionRequests");
	}

	public List<String> showPopupOnClients(String message, List<String> clientIds, Float seconds) {
		OpsiMethodCall omc;

		if (seconds == 0) {
			omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHOW_POPUP, new Object[] { message, clientIds });
		} else {
			omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHOW_POPUP,
					new Object[] { message, clientIds, "True", "True", seconds });
		}

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");
	}

	public List<String> shutdownClients(List<String> clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHUTDOWN, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	public List<String> rebootClients(List<String> clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_REBOOT, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public List<String> deletePackageCaches(List<String> hostIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SAFE_OPSICLIENTD_RPC,
				new Object[] { "cacheService_deleteCache", new Object[] {}, hostIds });

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "deleteCache");
	}

	// hostControl methods
	private List<String> collectErrorsFromResponsesByHost(Map<String, Object> responses, String callingMethodName) {
		List<String> errors = new ArrayList<>();

		for (Entry<String, Object> response : responses.entrySet()) {
			Map<String, Object> jO = POJOReMapper.remap(response.getValue(), new TypeReference<Map<String, Object>>() {
			});
			String error = exec.getErrorFromResponse(jO);

			if (error != null) {
				error = response.getKey() + ":\t" + error;
				Logging.info(callingMethodName + ",  " + error);
				errors.add(error);
			}
		}

		return errors;
	}
}
