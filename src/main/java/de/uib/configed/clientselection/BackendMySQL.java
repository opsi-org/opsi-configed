package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class BackendMySQL {

	private List<String> alle_hosts;

	private MySQL mySQL_rekursion;

	List<Map<String, Object>> hwConfig;

	// Für die Abfragen an den opsi-Server
	PersistenceController controller;

	public BackendMySQL(PersistenceController controller) {
		this.controller = controller;
		hwConfig = controller.getOpsiHWAuditConf("en_");

		mySQL_rekursion = new MySQL(hwConfig);

		alle_hosts = getListFromSQL("SELECT hostId FROM HOST;");

		
		// for(int i=0; i<hwConfig.size(); i++) {
		////
		/*
		 * for(int j=0; j<((List)((HashMap)hwConfig.get(i)).get("Values")).size(); j++)
		 * {
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get("Type"
		 * ));
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get("Opsi"
		 * ));
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get("WMI")
		 * );
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get(
		 * "Scope"));
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get(
		 * "Linux"));
		 * logging.info(this,
		 * ((HashMap)((List)((HashMap)hwConfig.get(i)).get("Values")).get(j)).get("OSX")
		 * );
		 * //
		 * }
		 * 
		 * //
		 * //
		 * logging.info(this,
		 * ((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("Opsi").getClass());
		 * logging.info(this,
		 * ((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("WMI").getClass());
		 * logging.info(this,
		 * ((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("Linux").getClass());
		 * logging.info(this,
		 * ((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("UI").getClass());
		 * logging.info(this,
		 * ((HashMap)((HashMap)hwConfig.get(i)).get("Class")).get("OSX").getClass());
		 * 
		 * //
		 */
		
		// }
	}

	public List<String> getListFromSQL(String abfrage) {

		logging.info(this, abfrage);

		List<List<java.lang.String>> clients = controller.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { abfrage }));

		List<String> list = new ArrayList<>();

		for (int i = 0; i < clients.size(); i++) {
			list.add(clients.get(i).get(0));
		}

		return list;
	}

	// ENDE lokales mysql

	public List<String> AND(JSONArray children) throws Exception {

		List<String> result = new ArrayList<>(alle_hosts);

		for (int i = 0; i < children.length(); i++) {
			List<String> list_i = getListFromJSONObject((JSONObject) children.get(i));

			result = intersection(result, list_i);
		}

		return result;
	}

	public List<String> OR(JSONArray children) throws Exception {
		List<String> result = new ArrayList<>();

		for (int i = 0; i < children.length(); i++) {
			List<String> list_i = getListFromJSONObject((JSONObject) children.get(i));
			

			result = union(result, list_i);

		}
		return result;
	}

	// Nimmt alle Clients und entfernt die gesuchten
	public List<String> NOT(JSONArray children) throws Exception {
		List<String> result = new ArrayList<>(alle_hosts);

		List<String> c = getListFromJSONObject((JSONObject) children.get(0));

		result.removeAll(c);

		return result;
	}

	public <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<>(set);
	}

	public <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<>();

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public List<String> getListFromJSONObject(JSONObject jsonObject) {
		List<String> list = null;

		try {
			if (jsonObject.isNull("element")) {

				JSONArray children;

				switch (jsonObject.getString("operation")) {
				case "OrOperation":
					children = jsonObject.getJSONArray("children");

					return OR(children);

				case "AndOperation":
					children = jsonObject.getJSONArray("children");

					return AND(children);

				case "NotOperation":
					children = jsonObject.getJSONArray("children");

					return NOT(children);

				case "SoftwareOperation": // PRODUCT

					String where_clause = doJSONObject(jsonObject, true, MySQL.Type.NEW);
					String inner_joins = mySQL_rekursion.getMySQL_INNERJOINS();

					// Diese Abfrage liefert alle hostIds, die ein 'passendes' Produkt in
					// PRODUCT_ON_CLIENT haben
					String abfrage = "SELECT DISTINCT HOST.hostId FROM HOST " + inner_joins + " WHERE " + where_clause
							+ ";";

					String where_clause2 = getWhereClauseDefaultProduct(where_clause);

					// Diese Abfrage soll alle hostIds liefern, die ein 'passendes' Produkt haben,
					// das nicht in PRODUCT_ON_CLIENT ist. Entsprechend werden die Standardwerte
					// abgefragt.
					String abfrage2 = "SELECT DISTINCT HOST.hostId FROM HOST CROSS JOIN PRODUCT d WHERE ( "
							+ where_clause2
							+ " ) AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE d.productId LIKE PRODUCT_ON_CLIENT.productId AND hostId LIKE PRODUCT_ON_CLIENT.clientId);";

					List<String> list1 = getListFromSQL(abfrage);
					List<String> list2 = getListFromSQL(abfrage2);

					return union(list1, list2);

				case "PropertiesOperation":

					where_clause = doJSONObject(jsonObject, true, MySQL.Type.NEW);
					inner_joins = mySQL_rekursion.getMySQL_INNERJOINS();

					// Diese Abfrage liefert alle hostIds, die ein 'passendes' Produkt in
					// PRODUCT_ON_CLIENT haben
					where_clause = where_clause.replace("d.productId", "h.productId");
					abfrage = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.objectId LIKE HOST.hostId) WHERE "
							+ where_clause + ";";

					// Abfrage 2 mit Standardwerten
					where_clause2 = getWhereClauseDefaultProductProperty(where_clause);

					where_clause2 = where_clause2.replace("%\"", "");
					where_clause2 = where_clause2.replace("\"%", "");
					where_clause2 = where_clause2.replace("h.productId", "v.productId");

					abfrage2 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_VALUE v WHERE v.isDefault LIKE '1' AND ("
							+ where_clause2
							+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE v.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

					// Abfragen

					list1 = getListFromSQL(abfrage);
					list2 = getListFromSQL(abfrage2);

					return union(list1, list2);

				case "SoftwareWithPropertiesOperation":

					where_clause = doJSONObject(jsonObject, true, MySQL.Type.NEW);
					inner_joins = mySQL_rekursion.getMySQL_INNERJOINS();

					// Diese Abfrage liefert alle hostIds, die ein 'passendes' Produkt in
					// PRODUCT_ON_CLIENT haben
					abfrage = "SELECT DISTINCT HOST.hostId FROM HOST " + inner_joins + " WHERE " + where_clause + ";";

					where_clause2 = getWhereClauseDefaultProduct(where_clause);
					String where_clause3 = getWhereClauseDefaultProductProperty(where_clause); // v an die Abfragen
																								// schreiben, da
																								// hier in
					String where_clause4 = getWhereClauseDefaultProductProperty(where_clause2); // PRODUCT_PROPERTY_STATE
																								// v abgefragt wird

					// Diese Abfrage soll alle hostIds liefern, die ein 'passendes' Produkt haben,
					// das nicht in PRODUCT_ON_CLIENT ist, aber
					// eine 'passende' Property, die in PRODUCT_PROPERTY_STATE eingetragen ist
					// Entsprechend werden die Standardwerte für PRODUCT abgefragt.

					abfrage2 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.objectId LIKE HOST.hostId) INNER JOIN PRODUCT d ON (d.productId LIKE h.productId) WHERE ("
							+ where_clause2
							+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE h.productId LIKE PRODUCT_ON_CLIENT.productId AND d.productVersion LIKE PRODUCT_ON_CLIENT.productVersion AND d.packageVersion LIKE PRODUCT_ON_CLIENT.packageVersion AND hostId LIKE PRODUCT_ON_CLIENT.clientId);";

					// Diese Abfrage soll alle hostIds liefern, die eine 'passende' Product-Property
					// haben, die nicht in PRODUCT_PROPERTY_STATE ist,
					// aber ein 'passendes' Produkt, das in PRODUCT_ON_CLIENT ist
					// Entsprechend werden die Standardwerte abgefragt.
					where_clause3 = where_clause3.replace("%\"", "");
					where_clause3 = where_clause3.replace("\"%", "");
					String abfrage3 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_ON_CLIENT d ON (HOST.hostId=d.clientId) INNER JOIN PRODUCT_PROPERTY_VALUE v ON (v.productId LIKE d.productId) WHERE v.isDefault LIKE '1' AND ("
							+ where_clause3
							+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE d.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

					// Diese Abfrage dient der Suche nach hostIds, die ein 'passendes' Produkt
					// haben, das nicht in PRODUCT_ON_CLIENT ist, und eine
					// 'passende' PRODUCT_PROPERTY, die nicht in PRODUCT_PROPERTY_STATE ist.
					where_clause4 = where_clause4.replace("%\"", "");
					where_clause4 = where_clause4.replace("\"%", "");

					String abfrage4 = "SELECT DISTINCT HOST.hostId FROM HOST CROSS JOIN PRODUCT_PROPERTY_VALUE v INNER JOIN PRODUCT d ON (d.productId LIKE v.productId AND d.productVersion LIKE v.productVersion AND d.packageVersion LIKE v.packageVersion) WHERE v.isDefault like '1' AND ( "
							+ where_clause4
							+ " ) AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE v.productId LIKE PRODUCT_ON_CLIENT.productId AND hostId LIKE PRODUCT_ON_CLIENT.clientId) AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE v.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

					// Abfragen

					list1 = getListFromSQL(abfrage);
					list2 = getListFromSQL(abfrage2);
					List<String> list3 = getListFromSQL(abfrage3);
					List<String> list4 = getListFromSQL(abfrage4);

					// Die Vereinigung aller Listen zurückgeben
					return union(union(list1, list2), union(list3, list4));

				case "HardwareOperation":
					abfrage = doJSONObject(jsonObject, true, MySQL.Type.NEW);

					inner_joins = mySQL_rekursion.getMySQL_INNERJOINS();

					abfrage = "SELECT DISTINCT HOST.hostId FROM HOST " + inner_joins + " WHERE i.state LIKE 1 AND ("
							+ abfrage + ")";

					return getListFromSQL(abfrage);

				case "SwAuditOperation":
					abfrage = doJSONObject(jsonObject, true, MySQL.Type.NEW);

					inner_joins = mySQL_rekursion.getMySQL_INNERJOINS();

					abfrage = "SELECT DISTINCT HOST.hostId FROM HOST " + inner_joins + " WHERE " + abfrage;

					return getListFromSQL(abfrage);

				default:
					JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("children");
					return getListFromJSONObject((JSONObject) jsonArray.get(0));
				}
			} else if (jsonObject.getJSONArray("elementPath").getString(0).equals("GroupWithSubgroups")) { // Group with
																											// subgroups
				return getGroupWithSubgroup(jsonObject.getString("data"));
			} else {

				MySQL mySQL = new MySQL(hwConfig);
				String abfrage = mySQL.getMySQLforJSONObject(jsonObject);

				String inner_joins = mySQL.getMySQL_INNERJOINS();

				abfrage = "SELECT DISTINCT HOST.hostId FROM HOST " + inner_joins + " WHERE " + abfrage;

				return getListFromSQL(abfrage);
			}
		} catch (Exception e) {
		}

		/*
		 * List<List<java.lang.String>> result
		 * = controller.exec.getListOfStringLists(
		 * new OpsiMethodCall(
		 * "getRawData",
		 * new Object[]{abfrage}
		 * )
		 * );
		 */

		// for(int i=0; i<result.size(); i++) {
		// arrayList.add(result.get(i).get(0));
		// }

		return list;
	}

	private List<String> getGroupWithSubgroup(String groupname) {
		List<String> subgroups = getListFromSQL(
				"SELECT groupId FROM `GROUP` WHERE parentGroupId LIKE '" + groupname + "'");

		List<String> clients = getListFromSQL(
				"SELECT hostId FROM HOST INNER JOIN OBJECT_TO_GROUP a ON (HOST.hostId=a.objectId AND a.groupType LIKE 'HostGroup' ) WHERE a.groupId LIKE '"
						+ groupname + "'");

		for (int i = 0; i < subgroups.size(); i++) {
			for (int j = 0; j < 100; j++)
				logging.info(this, subgroups.get(i));
			clients = union(clients, getGroupWithSubgroup(subgroups.get(i)));
		}

		return clients;
	}

	public void printList(List<String> l) {
		for (int i = 0; i < l.size(); i++) {
			logging.info(this, l.get(i));
		}
	}

	
	// Es müssen entsprechend noch die Standardwerte anstelle der Platzhalter
	// ergänzt werden.
	public String getWhereClauseDefaultProduct(String abfrage) {
		abfrage = abfrage.replace("d.installationStatus", "'not_installed'");
		abfrage = abfrage.replace("d.actionResult", "'none'");
		abfrage = abfrage.replace("d.actionRequest", "'none'");
		abfrage = abfrage.replace("d.actionProgress", "''");
		abfrage = abfrage.replace("d.lastAction", "'none'");
		abfrage = abfrage.replace("d.productVersion", "''");
		abfrage = abfrage.replace("d.packageVersion", "''");
		abfrage = abfrage.replace("DATE(d.modificationTime)", "'IRGENDWAS MIT DATUM'"); // Was nie modifiziert wurde,
																						// soll hier auch nie kommen

		return abfrage;
	}

	/*
	 * Diese Funktion trägt die Standardwerte für die Product-Properties ein.
	 * Muss aber natürlich noch angepasst werden, da die Standardwerte vom Produkt
	 * abhängen und das muss daher dynamischer gestaltet werden...
	 */
	public String getWhereClauseDefaultProductProperty(String abfrage) {

		abfrage = abfrage.replace("h.propertyId", "v.propertyId");
		abfrage = abfrage.replace("h.values", "v.value");
		abfrage = abfrage.replace("h.isDefault", "v.isDefault");

		// Weil in PRODUCT_PROPERTY_VALUE 'false' als 0 und 'true' als 1 abgespeichert
		// wird
		abfrage = abfrage.replace("%false%", "0");
		abfrage = abfrage.replace("%true%", "1");

		return abfrage;
	}

	

	/*
	 * Der Teil hier wird eigentlich nur für das Testen mit der lokalen
	 * mySQL-Datenbank benötigt
	 * 
	 * 
	 * // Connect to SQL Database
	 * public static Connection getConnection() throws SQLException, IOException {
	 * Properties props = new Properties();
	 * FileInputStream in = new FileInputStream("database.properties");
	 * props.load(in);
	 * in.close();
	 * 
	 * String drivers = props.getProperty("jdbc.drivers");
	 * 
	 * //if(drivers != null)
	 * //System.setProperty("jdbc.drivers", idbc.drivers);
	 * 
	 * 
	 * System.setProperty("jdbc.drivers", "com.mysql.idbc.Driver");
	 * 
	 * String url = props.getProperty("jdbc.url");
	 * String username = props.getProperty("jdbc.username");
	 * String password = props.getProperty("jdbc.password");
	 * 
	 * return DriverManager.getConnection(url, username, password);
	 * }
	 * 
	 * public static String getAbfrage(int lineNumber) {
	 * try {
	 * String line;
	 * BufferedReader bufferedReader = new BufferedReader(new
	 * FileReader("gespeicherte_abfragen.txt"));
	 * for(int i=1; i<lineNumber; i++)
	 * bufferedReader.readLine();
	 * 
	 * return bufferedReader.readLine();
	 * } catch (Exception e) { logging.info(this, e);}
	 * 
	 * return null;
	 * }
	 * 
	 * 
	 * public List<String> getListFromSQL(String abfrage) {
	 * 
	 * 
	 * ArrayList arrayList = new ArrayList<>();
	 * 
	 * try {
	 * ResultSet result = stat.executeQuery(abfrage);
	 * 
	 * 
	 * 
	 * 
	 * while(result.next())
	 * arrayList.add(result.getString(1));
	 * 
	 * } catch(Exception e) {}
	 * 
	 * 
	 * return (List<String>) arrayList;
	 * }
	 */

	public List<String> getClientListFromJSONString(String abfrage) {

		logging.info(this, abfrage);

		try {
			JSONObject jsonObject = new JSONObject(abfrage);

			if (jsonObject.has("data"))
				return getListFromJSONObject((JSONObject) jsonObject.getJSONObject("data"));

		} catch (JSONException e) {
			logging.warning(this, "" + e);
		}

		return null;
	}

	private String doJSONObject(JSONObject jsonObject, boolean first, MySQL.Type type) {

		if (jsonObject.isNull("element")) {
			MySQL.Type newType = MySQL.getType(jsonObject);
			try {
				return doJSONArray((JSONArray) jsonObject.getJSONArray("children"), newType);

			} catch (Exception e) {
				logging.warning(this, "" + e);
			}

		} else
			return mySQL_rekursion.getMySQLforJSONObject(jsonObject);

		return "";
	}

	private String doJSONArray(JSONArray jsonArray, MySQL.Type type) {
		int length = jsonArray.length();
		String mysql = " ( ";

		for (int i = 0; i < length; i++) {
			try {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);

				if (i == 0) {
					if (type == MySQL.Type.NOT)
						mysql += " " + type;
				} else {
					if (type == MySQL.Type.AND || type == MySQL.Type.OR)
						mysql += type;
				}

				mysql += doJSONObject(jsonObject, i == 0, MySQL.Type.NEW);
			} catch (Exception e) {
				logging.warning(this, "" + e);
			}
		}

		return mysql + " ) ";
	}
}
