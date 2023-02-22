package de.uib.configed;

import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

/**
 * HealthInfo processes retrieved data from service_healthCheck RPC method in a
 * way, that retrieved data could be displayed in a frame and saved in a file.
 */
public final class HealthInfo {
	private static AbstractPersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	private HealthInfo() {
	}

	/**
	 * retrieves processed data.
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
			healthDataBuilder.append((String) data.get("check_name") + ": ");
			healthDataBuilder.append(((String) data.get("check_status")).toUpperCase() + " ");
			healthDataBuilder.append("\n\t");
			healthDataBuilder.append((String) data.get("message"));
			healthDataBuilder.append("\n");

			if (includeDetailedInformation) {
				healthDataBuilder.append("\n");

				switch ((String) data.get("check_id")) {
				case "opsiconfd_config":
					processHealthDetails(persist.getOpsiconfdConfigHealth(), healthDataBuilder);
					break;
				case "disk_usage":
					processHealthDetails(persist.getDiskUsageHealth(), healthDataBuilder);
					break;
				case "depotservers":
					processHealthDetails(persist.getDepotHealth(), healthDataBuilder);
					break;
				case "system_packages":
					processHealthDetails(persist.getSystemPackageHealth(), healthDataBuilder);
					break;
				case "product_on_depots":
					processHealthDetails(persist.getProductOnDepotsHealth(), healthDataBuilder);
					break;
				case "product_on_clients":
					processHealthDetails(persist.getProductOnClientsHealth(), healthDataBuilder);
					break;
				case "redis":
				case "mysql":
					continue;
				case "opsi_licenses":
					processHealthDetails(persist.getLicenseHealth(), healthDataBuilder);
					break;
				case "deprecated_calls":
					processHealthDetails(persist.getDeprecatedCalls(), healthDataBuilder);
					break;
				default:
					Logging.warning(HealthInfo.class, "Unknown check id: " + data.get("check_id"));
				}

				healthDataBuilder.append("\n");
			}
		}

		return healthDataBuilder;
	}

	private static void processHealthDetails(List<Map<String, Object>> healthDetails, StringBuilder healthDataBuilder) {
		for (Map<String, Object> details : healthDetails) {
			healthDataBuilder.append("\t");
			healthDataBuilder.append(((String) details.get("check_status")).toUpperCase() + " - ");
			healthDataBuilder.append(((String) details.get("message")).replace("\n", "\n\t\t"));
			healthDataBuilder.append("\n");
		}
	}
}
