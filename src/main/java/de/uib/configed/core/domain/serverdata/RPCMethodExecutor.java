/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.dataservice.DataService;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.share.logging.Logging;

/**
 * Provides methods to handle RPC methods, that don't directly deal with data
 * update/retrieval. Instead they deal with actions, i.e. installing package on
 * depot or firing an event on a host.
 */
public class RPCMethodExecutor extends DataService {
	public RPCMethodExecutor(DataServices dataServices) {
		super(dataServices);
	}

	public List<String> wakeOnLanOpsi43(Collection<String> hostIds) {
		Map<String, Object> response = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_START, hostIds);

		return collectErrorsFromResponsesByHost(response, "wakeOnLan");
	}

	public List<String> fireOpsiclientdEventOnClients(String event, List<String> clientIds) {
		Map<String, Object> responses = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_FIRE_EVENT, event,
				clientIds);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public List<String> processActionRequests(List<String> clientIds, Set<String> productIds, String visibility) {
		Map<String, Object> responses = dataServices.exec
				.getMapResult(RPCMethodName.HOST_CONTROL_PROCESS_ACTION_REQUESTS, clientIds, productIds, visibility);
		return collectErrorsFromResponsesByHost(responses, "processActionRequests");
	}

	public List<String> showPopupOnClients(String message, List<String> clientIds, Float seconds) {

		Object[] parameters;

		if (seconds == 0) {
			parameters = new Object[] { message, clientIds };
		} else {
			parameters = new Object[] { message, clientIds, "True", "True", seconds };
		}

		Map<String, Object> responses = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_SHOW_POPUP,
				parameters);
		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");
	}

	public List<String> shutdownClients(List<String> clientIds) {
		Map<String, Object> responses = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_SHUTDOWN, clientIds);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	public List<String> rebootClients(List<String> clientIds) {
		Map<String, Object> responses = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_REBOOT, clientIds);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public List<String> deletePackageCaches(List<String> hostIds) {
		Map<String, Object> responses = dataServices.exec.getMapResult(RPCMethodName.HOST_CONTROL_SAFE_OPSICLIENTD_RPC,
				"cacheService_deleteCache", new Object[0], hostIds);
		return collectErrorsFromResponsesByHost(responses, "deleteCache");
	}

	// hostControl methods
	private List<String> collectErrorsFromResponsesByHost(Map<String, Object> responses, String callingMethodName) {
		List<String> errors = new ArrayList<>();

		for (Entry<String, Object> response : responses.entrySet()) {
			String error = dataServices.exec.getErrorFromResponse(POJOReMapper.remap(response.getValue()));

			if (error != null) {
				error = response.getKey() + ":\t" + error;
				Logging.info(callingMethodName, ",  ", error);
				errors.add(error);
			}
		}

		return errors;
	}
}
