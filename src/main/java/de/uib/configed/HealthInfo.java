/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

/**
 * HealthInfo processes retrieved data from service_healthCheck RPC method in a
 * way, that retrieved data could be displayed in a frame and saved in a file.
 */
public final class HealthInfo {
	private static OpsiserviceNOMPersistenceController persist = PersistenceControllerFactory
			.getPersistenceController();

	private HealthInfo() {
	}

	/**
	 * retrieves processed data as String.
	 * 
	 * @param includeDetailedInformation whether or not to include detailed
	 *                                   information, when processing health
	 *                                   data
	 * @return processed data with or without detailed information (depends on
	 *         includeDetailedInformation)
	 */
	public static String getHealthData(boolean includeDetailedInformation) {
		return processHealthData(includeDetailedInformation).toString();
	}

	private static StringBuilder processHealthData(boolean includeDetailedInformation) {
		List<Map<String, Object>> healthData = persist.checkHealth();
		StringBuilder healthDataBuilder = new StringBuilder();

		for (Map<String, Object> data : healthData) {
			healthDataBuilder.append(produceMessages(data));

			if (includeDetailedInformation) {
				healthDataBuilder.append(produceHealthDetails(data));
			}
		}

		return healthDataBuilder;
	}

	/**
	 * retrieves processed data as Map object.
	 * 
	 * @param includeDetailedInformation whether or not to include detailed
	 *                                   information, when processing health
	 *                                   data
	 * @return processed data with or without detailed information (depends on
	 *         includeDetailedInformation)
	 */
	public static Map<String, Map<String, Object>> getHealthDataMap(boolean includeDetailedInformation) {
		return produceMap(includeDetailedInformation);
	}

	private static Map<String, Map<String, Object>> produceMap(boolean includeDetailedInformation) {
		Map<String, Map<String, Object>> result = new TreeMap<>();
		List<Map<String, Object>> healthData = persist.checkHealth();

		for (Map<String, Object> data : healthData) {
			Map<String, Object> info = new TreeMap<>();
			info.put("message", produceMessages(data));
			info.put("details", produceHealthDetails(data));
			info.put("showDetails", includeDetailedInformation);
			result.put((String) data.get("check_name"), info);
		}

		return result;
	}

	private static String produceMessages(Map<String, Object> healthData) {
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append((String) healthData.get("check_name") + ": ");
		messageBuilder.append(((String) healthData.get("check_status")).toUpperCase(Locale.ROOT) + " ");
		messageBuilder.append("\n\t");
		messageBuilder.append((String) healthData.get("message"));
		messageBuilder.append("\n");

		return messageBuilder.toString();
	}

	private static String produceHealthDetails(Map<String, Object> healthData) {
		StringBuilder detailsBuilder = new StringBuilder();

		switch ((String) healthData.get("check_id")) {
		case "opsiconfd_config":
			detailsBuilder.append(processHealthDetails(persist.getOpsiconfdConfigHealth()));
			break;
		case "disk_usage":
			detailsBuilder.append(processHealthDetails(persist.getDiskUsageHealth()));
			break;
		case "depotservers":
			detailsBuilder.append(processHealthDetails(persist.getDepotHealth()));
			break;
		case "system_packages":
			detailsBuilder.append(processHealthDetails(persist.getSystemPackageHealth()));
			break;
		case "product_on_depots":
			detailsBuilder.append(processHealthDetails(persist.getProductOnDepotsHealth()));
			break;
		case "product_on_clients":
			detailsBuilder.append(processHealthDetails(persist.getProductOnClientsHealth()));
			break;
		case "redis":
		case "mysql":
			break;
		case "opsi_licenses":
			detailsBuilder.append(processHealthDetails(persist.getLicenseHealth()));
			break;
		case "deprecated_calls":
			detailsBuilder.append(processHealthDetails(persist.getDeprecatedCalls()));
			break;
		default:
			Logging.notice(HealthInfo.class, "Unknown check id: " + healthData.get("check_id"));
			break;
		}

		return detailsBuilder.toString();
	}

	private static String processHealthDetails(List<Map<String, Object>> healthDetails) {
		StringBuilder healthDetailsBuilder = new StringBuilder();
		healthDetailsBuilder.append("\n");

		for (Map<String, Object> details : healthDetails) {
			healthDetailsBuilder.append("\t");
			healthDetailsBuilder.append(((String) details.get("check_status")).toUpperCase(Locale.ROOT) + " - ");
			healthDetailsBuilder.append(((String) details.get("message")).replace("\n", "\n\t\t"));
			healthDetailsBuilder.append("\n");
		}

		healthDetailsBuilder.append("\n");

		return healthDetailsBuilder.toString();
	}
}
