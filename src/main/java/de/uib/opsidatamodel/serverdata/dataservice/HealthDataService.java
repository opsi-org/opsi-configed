/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;

/**
 * Provides methods for working with health data on the server.
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
public class HealthDataService {
	private CacheManager cacheManager;
	private AbstractExecutioner exec;

	public HealthDataService(AbstractExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public List<Map<String, Object>> checkHealthPD() {
		retrieveHealthDataPD();
		return cacheManager.getCachedData(CacheIdentifier.HEALTH_CHECK_DATA, List.class);
	}

	public void retrieveHealthDataPD() {
		if (cacheManager.getCachedData(CacheIdentifier.HEALTH_CHECK_DATA, List.class) != null) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SERVICE_HEALTH_CHECK, new Object[0]);
		cacheManager.setCachedData(CacheIdentifier.HEALTH_CHECK_DATA, exec.getListOfMaps(omc));
	}

	public Map<String, Object> getDiagnosticDataPD() {
		retrieveDiagnosticDataPD();
		return cacheManager.getCachedData(CacheIdentifier.DIAGNOSTIC_DATA, Map.class);
	}

	public void retrieveDiagnosticDataPD() {
		if (cacheManager.getCachedData(CacheIdentifier.DIAGNOSTIC_DATA, Map.class) != null) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SERVICE_GET_DIAGNOSTIC_DATA, new Object[0]);
		cacheManager.setCachedData(CacheIdentifier.DIAGNOSTIC_DATA, exec.getMapResult(omc));
	}

	public boolean isHealthDataAlreadyLoaded() {
		return cacheManager.getCachedData(CacheIdentifier.HEALTH_CHECK_DATA, List.class) != null;
	}

	public List<Map<String, Object>> retrieveHealthDetails(String checkId) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> data : checkHealthPD()) {
			if (((String) data.get("check_id")).equals(checkId)) {
				result = POJOReMapper.remap(data.get("partial_results"),
						new TypeReference<List<Map<String, Object>>>() {
						});
				break;
			}
		}
		return result;
	}
}
