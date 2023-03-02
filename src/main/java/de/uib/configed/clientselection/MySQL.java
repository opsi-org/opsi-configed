package de.uib.configed.clientselection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.uib.utilities.logging.Logging;

public class MySQL {

	public static final String KEY_OPERATION = "operation";

	private boolean group;
	private boolean product;
	private boolean property;
	private boolean software;
	private boolean hardware;

	private boolean hardwareWithDevice;
	private String hardwareTableName = "";

	// Die Hardwarekonfiguration
	List<Map<String, List<Map<String, Object>>>> hwConfig;

	public MySQL(List<Map<String, List<Map<String, Object>>>> hwConfig) {
		this.hwConfig = hwConfig;
	}

	public String getMySQLInnerJoins() {
		String joins = "";

		if (group) {
			joins += " INNER JOIN OBJECT_TO_GROUP a ON (HOST.hostId=a.objectId AND a.groupType LIKE 'HostGroup' ) ";
		}

		if (product) {
			joins += " INNER JOIN PRODUCT_ON_CLIENT d ON (HOST.hostId=d.clientId) ";
		}

		if (property) {
			joins += " INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.productId LIKE d.productId AND h.objectId LIKE HOST.hostId) ";
		}

		if (software) {
			joins += " INNER JOIN SOFTWARE_CONFIG f ON (HOST.hostId=f.clientId) INNER JOIN SOFTWARE g ON (f.name=g.name AND f.version=g.version AND f.subVersion=g.subVersion AND f.language=g.language AND f.architecture=g.architecture) ";
		}

		if (hardware) {
			String hardwareJoinString;

			if (hardwareWithDevice) {
				hardwareJoinString = " INNER JOIN HARDWARE_CONFIG_" + hardwareTableName
						+ " i ON (i.hostId LIKE HOST.hostId) INNER JOIN HARDWARE_DEVICE_" + hardwareTableName
						+ " g ON (g.hardware_id LIKE i.hardware_id) ";
			} else {
				hardwareJoinString = " INNER JOIN HARDWARE_CONFIG_" + hardwareTableName
						+ " i ON (i.hostId LIKE HOST.hostId) ";
			}

			joins += hardwareJoinString;
		}
		return joins;
	}

	public String getMySQLforJSONObject(JSONObject json) {

		Logging.info(this, "json source " + json);
		try {
			if (!json.isNull("element")) {

				String data = json.getString("data");

				data = data.replace('*', '%');

				switch (json.getString("element")) {

				// HOST
				case "GroupElement":
					group = true;

					return " a.groupId LIKE '" + data + "' ";

				case "IPElement":
					return " HOST.ipAddress LIKE '" + data + "' ";

				case "NameElement":
					return " HOST.hostId LIKE '" + data + "' ";

				case "DescriptionElement":
					return " HOST.description LIKE '" + data + "' ";

				// opsi-Product
				case "SoftwareNameElement":
					product = true;
					return " d.productId LIKE '" + data + "' ";

				case "SoftwareInstallationStatusElement":
					product = true;
					return " IFNULL(d.installationStatus, '') LIKE '" + data + "' ";

				case "SoftwareActionResultElement":
					product = true;
					return " IFNULL(d.actionResult, '') LIKE '" + data + "' ";

				case "SoftwareRequestElement":
					product = true;
					return " IFNULL(d.actionRequest, '') LIKE '" + data + "' ";

				case "SoftwareActionProgressElement":
					product = true;
					return " IFNULL(d.actionProgress, '') LIKE '" + data + "' ";

				case "SoftwareLastActionElement":
					product = true;
					return " IFNULL(d.lastAction, '') LIKE '" + data + "' ";

				case "SoftwareVersionElement":
					product = true;
					return " IFNULL(d.productVersion, '') LIKE '" + data + "' ";

				case "SoftwarePackageVersionElement":
					product = true;
					return " IFNULL(d.packageVersion, '') LIKE '" + data + "' ";

				case "SoftwareModificationTimeElement":
					product = true;
					return getMySQLSoftwareModificationTime(json.getString(KEY_OPERATION), data);

				// Property
				case "PropertyIdElement":
					product = property = true;
					return " h.propertyId LIKE '" + data + "' ";

				case "PropertyValueElement":
					product = property = true;

					// In der Datenbank sind die 'values' immer in Anführungszeichen,
					// Außnahme: true, false
					if (data.equals("false") || data.equals("true")) {
						return " (h.values LIKE '%" + data + "%' OR h.values LIKE '\"%" + data + "\"%') ";
					}

					// 'data' should be part of the array
					return " h.values LIKE '%\"" + data + "\"%' ";

				// Software-Inventur-Data
				case "SwAuditNameElement":
					software = true;
					return " f.name LIKE '" + data + "' ";

				case "SwAuditVersionElement":
					software = true;
					return " f.version LIKE '" + data + "' ";

				case "SwAuditSubversionElement":
					software = true;
					return " f.subVersion LIKE '" + data + "' ";

				case "SwAuditArchitectureElement":
					software = true;
					return " f.architecture LIKE '" + data + "' ";

				case "SwAuditLanguageElement":
					software = true;
					return " f.language LIKE '" + data + "' ";

				case "SwAuditSoftwareIdElement":
					software = true;
					return " g.WindowsSoftwareId LIKE '" + data + "' ";

				// Hardware
				case "GenericTextElement":
					hardware = true;

					String query = setHardware(json);

					return " (" + query + " LIKE '" + data + "') ";

				case "GenericBigIntegerElement":
				case "GenericIntegerElement":
					hardware = true;

					String operation = json.getString(KEY_OPERATION);
					operation = getOperationFromElement(operation);

					query = setHardware(json);

					return " (" + query + " " + operation + " '" + data + "') ";

				case "GenericDateElement":
					Logging.error(this, "Date at the moment not supported...");
					break;

				default:
					Logging.error(this, "Unexpected value of 'element' in JSON query for producing MySQL-query");
					break;
				}
			}
		} catch (Exception e) {
			Logging.warning(this, "we did not interpret element selection " + e);
		}

		return "";
	}

	private String setHardware(JSONObject json) {

		JSONArray elementPath = json.getJSONArray("elementPath");
		String hardwareType = elementPath.getString(0);
		String column = elementPath.getString(1);
		Map<String, List<Map<String, Object>>> element = findHardwareInConfig(hardwareType);

		hardwareTableName = (String) element.get("Class").get(0).get("Opsi");

		List<Map<String, Object>> values = element.get("Values");

		Map<String, Object> spalte = findColumnInTable(column, values);
		String spaltenName = (String) spalte.get("Opsi");
		String scope = (String) spalte.get("Scope");

		if (scope.equals("g")) {
			hardwareWithDevice = true;
		}

		// Do not allow empty fields
		return scope + "." + spaltenName + " NOT LIKE '' AND " + scope + "." + spaltenName;
	}

	private Map<String, Object> findColumnInTable(String column, List<Map<String, Object>> values) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).get("UI").equals(column)) {
				return values.get(i);
			}
		}

		return new HashMap<>();
	}

	// Actually return type is Map<String, Map<String, String>>, but cannot cast
	private Map<String, List<Map<String, Object>>> findHardwareInConfig(String elementPath) {
		for (int i = 0; i < hwConfig.size(); i++) {
			if (hwConfig.get(i).get("Class").get(0).get("UI").equals(elementPath)) {
				return hwConfig.get(i);
			}
		}

		return new HashMap<>();
	}

	private String getOperationFromElement(String operation) {

		switch (operation) {
		case "BigIntLessThanOperation":
		case "IntLessThanOperation":
			return "<";

		case "BigIntLessOrEqualOperation":
		case "IntLessOrEqualOperation":
			return "<=";

		case "BigIntEqualsOperation":
		case "IntEqualsOperation":
			return "=";

		case "BigIntGreaterThanOperation":
		case "IntGreaterThanOperation":
			return ">";

		case "BigIntGreaterOrEqualOperation":
		case "IntGreaterOrEqualOperation":
			return ">=";

		default:
			return null;
		}
	}

	// // to opsi-Product
	private String getMySQLSoftwareModificationTime(String operation, String data) {
		String expression = "";
		switch (operation) {
		case "DateGreaterThanOperation":
			expression = ">";
			break;

		case "DateGreaterOrEqualOperation":
			expression = ">=";
			break;

		case "DateEqualsOperation":
			expression = "=";
			break;

		case "DateLessOrEqualOperation":
			expression = "<=";
			break;

		case "DateLessThanOperation":
			expression = "<";
			break;

		default:
			Logging.error(this, "unexpected Date Operation");
			break;
		}

		return " DATE(d.modificationTime)" + expression + "'" + data + "'";
	}
}
