/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

/**
 * {@link HealthInfo} processes retrieved data from {@code service_healthCheck}
 * RPC method in a way, that retrieved data could be displayed in a frame and
 * saved in a file.
 */
public final class HealthInfo {
	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private HealthInfo() {
	}

	/**
	 * retrieves processed data as String. Data entries are sorted based on the
	 * status level (error, warning, ok).
	 * 
	 * @return processed data with or without detailed information
	 */
	public static String getHealthData() {
		List<Map<String, Object>> healthData = persistenceController.getHealthDataService().checkHealthPD();
		StringBuilder healthDataBuilder = new StringBuilder();

		sortHealthDataBasedOnStatusLevel(healthData);

		for (Map<String, Object> data : healthData) {
			healthDataBuilder.append(produceMessages(data));
			healthDataBuilder.append(produceHealthDetails(data));
		}

		return healthDataBuilder.toString();
	}

	/**
	 * retrieves processed data as Map object. Data entries are sorted based on
	 * the status level (error, warning, ok).
	 * 
	 * @param includeDetailedInformation whether to include detailed
	 *                                   information, when processing health
	 *                                   data
	 * @return processed data with or without detailed information (depends on
	 *         {@code includeDetailedInformation})
	 */
	public static Map<String, Map<String, Object>> getHealthDataMap(boolean includeDetailedInformation) {
		return produceMap(includeDetailedInformation);
	}

	private static Map<String, Map<String, Object>> produceMap(boolean includeDetailedInformation) {
		Map<String, Map<String, Object>> result = new LinkedHashMap<>();
		List<Map<String, Object>> healthData = persistenceController.getHealthDataService().checkHealthPD();

		sortHealthDataBasedOnStatusLevel(healthData);

		for (Map<String, Object> data : healthData) {
			Map<String, Object> info = new TreeMap<>();
			info.put("message", produceMessages(data));
			info.put("details", produceHealthDetails(data));
			info.put("showDetails", includeDetailedInformation);
			result.put((String) ((Map<?, ?>) data.get("check")).get("name"), info);
		}

		return result;
	}

	private static String produceMessages(Map<String, Object> healthData) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append((String) ((Map<?, ?>) healthData.get("check")).get("name") + ": ");
		messageBuilder.append(((String) healthData.get("check_status")).toUpperCase(Locale.ROOT) + " ");
		messageBuilder.append("\n\t");
		messageBuilder.append((String) healthData.get("message"));
		messageBuilder.append("\n");

		return messageBuilder.toString();
	}

	private static String produceHealthDetails(Map<String, Object> healthData) {
		List<Map<String, Object>> healthDetails = persistenceController.getHealthDataService()
				.retrieveHealthDetails((String) ((Map<?, ?>) healthData.get("check")).get("id"));

		sortHealthDataBasedOnStatusLevel(healthDetails);

		if (healthDetails.isEmpty()) {
			return "";
		}

		StringBuilder healthDetailsBuilder = new StringBuilder();
		healthDetailsBuilder.append("\n");

		for (Map<String, Object> details : healthDetails) {
			healthDetailsBuilder.append("\t");
			healthDetailsBuilder.append(((String) details.get("check_status")).toUpperCase(Locale.ROOT) + " - ");
			healthDetailsBuilder.append(((String) details.get("message")).replace("\n", "\n\t\t"));
			healthDetailsBuilder.append("\n");
		}

		return healthDetailsBuilder.toString();
	}

	private static void sortHealthDataBasedOnStatusLevel(List<Map<String, Object>> healthData) {
		Collections.sort(healthData, (map1, map2) -> {
			List<String> statusLevels = Arrays.asList("error", "warning", "ok");
			String status1 = (String) map1.get("check_status");
			String status2 = (String) map2.get("check_status");
			return Integer.compare(statusLevels.indexOf(status1), statusLevels.indexOf(status2));
		});
	}
}
