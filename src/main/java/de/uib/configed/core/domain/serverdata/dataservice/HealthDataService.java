/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.infrastructure.POJOReMapper;

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
public class HealthDataService extends DataService {
	public HealthDataService(DataServices dataServices) {
		super(dataServices);
	}

	public List<Map<String, Object>> checkHealthPD() {
		retrieveHealthDataPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.HEALTH_CHECK_DATA, List.class);
	}

	public void retrieveHealthDataPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.HEALTH_CHECK_DATA)) {
			return;
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.HEALTH_CHECK_DATA,
				dataServices.exec.getListOfMaps(RPCMethodName.SERVICE_HEALTH_CHECK));
	}

	public Map<String, Object> getDiagnosticDataPD() {
		retrieveDiagnosticDataPD();
		return dataServices.cacheManager.getCachedData(CacheIdentifier.DIAGNOSTIC_DATA, Map.class);
	}

	public void retrieveDiagnosticDataPD() {
		if (dataServices.cacheManager.isDataCached(CacheIdentifier.DIAGNOSTIC_DATA)) {
			return;
		}

		dataServices.cacheManager.setCachedData(CacheIdentifier.DIAGNOSTIC_DATA,
				dataServices.exec.getMapResult(RPCMethodName.SERVICE_GET_DIAGNOSTIC_DATA));
	}

	public List<Map<String, Object>> retrieveHealthDetails(String checkId) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (Map<String, Object> data : checkHealthPD()) {
			if (((Map<?, ?>) data.get("check")).get("id").equals(checkId)) {
				result = POJOReMapper.remap(data.get("partial_results"));
				break;
			}
		}
		return result;
	}
}
