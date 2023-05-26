/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class BackendMySQL {

	private static final String KEY_CHILDREN = "children";
	private static final String STRING_MYSQL_WHERE = " WHERE ";
	private static final String STRING_MYSQL_BASIC_SELECT_STATEMENT = "SELECT DISTINCT HOST.hostId FROM HOST ";

	private static final String KEY_DEFAULT_VALUE_NOT_INSTALLED = "'not_installed'";
	private static final String KEY_DEFAULT_VALUE_NONE = "'none'";
	private static final String KEY_DEFAULT_VALUE_EMPTY = "''";

	private enum Type {
		OR, AND, NOT, NEW
	}

	private List<String> allHosts;

	private List<Map<String, List<Map<String, Object>>>> hwConfig;

	// For the queries to the opsi-server
	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public BackendMySQL() {
		hwConfig = persistenceController.getOpsiHWAuditConf("en_");

		allHosts = getListFromSQL("SELECT hostId FROM HOST;");
	}

	private List<String> getListFromSQL(String query) {

		Logging.info(this, query);

		List<List<String>> clients = persistenceController.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));

		List<String> list = new ArrayList<>();

		for (int i = 0; i < clients.size(); i++) {
			list.add(clients.get(i).get(0));
		}

		return list;
	}

	public List<String> and(JSONArray children) throws JSONException {

		List<String> result = new ArrayList<>(allHosts);

		for (int i = 0; i < children.length(); i++) {
			List<String> list = getListFromJSONObject((JSONObject) children.get(i));

			result = intersection(result, list);
		}

		return result;
	}

	public List<String> or(JSONArray children) throws JSONException {
		List<String> result = new ArrayList<>();

		for (int i = 0; i < children.length(); i++) {
			List<String> list = getListFromJSONObject((JSONObject) children.get(i));

			result = union(result, list);

		}
		return result;
	}

	// Nimmt alle Clients und entfernt die gesuchten
	public List<String> not(JSONArray children) throws JSONException {
		List<String> result = new ArrayList<>(allHosts);

		List<String> c = getListFromJSONObject((JSONObject) children.get(0));

		result.removeAll(c);

		return result;
	}

	public List<String> union(List<String> list1, List<String> list2) {
		Set<String> set = new HashSet<>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<>(set);
	}

	public <T> List<T> intersection(Iterable<T> list1, Collection<T> list2) {
		List<T> list = new ArrayList<>();

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public List<String> getListFromJSONObject(JSONObject jsonObject) {

		MySQL mySQLRecursion = new MySQL(hwConfig);

		if (jsonObject.isNull("element")) {

			JSONArray children;

			switch (jsonObject.getString("operation")) {
			case "OrOperation":
				children = jsonObject.getJSONArray(KEY_CHILDREN);

				return or(children);

			case "AndOperation":
				children = jsonObject.getJSONArray(KEY_CHILDREN);

				return and(children);

			case "NotOperation":
				children = jsonObject.getJSONArray(KEY_CHILDREN);

				return not(children);

			case "SoftwareOperation": // PRODUCT

				String whereClause = doJSONObject(mySQLRecursion, jsonObject);
				String innerJoins = mySQLRecursion.getMySQLInnerJoins();

				// This query gives all hostIds that have a 'fitting' product in PRODUCT_ON_CLIENT
				String query = STRING_MYSQL_BASIC_SELECT_STATEMENT + innerJoins + STRING_MYSQL_WHERE + whereClause
						+ ";";

				String whereClause2 = getWhereClauseDefaultProduct(whereClause);

				/**
				 * This query gives all hostIds that have a 'fitting' product,
				 * that is not in PRODUCT_ON_CLIENT. So it will be asked if the
				 * default values are ok
				 */
				String query2 = "SELECT DISTINCT HOST.hostId FROM HOST CROSS JOIN PRODUCT d WHERE ( " + whereClause2
						+ " ) AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE d.productId LIKE PRODUCT_ON_CLIENT.productId AND hostId LIKE PRODUCT_ON_CLIENT.clientId);";

				List<String> list1 = getListFromSQL(query);
				List<String> list2 = getListFromSQL(query2);

				return union(list1, list2);

			case "PropertiesOperation":

				whereClause = doJSONObject(mySQLRecursion, jsonObject);

				// Gives all hostIds that have a 'fitting' product in PRODUCT_ON_CLIENT
				whereClause = whereClause.replace("d.productId", "h.productId");
				query = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.objectId LIKE HOST.hostId) WHERE "
						+ whereClause + ";";

				// Query 2 with standard values
				whereClause2 = getWhereClauseDefaultProductProperty(whereClause);

				whereClause2 = whereClause2.replace("%\"", "");
				whereClause2 = whereClause2.replace("\"%", "");
				whereClause2 = whereClause2.replace("h.productId", "v.productId");

				query2 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_VALUE v WHERE v.isDefault LIKE '1' AND ("
						+ whereClause2
						+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE v.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

				// queries

				list1 = getListFromSQL(query);
				list2 = getListFromSQL(query2);

				return union(list1, list2);

			case "SoftwareWithPropertiesOperation":

				whereClause = doJSONObject(mySQLRecursion, jsonObject);
				innerJoins = mySQLRecursion.getMySQLInnerJoins();

				// Gives all hostIds that have a 'fitting' product in PRODUCT_ON_CLIENT
				query = STRING_MYSQL_BASIC_SELECT_STATEMENT + innerJoins + STRING_MYSQL_WHERE + whereClause + ";";

				whereClause2 = getWhereClauseDefaultProduct(whereClause);
				String whereClause3 = getWhereClauseDefaultProductProperty(whereClause);
				String whereClause4 = getWhereClauseDefaultProductProperty(whereClause2);

				/**
				 * This query gives all hostIds that have a 'fitting' product in
				 * PRODUCT_ON_CLIENT, but a 'fitting' property in
				 * PRODUCT_PROPERTY_STATE. Therefore asking for standard values
				 */
				query2 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_PROPERTY_STATE h ON (h.objectId LIKE HOST.hostId) INNER JOIN PRODUCT d ON (d.productId LIKE h.productId) WHERE ("
						+ whereClause2
						+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE h.productId LIKE PRODUCT_ON_CLIENT.productId AND d.productVersion LIKE PRODUCT_ON_CLIENT.productVersion AND d.packageVersion LIKE PRODUCT_ON_CLIENT.packageVersion AND hostId LIKE PRODUCT_ON_CLIENT.clientId);";

				/**
				 * This query gives all hostIds with a 'fitting'
				 * product-property, that is not in PRODUCT_PROPERTY_STATE, but
				 * has a 'fitting' product in PRODUCT_ON_CLIENT
				 */
				whereClause3 = whereClause3.replace("%\"", "");
				whereClause3 = whereClause3.replace("\"%", "");
				String query3 = "SELECT DISTINCT HOST.hostId FROM HOST INNER JOIN PRODUCT_ON_CLIENT d ON (HOST.hostId=d.clientId) INNER JOIN PRODUCT_PROPERTY_VALUE v ON (v.productId LIKE d.productId) WHERE v.isDefault LIKE '1' AND ("
						+ whereClause3
						+ ") AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE d.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

				/**
				 * This query gives all hostIds that have a 'fitting' product
				 * that is not in PRODUCT_ON_CLIENT and a 'fitting'
				 * PRODUCT_PROPERTY that is not in PRODUCT_PROPERTY_STATE
				 */
				whereClause4 = whereClause4.replace("%\"", "");
				whereClause4 = whereClause4.replace("\"%", "");

				String query4 = "SELECT DISTINCT HOST.hostId FROM HOST CROSS JOIN PRODUCT_PROPERTY_VALUE v INNER JOIN PRODUCT d ON (d.productId LIKE v.productId AND d.productVersion LIKE v.productVersion AND d.packageVersion LIKE v.packageVersion) WHERE v.isDefault like '1' AND ( "
						+ whereClause4
						+ " ) AND NOT EXISTS (SELECT null FROM PRODUCT_ON_CLIENT WHERE v.productId LIKE PRODUCT_ON_CLIENT.productId AND hostId LIKE PRODUCT_ON_CLIENT.clientId) AND NOT EXISTS (SELECT null FROM PRODUCT_PROPERTY_STATE WHERE v.productId LIKE productId AND HOST.hostId LIKE objectId AND v.propertyId LIKE propertyId);";

				// queries

				list1 = getListFromSQL(query);
				list2 = getListFromSQL(query2);
				List<String> list3 = getListFromSQL(query3);
				List<String> list4 = getListFromSQL(query4);

				// union of all lists
				return union(union(list1, list2), union(list3, list4));

			case "HardwareOperation":
				query = doJSONObject(mySQLRecursion, jsonObject);

				innerJoins = mySQLRecursion.getMySQLInnerJoins();

				query = STRING_MYSQL_BASIC_SELECT_STATEMENT + innerJoins + " WHERE i.state LIKE 1 AND (" + query + ")";

				return getListFromSQL(query);

			case "SwAuditOperation":
				query = doJSONObject(mySQLRecursion, jsonObject);

				innerJoins = mySQLRecursion.getMySQLInnerJoins();

				query = STRING_MYSQL_BASIC_SELECT_STATEMENT + innerJoins + STRING_MYSQL_WHERE + query;

				return getListFromSQL(query);

			default:
				JSONArray jsonArray = jsonObject.getJSONArray(KEY_CHILDREN);
				return getListFromJSONObject((JSONObject) jsonArray.get(0));
			}
		} else if ("GroupWithSubgroups".equals(jsonObject.getJSONArray("elementPath").getString(0))) {
			// Group with subgroups

			return getGroupWithSubgroup(jsonObject.getString("data").replace("*", "%"));
		} else {
			MySQL mySQL = new MySQL(hwConfig);
			String query = mySQL.getMySQLforJSONObject(jsonObject);

			String innerJoins = mySQL.getMySQLInnerJoins();

			query = STRING_MYSQL_BASIC_SELECT_STATEMENT + innerJoins + STRING_MYSQL_WHERE + query;

			return getListFromSQL(query);
		}
	}

	private List<String> getGroupWithSubgroup(String groupname) {
		List<String> subgroups = getListFromSQL(
				"SELECT groupId FROM `GROUP` WHERE parentGroupId LIKE '" + groupname + "'");

		List<String> clients = getListFromSQL(
				"SELECT hostId FROM HOST INNER JOIN OBJECT_TO_GROUP a ON (HOST.hostId=a.objectId AND a.groupType LIKE 'HostGroup' ) WHERE a.groupId LIKE '"
						+ groupname + "'");

		for (int i = 0; i < subgroups.size(); i++) {
			clients = union(clients, getGroupWithSubgroup(subgroups.get(i)));
		}

		return clients;
	}

	public void printList(List<String> l) {
		for (int i = 0; i < l.size(); i++) {
			Logging.info(this, l.get(i));
		}
	}

	// For replacing it with the standard values
	public String getWhereClauseDefaultProduct(String query) {
		query = query.replace("d.installationStatus", KEY_DEFAULT_VALUE_NOT_INSTALLED);
		query = query.replace("d.actionResult", KEY_DEFAULT_VALUE_NONE);
		query = query.replace("d.actionRequest", KEY_DEFAULT_VALUE_NONE);
		query = query.replace("d.actionProgress", KEY_DEFAULT_VALUE_EMPTY);
		query = query.replace("d.lastAction", KEY_DEFAULT_VALUE_NONE);
		query = query.replace("d.productVersion", KEY_DEFAULT_VALUE_EMPTY);
		query = query.replace("d.packageVersion", KEY_DEFAULT_VALUE_EMPTY);

		// TODO Date
		query = query.replace("DATE(d.modificationTime)", "'IRGENDWAS MIT DATUM'");

		return query;
	}

	// Change this for asking for default values on product properties
	public String getWhereClauseDefaultProductProperty(String query) {

		query = query.replace("h.propertyId", "v.propertyId");
		query = query.replace("h.values", "v.value");
		query = query.replace("h.isDefault", "v.isDefault");

		// IN PRODUCT_PROPERTY_VALUE 'false' is saved as 0 and 'true' as 1 
		query = query.replace("%false%", "0");
		query = query.replace("%true%", "1");

		return query;
	}

	public List<String> getClientListFromJSONString(String query) {
		Logging.info(this, query);

		try {
			JSONObject jsonObject = new JSONObject(query);

			if (jsonObject.has("data")) {
				return getListFromJSONObject(jsonObject.getJSONObject("data"));
			}
		} catch (JSONException e) {
			Logging.warning(this, "" + e);
		}

		return new ArrayList<>();
	}

	private String doJSONObject(MySQL mySQLRecursion, JSONObject jsonObject) {

		if (jsonObject.isNull("element")) {
			Type newType = getType(jsonObject);
			try {
				return doJSONArray(mySQLRecursion, jsonObject.getJSONArray(KEY_CHILDREN), newType);

			} catch (Exception e) {
				Logging.warning(this, "" + e);
			}
		} else {
			return mySQLRecursion.getMySQLforJSONObject(jsonObject);
		}

		return "";
	}

	private String doJSONArray(MySQL mySQLRecursion, JSONArray jsonArray, Type type) {
		int length = jsonArray.length();
		StringBuilder mysql = new StringBuilder(" ( ");

		for (int i = 0; i < length; i++) {
			try {

				if (i == 0) {
					if (type == Type.NOT) {
						mysql.append(" " + type);
					}
				} else if (type == Type.AND || type == Type.OR) {
					mysql.append(type);
				}

				mysql.append(doJSONObject(mySQLRecursion, (JSONObject) jsonArray.get(i)));
			} catch (Exception e) {
				Logging.warning(this, "" + e);
			}
		}

		return mysql + " ) ";

	}

	private static Type getType(JSONObject json) {
		try {
			switch (json.getString(MySQL.KEY_OPERATION)) {
			case "OrOperation":
				return Type.OR;
			case "AndOperation":
				return Type.AND;
			case "NotOperation":
				return Type.NOT;
			default:
				return Type.NEW;
			}
		} catch (JSONException e) {
			Logging.warning("we did get type of operation from " + json, e);
			return Type.NEW;
		}
	}
}
