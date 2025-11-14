/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;

/**
 * Utility class for processing health check data.
 * <p>
 * Provides methods to transform raw health data retrieved from the server into
 * formats suitable for:
 * <ul>
 * <li>UI display (as a Map)</li>
 * <li>File export or saving (as a formatted String)</li>
 * </ul>
 * <p>
 * Data entries are consistently sorted by status level (ERROR > WARNING > OK),
 * and detailed information can optionally be included.
 */
public final class HealthDataProcessor {
	public enum StatusLevel {
		OK, WARNING, ERROR
	}

	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_SHOW_DETAILS = "showDetails";

	private static final String KEY_ID = "id";
	private static final String KEY_CHECK = "check";
	private static final String KEY_NAME = "name";
	private static final String KEY_CHECK_STATUS = "check_status";

	private HealthDataProcessor() {
	}

	/**
	 * Builds a processed health data string suitable for export or saving to a
	 * file. Entries are sorted by status level (ERROR > WARNING > OK) and
	 * formatted for readability.
	 *
	 * @return a formatted string representing all health checks, including
	 *         messages and details
	 */
	public static String buildHealthDataForExport() {
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
	 * Builds a processed health data map suitable for UI display. Each entry is
	 * sorted by status level (ERROR > WARNING > OK).
	 *
	 * @param includeDetailedInformation whether to include detailed information
	 *                                   in each entry
	 * @return a map of health checks with optional detailed information
	 */
	public static Map<String, Map<String, Object>> buildHealthDataForUI(boolean includeDetailedInformation) {
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
			StatusLevel s1 = StatusLevel.valueOf(((String) map1.get(KEY_CHECK_STATUS)).toUpperCase(Locale.ROOT));
			StatusLevel s2 = StatusLevel.valueOf(((String) map2.get(KEY_CHECK_STATUS)).toUpperCase(Locale.ROOT));
			return -Integer.compare(s1.ordinal(), s2.ordinal());
		});
	}

	public static StatusLevel getMaxStatusLevel() {
		List<Map<String, Object>> healthData = PersistenceControllerFactory.getPersistenceController()
				.getHealthDataService().checkHealthPD();

		return healthData.stream()
				.map(m -> StatusLevel.valueOf(((String) m.get(KEY_CHECK_STATUS)).toUpperCase(Locale.ROOT)))
				.max(Comparator.comparingInt(StatusLevel::ordinal)).orElse(StatusLevel.OK);
	}
}
