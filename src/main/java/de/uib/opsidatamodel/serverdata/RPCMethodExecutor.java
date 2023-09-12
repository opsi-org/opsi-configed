/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.utilities.logging.Logging;

/**
 * Provides methods to handle RPC methods, that don't directly deal with data
 * update/retrieval. Instead they deal with actions, i.e. installing package on
 * depot or firing an event on a host.
 */
public class RPCMethodExecutor {
	AbstractExecutioner exec;
	VolatileDataRetriever volatileDataRetriever;
	PersistentDataRetriever persistentDataRetriever;
	OpsiServiceNOMPersistenceController persistenceController;

	public RPCMethodExecutor(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController,
			VolatileDataRetriever volatileDataRetriever, PersistentDataRetriever persistentDataRetriever) {
		this.exec = exec;
		this.persistenceController = persistenceController;
		this.volatileDataRetriever = volatileDataRetriever;
		this.persistentDataRetriever = persistentDataRetriever;
	}

	public boolean installPackage(String filename) {
		boolean result = exec
				.doCall(new OpsiMethodCall(RPCMethodName.DEPOT_INSTALL_PACKAGE, new Object[] { filename, true }));
		Logging.info(this, "installPackage result " + result);
		return result;
	}

	public boolean setRights(String path) {
		Logging.info(this, "setRights for path " + path);
		String[] args = new String[] { path };
		if (path == null) {
			args = new String[] {};
		}
		return exec.doCall(new OpsiMethodCall(RPCMethodName.SET_RIGHTS, args));
	}

	public List<String> wakeOnLan(String[] hostIds) {
		return wakeOnLan(volatileDataRetriever.getHostSeparationByDepots(hostIds));
	}

	private List<String> wakeOnLan(Map<String, List<String>> hostSeparationByDepot) {
		Map<String, Object> responses = new HashMap<>();

		Map<String, AbstractExecutioner> executionerForDepots = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			Logging.info(this,
					"from depot " + hostSeparationEntry.getKey() + " we have hosts " + hostSeparationEntry.getValue());

			AbstractExecutioner exec1 = executionerForDepots.get(hostSeparationEntry.getKey());

			Logging.info(this, "working exec for depot " + hostSeparationEntry.getKey() + " " + (exec1 != null));

			if (exec1 == null) {
				exec1 = persistenceController.retrieveWorkingExec(hostSeparationEntry.getKey());
			}

			if (exec1 != null && exec1 != AbstractExecutioner.getNoneExecutioner()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START,
						new Object[] { hostSeparationEntry.getValue().toArray(new String[0]) });

				Map<String, Object> responses1 = exec1.getMapResult(omc);
				responses.putAll(responses1);
			}
		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	public List<String> wakeOnLan(Set<String> hostIds, Map<String, List<String>> hostSeparationByDepot,
			Map<String, AbstractExecutioner> execsByDepot) {
		Map<String, Object> responses = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			if (hostSeparationEntry.getValue() != null && !hostSeparationEntry.getValue().isEmpty()) {
				Set<String> hostsToWake = new HashSet<>(hostIds);
				hostsToWake.retainAll(hostSeparationEntry.getValue());

				if (execsByDepot.get(hostSeparationEntry.getKey()) != null
						&& execsByDepot.get(hostSeparationEntry.getKey()) != AbstractExecutioner.getNoneExecutioner()
						&& !hostsToWake.isEmpty()) {
					Logging.debug(this, "wakeOnLan execute for " + hostsToWake);
					OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START,
							new Object[] { hostsToWake.toArray(new String[0]) });

					Map<String, Object> responses1 = execsByDepot.get(hostSeparationEntry.getKey()).getMapResult(omc);
					responses.putAll(responses1);
				}
			}

		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	public List<String> wakeOnLanOpsi43(String[] hostIds) {
		Map<String, Object> response = new HashMap<>();

		AbstractExecutioner exec1 = persistenceController
				.retrieveWorkingExec(persistentDataRetriever.getHostInfoCollections().getConfigServer());

		Logging.info(this, "working exec for config server "
				+ persistentDataRetriever.getHostInfoCollections().getConfigServer() + " " + (exec1 != null));

		if (exec1 != null && exec1 != AbstractExecutioner.getNoneExecutioner()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_START, new Object[] { hostIds });

			response = exec1.getMapResult(omc);
		}

		return collectErrorsFromResponsesByHost(response, "wakeOnLan");
	}

	public List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_FIRE_EVENT,
				new Object[] { event, clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public List<String> showPopupOnClients(String message, String[] clientIds, Float seconds) {
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

	public List<String> shutdownClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_SHUTDOWN, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	public List<String> rebootClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.HOST_CONTROL_REBOOT, new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public List<String> deletePackageCaches(String[] hostIds) {
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
