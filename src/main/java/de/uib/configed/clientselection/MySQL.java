package de.uib.configed.clientselection;

import org.json.*;
import de.uib.opsidatamodel.PersistenceController;
import java.util.*;
import de.uib.utilities.logging.*;

public class MySQL {
		
	public enum Type {
		OR, AND, NOT, NEW};
		
	boolean group, product, property, software, hardware = false;
	
	private boolean hardwareWithDevice = false;
	private String hardwareTableName = "";
		
	// Die Hardwarekonfiguration
	List hwConfig;
	
	public MySQL(List hwConfig) {
		this.hwConfig = hwConfig;
	}
		
	public String getMySQL_INNERJOINS() {
		String joins = "";
		
		if(group)
			joins += " INNER JOIN OBJECT_TO_GROUP a ON (HOST.hostId=a.objectId AND a.groupType LIKE 'HostGroup' ) ";
		
		if(product)
			joins += " INNER JOIN PRODUCT_ON_CLIENT d ON (HOST.hostId=d.clientId) ";
		
		if(property)
			joins += " INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.productId LIKE d.productId AND h.objectId LIKE HOST.hostId) ";
			
			//joins += " INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.productId LIKE e.productId AND h.productVersion LIKE e.productVersion AND h.packageVersion LIKE e.packageVersion) ";
			
		if(software)
			joins += " INNER JOIN SOFTWARE_CONFIG f ON (HOST.hostId=f.clientId) INNER JOIN SOFTWARE g ON (f.name=g.name AND f.version=g.version AND f.subVersion=g.subVersion AND f.language=g.language AND f.architecture=g.architecture) ";
			
		if(hardware) {
			String hardwareJoinString;
			
			if(hardwareWithDevice)
				hardwareJoinString = " INNER JOIN HARDWARE_CONFIG_" + hardwareTableName + " i ON (i.hostId LIKE HOST.hostId) INNER JOIN HARDWARE_DEVICE_" + hardwareTableName + " g ON (g.hardware_id LIKE i.hardware_id) ";
			else
				hardwareJoinString = " INNER JOIN HARDWARE_CONFIG_" + hardwareTableName + " i ON (i.hostId LIKE HOST.hostId) ";
				
				
			joins += hardwareJoinString;
		}
		return joins;
	}
	
	public String getMySQLforJSONObject(JSONObject json) {
		
		
		logging.info(this, "json source " + json);
		try {
			if(!json.isNull("element")) {
				
				String data = "%";
				try {
					data = json.getString("data");
				} catch(Exception e){logging.warning(this, "we did not get json from data " + e);}
				
				data = data.replace('*', '%');
				
				switch(json.getString("element")) {
					
					// HOST
					case "GroupElement":
						group = true;
						String operation = json.getString("elementPath");
						if(operation.equals("Group"))
							return " a.groupId LIKE '"+data+"' ";
						
						
						return " a.groupId LIKE '"+data+"' ";
						
					case "IPElement":
						return " HOST.ipAddress LIKE '"+data+"' ";
						
					case "NameElement":
						return " HOST.hostId LIKE '"+data+"' ";
					
					case "DescriptionElement":
						return " HOST.description LIKE '"+data+"' ";
						
					// opsi-Product
					case "SoftwareNameElement":
						product = true;
						return " d.productId LIKE '"+data+"' ";

					case "SoftwareInstallationStatusElement":
						product = true;
						return " IFNULL(d.installationStatus, '') LIKE '"+data+"' ";
					
					case "SoftwareActionResultElement":
						product = true;
						return " IFNULL(d.actionResult, '') LIKE '"+data+"' ";
					
					case "SoftwareRequestElement":
						product = true;
						return " IFNULL(d.actionRequest, '') LIKE '"+data+"' ";
						
					case "SoftwareActionProgressElement":
						product = true;
						return " IFNULL(d.actionProgress, '') LIKE '"+data+"' ";
						
					case "SoftwareLastActionElement":
						product = true;
						return " IFNULL(d.lastAction, '') LIKE '"+data+"' ";
						
					case "SoftwareVersionElement":
						product = true;
						return " IFNULL(d.productVersion, '') LIKE '"+data+"' ";
						
					case "SoftwarePackageVersionElement":
						product = true;
						return " IFNULL(d.packageVersion, '') LIKE '"+data+"' ";
					
					case "SoftwareModificationTimeElement":
						product = true;
						return getMySQL_SoftwareModificationTime(json.getString("operation"), data);
						
					// Property
					case "PropertyIdElement":
						product = property = true;
						return " h.propertyId LIKE '"+data+"' ";
					
					case "PropertyValueElement":
						product = property = true;
						
						// In der Datenbank sind die 'values' immer in Anführungszeichen,
						// Außnahme: true, false
						if(data.equals("false") || data.equals("true"))
							return " (h.values LIKE '%" + data + "%' OR h.values LIKE '\"%" + data + "\"%') ";
							
						return " h.values LIKE '%\"" + data + "\"%' "; // 'data' should be part of the array
						
						
					// Software-Inventur-Data
					case "SwAuditNameElement":
						software = true;
						return " f.name LIKE '"+data+"' ";
					
					case "SwAuditVersionElement":
						software = true;
						return " f.version LIKE '"+data+"' ";
						
					case "SwAuditSubversionElement":
						software = true;
						return " f.subVersion LIKE '"+data+"' ";
					
					case "SwAuditArchitectureElement":
						software = true;
						return " f.architecture LIKE '"+data+"' ";
						
					case "SwAuditLanguageElement":
						software = true;
						return " f.language LIKE '"+data+"' ";
					
					case "SwAuditSoftwareIdElement":
						software = true;
						return " g.WindowsSoftwareId LIKE '"+data+"' ";
						
					// Hardware
					case "GenericTextElement":
						hardware = true;
						
						String abfrage = setHardware(json);
						
						return " (" + abfrage + " LIKE '" + data + "') ";
					
					case "GenericBigIntegerElement":
					case "GenericIntegerElement":
						hardware = true;
						
						operation = json.getString("operation");
						operation = getOperationFromElement(operation);
						
						abfrage = setHardware(json);
						
						return " (" + abfrage + " " + operation + " '" + data + "') ";
						
					case "GenericDateElement":
					 break;
				}
				
			}
		} catch(Exception e){logging.warning(this, "we did not interpret element selection " + e);}
		
		return "";
	}

	private String setHardware(JSONObject json) throws Exception {

		JSONArray elementPath = json.getJSONArray("elementPath");
		String hardwareType = elementPath.getString(0);
		String column = elementPath.getString(1);
		HashMap element = findHardwareInConfig(hardwareType);
						
		hardwareTableName = (String) ((HashMap) element.get("Class")).get("Opsi");
		List values = (List) element.get("Values");
						
		HashMap spalte = findSpalteInTabelle(column, values);
		String spaltenName = (String) spalte.get("Opsi");
		String scope = (String) spalte.get("Scope");
		
		if(scope.equals("g"))
			hardwareWithDevice = true;
				
		return scope + "." + spaltenName + " NOT LIKE '' AND " + scope + "." + spaltenName; // LEERE FELDER NICHT ERLAUBEN
	}
	
	
	private HashMap findSpalteInTabelle(String column, List values) {
		for(int i=0; i<values.size(); i++)
			if(((HashMap) values.get(i)).get("UI").equals(column))
				return (HashMap) values.get(i);
		
		return null;
	}
	
	private HashMap findHardwareInConfig(String elementPath) {
		for(int i=0; i<hwConfig.size(); i++) {
			if(((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("UI").equals(elementPath))
				return (HashMap) hwConfig.get(i);
		}
		return null;
	}
	
	private String getMySQL_Operation(String operation) {
		return "";
	}
	
	private String getOperationFromElement(String operation) {
		
		switch(operation) {
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
	private String getMySQL_SoftwareModificationTime(String operation, String data) {
		String expression = "";
		switch(operation) {
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
		}
		
		return " DATE(d.modificationTime)" + expression + "'" + data + "'";
	}
	
	public static Type getType(JSONObject json) {
		try {
			switch(json.getString("operation")) {
				case "OrOperation":
					return Type.OR;
				case "AndOperation":
					return Type.AND;
				case "NotOperation":
					return Type.NOT;
			}
		} catch(Exception e){logging.warning("we did get type of operation from " + json);}
			
		// if not a logical operator
		return Type.NEW;
	}
}
