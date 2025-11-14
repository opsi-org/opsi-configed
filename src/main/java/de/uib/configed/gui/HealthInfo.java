/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;

/**
 * {@link HealthInfo} processes retrieved data from {@code service_healthCheck}
 * RPC method in a way, that retrieved data could be displayed in a frame and
 * saved in a file.
 */
public final class HealthInfo {
	public static final String ERROR = "error";
	public static final String WARNING = "warning";
	public static final String OK = "ok";

	private static final List<String> statusLevels = Arrays.asList(OK, WARNING, ERROR);

	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_SHOW_DETAILS = "showDetails";

	private static final String KEY_ID = "id";
	private static final String KEY_CHECK = "check";
	private static final String KEY_NAME = "name";
	private static final String KEY_CHECK_STATUS = "check_status";

	private HealthInfo() {
	}

	/**
	 * retrieves processed data as String. Data entries are sorted based on the
	 * status level (error, warning, ok).
	 * 
	 * @return processed data with or without detailed information
	 */
	public static String getHealthData() {
		List<Map<String, Object>> healthData = PersistenceControllerFactory.getPersistenceController()
				.getHealthDataService().checkHealthPD();
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
		List<Map<String, Object>> healthData = PersistenceControllerFactory.getPersistenceController()
				.getHealthDataService().checkHealthPD();

		sortHealthDataBasedOnStatusLevel(healthData);

		for (Map<String, Object> data : healthData) {
			Map<String, Object> info = new TreeMap<>();
			info.put(KEY_MESSAGE, produceMessages(data));
			info.put(KEY_DETAILS, produceHealthDetails(data));
			info.put(KEY_SHOW_DETAILS, includeDetailedInformation);

			String checkName = (String) ((Map<?, ?>) data.get(KEY_CHECK)).get(KEY_NAME);
			result.put(checkName, info);
		}

		return result;
	}

	private static String produceMessages(Map<String, Object> healthData) {
		StringBuilder messageBuilder = new StringBuilder();
		String checkName = (String) ((Map<?, ?>) healthData.get(KEY_CHECK)).get(KEY_NAME);
		messageBuilder.append(checkName);
		messageBuilder.append(": ");
		messageBuilder.append(((String) healthData.get(KEY_CHECK_STATUS)).toUpperCase(Locale.ROOT));
		messageBuilder.append(" ");
		messageBuilder.append("\n\t");
		messageBuilder.append((String) healthData.get(KEY_MESSAGE));
		messageBuilder.append("\n");

		return messageBuilder.toString();
	}

	private static String produceHealthDetails(Map<String, Object> healthData) {
		List<Map<String, Object>> healthDetails = PersistenceControllerFactory.getPersistenceController()
				.getHealthDataService()
				.retrieveHealthDetails((String) ((Map<?, ?>) healthData.get(KEY_CHECK)).get(KEY_ID));

		sortHealthDataBasedOnStatusLevel(healthDetails);

		if (healthDetails.isEmpty()) {
			return "";
		}

		StringBuilder healthDetailsBuilder = new StringBuilder();
		healthDetailsBuilder.append("\n");

		for (Map<String, Object> details : healthDetails) {
			healthDetailsBuilder.append("\t");
			healthDetailsBuilder.append(((String) details.get(KEY_CHECK_STATUS)).toUpperCase(Locale.ROOT));
			healthDetailsBuilder.append(" - ");
			healthDetailsBuilder.append(((String) details.get(KEY_MESSAGE)).replace("\n", "\n\t\t"));
			healthDetailsBuilder.append("\n");
		}

		return healthDetailsBuilder.toString();
	}

	private static void sortHealthDataBasedOnStatusLevel(List<Map<String, Object>> healthData) {
		Collections.sort(healthData, (Map<String, Object> map1, Map<String, Object> map2) -> {
			String status1 = (String) map1.get(KEY_CHECK_STATUS);
			String status2 = (String) map2.get(KEY_CHECK_STATUS);
			return -Integer.compare(statusLevels.indexOf(status1), statusLevels.indexOf(status2));
		});
	}

	public static String getMaxWarningLevel() {
		int warningLevel = 0;

		List<Map<String, Object>> healthData = PersistenceControllerFactory.getPersistenceController()
				.getHealthDataService().checkHealthPD();

		for (Map<String, Object> data : healthData) {
			warningLevel = Math.max(warningLevel, statusLevels.indexOf(data.get(KEY_CHECK_STATUS)));
		}

		return statusLevels.get(warningLevel);
	}
}
