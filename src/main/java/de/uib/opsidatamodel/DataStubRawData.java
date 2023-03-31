/**
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2014-2018
 *  organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.opsidatamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.type.ConfigStateEntry;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsicommand.JSONthroughHTTPS;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.dbtable.Host;
import de.uib.opsidatamodel.dbtable.ProductPropertyState;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;

public class DataStubRawData extends DataStubNOM {

	private static final String STATE_IS_1 = ".state = 1 ";
	static final String CELL_SEPARATOR = "|";

	public DataStubRawData(OpsiserviceNOMPersistenceController controller) {
		super(controller);
	}

	// can be used if we do not need table specific translations of key names and
	// value types
	protected List<Map<String, Object>> retrieveListOfMapsBySQLselect(List<String> columns, String tables,
			String condition) {
		StringBuilder sb = new StringBuilder("select");

		if (columns == null || columns.isEmpty()) {
			sb.append(" * ");
		} else {
			sb.append(" ");
			sb.append(columns.get(0));
			for (int i = 1; i < columns.size(); i++) {
				sb.append(", ");
				sb.append(columns.get(i));
			}
		}
		sb.append("\n from ");
		sb.append(tables);
		sb.append("where \n");
		sb.append(condition);

		String query = sb.toString();

		List<List<String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));
		for (List<String> row : rows) {
			Logging.info(this, "sql produced row " + row);
		}

		return new ArrayList<>();

	}

	@Override
	public boolean canCallMySQL() {

		// we cannot call MySQL if version before 4.3
		if (JSONthroughHTTPS.isOpsi43()) {
			return false;
		}

		boolean result = false;

		// test if we can access any table

		String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME + " LIMIT 1 ";

		Logging.info(this, "test, query " + query);

		result = persist.exec.doCall(new OpsiMethodCall("getRawData", new Object[] { query }));

		Logging.info(this, "test result " + result);
		return result;
	}

	protected String giveWhereOR(String colName, List<String> values) {
		if (values == null || values.isEmpty()) {
			return "true";
		}

		StringBuilder result = new StringBuilder(colName + " = '" + values.get(0) + "'");

		int lineCount = 0;

		for (int i = 1; i < values.size(); i++) {
			result.append(" OR ");
			result.append(colName);
			result.append(" = '");
			result.append(values.get(i));
			result.append("'      ");
			lineCount++;
			if (lineCount == 100) {
				result.append("\n");
				lineCount = 0;
			}
		}

		return result.toString();
	}

	// netbootStatesAndActions
	// localbootStatesAndActions

	/*
	 * in superclass
	 * 
	 * 
	 * //will only be refreshed when all product data are refreshed
	 * 
	 * 
	 */

	// client is a set of added hosts, host represents the totality and will be
	// update as a side effect

	@Override
	protected List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients,
			Set<String> hosts) {
		Logging.debug(this, "produceProductPropertyStates new hosts " + clients + " old hosts " + hosts);

		List<Map<String, Object>> result = new ArrayList<>();

		List<String> newClients = null;
		if (clients == null) {
			newClients = new ArrayList<>();
		} else {
			newClients = new ArrayList<>(clients);
		}

		if (hosts == null) {
			hosts = new HashSet<>();
		} else {
			newClients.removeAll(hosts);
		}

		Logging.debug(this, "produceProductPropertyStates, new hosts " + clients);

		if (!newClients.isEmpty()) {
			hosts.addAll(newClients);

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " product property state");

			StringBuilder cols = new StringBuilder("");
			cols.append(ProductPropertyState.TABLE_NAME + "." + ProductPropertyState.PRODUCT_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.TABLE_NAME + "." + ProductPropertyState.PROPERTY_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.TABLE_NAME + "." + ProductPropertyState.OBJECT_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.TABLE_NAME + "." + ProductPropertyState.VALUES);

			String query = "select \n" + cols.toString() + "\n from " + ProductPropertyState.TABLE_NAME + "\n where "
					+ giveWhereOR(ProductPropertyState.TABLE_NAME + ".objectId", newClients);

			Logging.info(this, "produceProductPropertyStates query " + query);

			List<List<String>> rows = persist.exec
					.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));

			Logging.info(this, "produceProductPropertyStates got rows " + rows.size());
			int counter = 0;

			for (List<String> row : rows) {
				Map<String, Object> m = new HashMap<>();

				m.put(ProductPropertyState.PRODUCT_ID, row.get(0));
				m.put(ProductPropertyState.PROPERTY_ID, row.get(1));
				m.put(ProductPropertyState.OBJECT_ID, row.get(2));

				// parse String and produce json list

				JSONArray values = null;
				try {
					values = new JSONArray(row.get(3));
				} catch (JSONException ex) {
					Logging.warning(this, "produceProductPropertyStates, error when json parsing database string \n"
							+ row.get(3) + " for propertyId " + row.get(1), ex);
				}

				m.put(ProductPropertyState.VALUES, values);
				result.add(m);
				counter++;
			}

			Logging.info(this, "produceProductPropertyStates produced  items " + counter);

		}

		return result;
	}

	@Override
	protected void retrieveSoftwareAuditOnClients(final List<String> clients) {
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Globals.usedMemory());

		retrieveInstalledSoftwareInformation();
		Logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  clients count ======  " + clients.size());

		List<String> newClients = new ArrayList<>(clients);

		if (client2software != null) {
			Logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " + "   +++  "
					+ client2software.keySet().size());

			newClients.removeAll(client2software.keySet());
		}

		Logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  new clients count  ====== " + newClients.size());

		int missingEntries = 0;

		boolean fetchAll = true;

		if (client2software == null || softwareIdent2clients == null || !newClients.isEmpty()) {
			String clientSelection = null;

			if (newClients.size() >= 50) {
				clientSelection = "";
			} else {
				clientSelection = " AND ( " + giveWhereOR("clientId", newClients) + ") ";
				fetchAll = false;

			}

			if (client2software == null) {
				client2software = new HashMap<>();
			}

			if (softwareIdent2clients == null) {
				softwareIdent2clients = new HashMap<>();
			}

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " softwareConfig ");

			Logging.info(this, "retrieveSoftwareAuditOnClients/ SOFTWARE_CONFIG, start a request");

			String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString();
			columns = columns.substring(1);
			columns = columns.substring(0, columns.length() - 1);

			String query = "select " + columns + " from " + SWAuditClientEntry.DB_TABLE_NAME + " \n"
					+ " where  state = 1 " + clientSelection + " order by clientId ";

			Logging.info(this, "retrieveSoftwareAuditOnClients, query " + query);

			List<List<String>> rows = persist.exec
					.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));

			Logging.info(this, "retrieveSoftwareAuditOnClients, finished a request");

			if (rows.isEmpty()) {
				Logging.notice(this, "no auditSoftwareOnClient");
			} else {

				Logging.info(this, "retrieveSoftwareAuditOnClients rows size " + rows.size());

				if (fetchAll) {
					client2software.clear();
				}

				SWAuditClientEntry.summillis1stPartOfConstructor = 0;
				SWAuditClientEntry.summillis2ndPartOfConstructor = 0;

				for (List<String> row : rows) {
					String clientId = row.get(0);

					String swIdent = null;

					List<SWAuditClientEntry> entries = client2software.get(clientId);
					if (entries == null) {
						entries = new LinkedList<>();
						client2software.put(clientId, entries);
					}

					SWAuditClientEntry clientEntry = new SWAuditClientEntry(SWAuditClientEntry.DB_COLUMN_NAMES, row,
							persist);

					swIdent = clientEntry.getSWident();

					Set<String> clientsWithThisSW = softwareIdent2clients.get(swIdent);
					if (clientsWithThisSW == null) {
						clientsWithThisSW = new HashSet<>();

						softwareIdent2clients.put(swIdent, clientsWithThisSW);
					}

					clientsWithThisSW.add(clientId);

					entries.add(clientEntry);

				}

				Logging.info(this, "retrieveSoftwareAuditOnClients needed for 1st part of constructing entries "
						+ SWAuditClientEntry.summillis1stPartOfConstructor);
				Logging.info(this, "retrieveSoftwareAuditOnClients needed for 2nd part of constructing entries "
						+ SWAuditClientEntry.summillis2ndPartOfConstructor);

				newClients.removeAll(client2software.keySet());
				// the remaining clients are without software entry

				for (String clientId : newClients) {
					client2software.put(clientId, new LinkedList<>());
				}

			}

			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end "
					+ Runtime.getRuntime().totalMemory() / 1_000_000 + " MB");

			persist.notifyDataRefreshedObservers("softwareConfig");
		}

		Logging.info(this, " retrieveSoftwareAuditOnClients reports missingEntries " + missingEntries
				+ " whereas softwareList has entries " + softwareList.size());
	}

	/*
	 * getAuditSoftwareUsage
	 * 
	 * select count(*) as Anzahl, name, version, subversion, language, architecture
	 * from SOFTWARE_CONFIG group by name, version, subversion, language,
	 * architecture order by name, version, subversion, language, architecture 
	 */

	@Override
	protected void retrieveHostConfigs() {

		if (hostConfigs != null) {
			return;
		}

		Logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.loadtable") + " config state");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();
		Logging.info(this, "  retrieveHostConfigs ( CONFIG_STATE )  start a request");

		// json parsing for integer value false thereforw we omit the ID column

		String columns = ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.OBJECT_ID + ", "
				+ ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.CONFIG_ID + ", "
				+ ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.VALUES_ID;

		String query = "select " + columns + " from " + ConfigStateEntry.DB_TABLE_NAME + " ";

		Logging.info(this, "retrieveHostConfigs, query " + query);

		List<List<String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query })

				);

		Logging.info(this, "retrieveHostConfigs, finished a request");

		hostConfigs = new HashMap<>();

		if (rows.isEmpty()) {
			Logging.warning(this, "no host config rows " + rows);
		} else {
			Logging.info(this, "retrieveHostConfigs rows size " + rows.size());

			for (List<String> row : rows) {
				String hostId = row.get(0);

				Map<String, Object> configs1Host = hostConfigs.get(hostId);
				if (configs1Host == null) {
					configs1Host = new HashMap<>();
					hostConfigs.put(hostId, configs1Host);
				}

				String configId = row.get(1);

				// get values as String
				String valueString = row.get(2);

				// parse String and produce list

				List<Object> values = new ArrayList<>();
				try {
					values = (new JSONArray(valueString)).toList();
				} catch (JSONException ex) {
					Logging.warning(this, "retrieveHostConfigs, error when json parsing database string \n"
							+ valueString + " for configId " + configId, ex);
				}

				// put into host configs
				configs1Host.put(configId, values);

			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved ");
		persist.notifyDataRefreshedObservers("configState");

	}

	private static String maxTime(String time0, String time1) {
		if (time0 == null && time1 == null) {
			return null;
		}

		if (time0 == null || time0.equals("")) {
			return time1;
		}

		if (time1 == null || time1.equals("")) {
			return time0;
		}

		if (time0.compareTo(time1) < 0) {
			return time1;
		}

		return time0;
	}

	private Map<String, Map<String, Object>> client2HwRowsForHwClass(String hwClass) {
		Logging.info(this, "client2HwRowsForHwClass " + hwClass);

		if (client2HwRows == null) {
			return new HashMap<>();
		}

		// z.B. hwClass is DISK_PARTITION

		List<String> specificColumns = new ArrayList<>();
		specificColumns.add(Host.ID_COLUMN);

		StringBuilder buf = new StringBuilder("select HOST.hostId, ");
		StringBuilder cols = new StringBuilder("");

		String configTable = AbstractPersistenceController.HW_INFO_CONFIG + hwClass;

		String lastseenCol = configTable + "." + AbstractPersistenceController.LAST_SEEN_COL_NAME;
		specificColumns.add(lastseenCol);
		buf.append(lastseenCol);
		buf.append(", ");

		boolean foundAnEntry = false;

		// build and collect database columnnames
		for (String hwInfoCol : persist.getClient2HwRowsColumnNames()) {
			if (hwInfoCol.startsWith("HOST.")
					|| hwInfoCol.equals(AbstractPersistenceController.LAST_SEEN_VISIBLE_COL_NAME)) {
				continue;
			}

			Logging.info(this, "hwInfoCol " + hwInfoCol + " look for " + AbstractPersistenceController.HW_INFO_DEVICE
					+ " as well as " + AbstractPersistenceController.HW_INFO_CONFIG);
			String part0 = hwInfoCol.substring(0, AbstractPersistenceController.HW_INFO_DEVICE.length());

			boolean colFound = false;
			// check if colname is from a CONFIG or a DEVICE table
			if (hwInfoCol.startsWith(hwClass, part0.length())) {
				colFound = true;
				// we found a DEVICE column name
			} else {
				part0 = hwInfoCol.substring(0, AbstractPersistenceController.HW_INFO_CONFIG.length());

				if (hwInfoCol.startsWith(hwClass, part0.length())) {
					colFound = true;
					// we found a CONFIG column name
				}

			}

			if (colFound) {
				cols.append(" ");
				cols.append(hwInfoCol);
				cols.append(",");
				specificColumns.add(hwInfoCol);
				foundAnEntry = true;
			}
		}

		if (!foundAnEntry) {
			Logging.info(this, "no columns found for hwClass " + hwClass);
			return new HashMap<>();
		}

		String deviceTable = AbstractPersistenceController.HW_INFO_DEVICE + hwClass;

		String colsS = cols.toString();
		buf.append(colsS.substring(0, colsS.length() - 1));

		buf.append(" \nfrom HOST ");

		buf.append(", ");
		buf.append(deviceTable);
		buf.append(", ");
		buf.append(configTable);

		buf.append("\n where ");

		buf.append(Host.ID_COLUMN);
		buf.append(" = ");
		buf.append(configTable);
		buf.append(AbstractPersistenceController.HOST_ID_FIELD);

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(AbstractPersistenceController.HARDWARE_ID_FIELD);
		buf.append(" = ");
		buf.append(deviceTable);
		buf.append(AbstractPersistenceController.HARDWARE_ID_FIELD);

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(STATE_IS_1);

		String query = buf.toString();

		Logging.info(this, "retrieveClient2HwRows, query " + query);

		List<List<String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query })

				);
		Logging.info(this, "retrieveClient2HwRows, finished a request");
		Logging.info(this, "retrieveClient2HwRows, got rows for class " + hwClass);
		Logging.info(this, "retrieveClient2HwRows, got rows,  size  " + rows.size());

		// shrink to one line per client

		Map<String, Map<String, Object>> clientInfo = new HashMap<>();

		for (List<String> row : rows) {
			Map<String, Object> rowMap = clientInfo.get(row.get(0));
			if (rowMap == null) {
				rowMap = new HashMap<>();
				clientInfo.put(row.get(0), rowMap);
			}

			for (int i = 1; i < specificColumns.size(); i++) {
				Object value = rowMap.get(specificColumns.get(i));
				String valInRow = row.get(i);
				if (valInRow == null || valInRow.equals("null")) {
					valInRow = "";
				}

				if (value == null) {
					value = valInRow;
				} else {
					value = value + CELL_SEPARATOR + valInRow;
				}

				if (specificColumns.get(i).equals(lastseenCol)) {
					String timeS = maxTime((String) value, row.get(i));
					rowMap.put(AbstractPersistenceController.LAST_SEEN_VISIBLE_COL_NAME, timeS);
				} else {
					rowMap.put(specificColumns.get(i), value);
				}

			}

		}

		Logging.info(this, "retrieveClient2HwRows, got clientInfo, with size " + clientInfo.size());
		return clientInfo;

		/*
		 * example
		 * SELECT HOST.hostId,
		 * HARDWARE_DEVICE_DISK_PARTITION.name,
		 * HARDWARE_DEVICE_DISK_PARTITION.description
		 * 
		 * from HOST, HARDWARE_DEVICE_DISK_PARTITION, HARDWARE_CONFIG_DISK_PARTITION
		 * where
		 * HOST.hostId = "vbrupertwin7-64.uib.local" and
		 * HARDWARE_DEVICE_DISK_PARTITION.hardware_id =
		 * HARDWARE_CONFIG_DISK_PARTITION.hardware_id
		 * 
		 * and HOST.hostId = HARDWARE_CONFIG_DISK_PARTITION.hostId
		 * 
		 * and HARDWARE_CONFIG_DISK_PARTITION.state=1 
		 * 
		 */

	}

	@Override
	protected void retrieveClient2HwRows(String[] hosts) {
		Logging.info(this, "retrieveClient2HwRows( hosts )  for hosts " + hosts.length);

		if (client2HwRows != null) {
			Logging.info(this, "retrieveClient2HwRows client2HwRows.size() " + client2HwRows.size());
			return;
		}

		client2HwRows = new HashMap<>();

		// set default rows
		for (String host : persist.getHostInfoCollections().getOpsiHostNames()) {
			Map<String, Object> nearlyEmptyHwRow = new HashMap<>();
			nearlyEmptyHwRow.put(Host.ID_COLUMN, host);

			String hostDescription = "";
			String macAddress = "";
			if (persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host) != null) {
				hostDescription = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host).getDescription();
				macAddress = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host).getMacAddress();
			}
			nearlyEmptyHwRow.put(Host.DESCRIPTION_COLUMN, hostDescription);
			nearlyEmptyHwRow.put(Host.HW_ADRESS_COLUMN, macAddress);

			client2HwRows.put(host, nearlyEmptyHwRow);
		}

		persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.loadtable") + " hardware");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveClient2HwRows all ");
		timeCheck.start();

		for (String hwClass : persist.getHwInfoClassNames()) {
			Logging.info(this, "retrieveClient2HwRows hwClass " + hwClass);

			Map<String, Map<String, Object>> client2ClassInfos = client2HwRowsForHwClass(hwClass);

			if (!client2ClassInfos.isEmpty()) {
				for (Entry<String, Map<String, Object>> client2ClassInfo : client2ClassInfos.entrySet()) {
					Map<String, Object> allInfosForAClient = client2HwRows.get(client2ClassInfo.getKey());
					// find max lastseen time as last scan time

					String lastseen1 = (String) allInfosForAClient
							.get(AbstractPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					String lastseen2 = (String) client2ClassInfo.getValue()
							.get(AbstractPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					if (lastseen1 != null && lastseen2 != null) {
						client2ClassInfo.getValue().put(AbstractPersistenceController.LAST_SEEN_VISIBLE_COL_NAME,
								maxTime(lastseen1, lastseen2));
					}

					allInfosForAClient.putAll(client2ClassInfo.getValue());
				}
			}
		}

		Logging.info(this, "retrieveClient2HwRows result size " + client2HwRows.size());

		timeCheck.stop();
		Logging.info(this, "retrieveClient2HwRows finished  ");
		persist.notifyDataRefreshedObservers("client2HwRows");
	}
}
