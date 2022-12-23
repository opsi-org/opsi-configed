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
import java.util.Vector;

import org.json.JSONArray;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.ConfigStateEntry;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.dbtable.Host;
import de.uib.opsidatamodel.dbtable.ProductPropertyState;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.logging.logging;

public class DataStubRawData extends DataStubNOM {

	final String stateIs1 = ".state = 1 ";
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

		List<List<java.lang.String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));
		for (List<String> row : rows) {
			logging.info(this, "sql produced row " + row);
		}

		return null;

	}

	@Override
	public boolean canCallMySQL() {
		boolean result = false;

		// test if we can access any table

		String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME + " LIMIT 1 ";

		logging.info(this, "test, query " + query);

		result = persist.exec.doCall(new OpsiMethodCall("getRawData", new Object[] { query }));

		logging.info(this, "test result " + result);
		return result;
	}

	/*
	 * @Override
	 * protected boolean test()
	 * {
	 * if (!super.test())
	 * return false;
	 * 
	 * //test if we can access any table
	 * 
	 * String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME
	 * + " LIMIT 1 ";
	 * 
	 * logging.info(this, "test, query " + query);
	 * 
	 * boolean result
	 * = persist.exec.doCall(
	 * new OpsiMethodCall(
	 * "getRawData",
	 * new Object[]{query}
	 * )
	 * );
	 * 
	 * logging.info(this, "test result " + result);
	 * return result;
	 * }
	 */

	protected String giveWhereOR(String colName, List<String> values) {
		if (values == null || values.isEmpty())
			return "true";

		StringBuffer result = new StringBuffer(colName + " = '" + values.get(0) + "'");

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

	// ===================================================

	// netbootStatesAndActions
	// localbootStatesAndActions

	// ===================================================

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
	protected List<Map<String, Object>> produceProductPropertyStates(
			// protected List <Map<String, Object>>
			// ENTWURFproduceProductPropertyStates(
			final Collection<String> clients, java.util.Set<String> hosts) {
		logging.debug(this, "produceProductPropertyStates new hosts " + clients + " old hosts " + hosts);

		// List <Map<String, Object>> compareList =
		
		

		List<Map<String, Object>> result = new ArrayList<>();

		List<String> newClients = null;
		if (clients == null)
			newClients = new ArrayList<>();
		else
			newClients = new ArrayList<>(clients);

		if (hosts == null) {
			hosts = new HashSet<>();
		} else {
			newClients.removeAll(hosts);
		}

		logging.debug(this, "produceProductPropertyStates, new hosts " + clients);

		if (newClients.isEmpty()) {
		} else {
			hosts.addAll(newClients);

			
			

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " product property state");

			StringBuilder cols = new StringBuilder("");
			cols.append(ProductPropertyState.tableName + "." + ProductPropertyState.PRODUCT_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.tableName + "." + ProductPropertyState.PROPERTY_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.tableName + "." + ProductPropertyState.OBJECT_ID);
			cols.append(", ");
			cols.append(ProductPropertyState.tableName + "." + ProductPropertyState.VALUES);

			String query = "select \n" + cols.toString() + "\n from " + ProductPropertyState.tableName + "\n where "
					+ giveWhereOR(ProductPropertyState.tableName + ".objectId", newClients);

			logging.info(this, "produceProductPropertyStates query " + query);

			List<List<java.lang.String>> rows = persist.exec
					.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));

			logging.info(this, "produceProductPropertyStates got rows " + rows.size());
			int counter = 0;

			for (List<String> row : rows) {
				Map<String, Object> m = new HashMap<>();

				m.put(ProductPropertyState.PRODUCT_ID, row.get(0));
				m.put(ProductPropertyState.PROPERTY_ID, row.get(1));
				m.put(ProductPropertyState.OBJECT_ID, row.get(2));

				// parse String and produce json list
				
				org.json.JSONArray values = null;
				try {
					values = new org.json.JSONArray(row.get(3));
				} catch (Exception ex) {
					logging.warning(this, "produceProductPropertyStates, error when json parsing database string \n"
							+ row.get(3) + " for propertyId " + row.get(1));
				}

				m.put(ProductPropertyState.VALUES, values);
				result.add(m);
				counter++;
			}

			logging.info(this, "produceProductPropertyStates produced  items " + counter);
			
			

			/*
			 * 
			 * counter = 0;
			 * for (List<String> row : rows)
			 * {
			 * if (row.get(0).equals("firefox") && row.get(1).equals("profilemigrator"))
			 * {
			 * logging.info(this, "sql row  " + counter + ": " + row);
			 * for (int i = 0; i < row.size(); i++)
			 * {
			 * logging.info(this, "sql " + row.get(i));
			 * }
			 * 
			 * Map<String, Object> m = new HashMap<>();
			 * 
			 * m.put( ProductPropertyState.PRODUCT_ID, row.get(0) );
			 * m.put( ProductPropertyState.PROPERTY_ID, row.get(1) );
			 * m.put( ProductPropertyState.OBJECT_ID, row.get(2) );
			 * 
			 * //parse String and produce json list
			 * org.json.JSONArray values = null;
			 * try
			 * {
			 * values = new org.json.JSONArray(row.get(3));
			 * }
			 * catch(Exception ex)
			 * {
			 * logging.warning(this,
			 * "produceProductPropertyStates, error when json parsing database string \n"
			 * + row.get(3) + " for propertyId " + row.get(1) );
			 * }
			 * 
			 * 
			 * m.put( ProductPropertyState.VALUES, values);
			 * 
			 * logging.info(this, " values " + values);
			 * 
			 * result.add(m);
			 * 
			 * counter++;
			 * }
			 * 
			 * }
			 */

			/*
			 * counter = 0;
			 * logging.info(this, "compare to ");
			 * for (Map<String, Object> m : compareList)
			 * {
			 * if (
			 * m.get("productId").equals("firefox")
			 * &&
			 * m.get("propertyId").equals("profilemigrator")
			 * )
			 * 
			 * {
			 * logging.info(this, " .. " + counter);
			 * for (String key : m.keySet())
			 * {
			 * logging.info(this, " key " + key + " value of class "
			 * + m.get(key).getClass().getName() + " : "
			 * + m.get(key) );
			 * 
			 * }
			 * 
			 * 
			 * 
			 * counter++;
			 * }
			 * }
			 * System.exit(0);
			 */

			
		}

		return result;
	}

	// ===================================================

	/*
	 * in superclass
	 * 
	 * protected List <Map<String, Object>> softwareAuditOnClients;
	 * protected Map<String, List <SWAuditClientEntry>> client2software;
	 * 
	 * protected java.sql.Time SOFTWARE_CONFIG_last_entry = null;
	 * 
	 * 
	 * @Override
	 * public void softwareAuditOnClientsRequestRefresh()
	 * {
	 * softwareAuditOnClients = null;
	 * client2software = null;
	 * }
	 * 
	 * 
	 * 
	 * @Override
	 * public void fillClient2Software(String client)
	 * {
	 * logging.info(this, "fillClient2Software " + client);
	 * if (client2software == null)
	 * {
	 * retrieveSoftwareAuditOnClients(client);
	 * 
	 * return;
	 * }
	 * 
	 * if (client2software.get(client) == null)
	 * retrieveSoftwareAuditOnClients(client);
	 * 
	 * }
	 * 
	 * @Override
	 * public void fillClient2Software(List<String> clients)
	 * {
	 * retrieveSoftwareAuditOnClients(clients);
	 * }
	 * 
	 * @Override
	 * public Map<String, List<SWAuditClientEntry>> getClient2Software()
	 * //fill the clientlist by fill ...
	 * {
	 * logging.info(this, "getClient2Software  ============= ");
	 * return client2software;
	 * }
	 * 
	 * @Override
	 * public Map<String, java.util.Set<String>> getSoftwareIdent2clients()
	 * //fill the clientlist by fill ...
	 * {
	 * logging.info(this, "getSoftwareIdent2clients ============= "
	 * 
	 * + softwareIdent2clients );
	 * return softwareIdent2clients;
	 * }
	 * 
	 * 
	 * protected void retrieveSoftwareAuditOnClients()
	 * {
	 * retrieveSoftwareAuditOnClients(new ArrayList<>());
	 * }
	 * 
	 * protected void retrieveSoftwareAuditOnClients(String client)
	 * {
	 * List<String> clients = new ArrayList<>();
	 * clients.add(client);
	 * retrieveSoftwareAuditOnClients(clients);
	 * }
	 */

	@Override
	protected void retrieveSoftwareAuditOnClients(final List<String> clients) {
		logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Globals.usedMemory());

		retrieveInstalledSoftwareInformation();
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  clients count ======  " + clients.size());

		List<String> newClients = new ArrayList<>(clients);

		if (client2software != null) {
			logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " + "   +++  "
					+ client2software.keySet().size());

			newClients.removeAll(client2software.keySet());
		}

		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  new clients count  ====== " + newClients.size());

		int missingEntries = 0;

		boolean fetchAll = true;
		// if (client2software == null || softwareId2clients == null ||
		// newClients.size() > 0)
		if (client2software == null || softwareIdent2clients == null || !newClients.isEmpty()) {
			String clientSelection = null;

			if (newClients.size() >= 50) {
				clientSelection = "";
			} else {
				clientSelection = " AND ( " + giveWhereOR("clientId", newClients) + ") ";
				fetchAll = false;

			}

			
			// clientListForCall.size() + " clients " + clientListForCall);

			
			if (client2software == null)
				client2software = new HashMap<>();
			// if (softwareId2clients == null) softwareId2clients = new HashMap<Integer,
			
			if (softwareIdent2clients == null)
				softwareIdent2clients = new HashMap<>();

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " softwareConfig ");
			// , step " + step);

			logging.info(this, "retrieveSoftwareAuditOnClients/ SOFTWARE_CONFIG, start a request");

			String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString();
			columns = columns.substring(1);
			columns = columns.substring(0, columns.length() - 1);

			/*
			 * String query = "select " + columns + " from " + "HOST, " +
			 * SWAuditClientEntry.DB_TABLE_NAME + " \n"
			 * + " where  state = 1 "
			 * + " and HOST.hostID = " + SWAuditClientEntry.DB_TABLE_NAME + ".clientId "
			 * + " and HOST.type='OpsiClient' "
			 */
			String query = "select " + columns + " from " + SWAuditClientEntry.DB_TABLE_NAME + " \n"
					+ " where  state = 1 " + clientSelection + " order by clientId ";;

			logging.info(this, "retrieveSoftwareAuditOnClients, query " + query);

			List<List<java.lang.String>> rows = persist.exec
					.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query }));

			logging.info(this, "retrieveSoftwareAuditOnClients, finished a request");

			if (rows == null || rows.isEmpty()) {
				logging.notice(this, "no auditSoftwareOnClient");
			} else {

				logging.info(this, "retrieveSoftwareAuditOnClients rows size " + rows.size());

				/*
				 * for (String clientId : clientListForCall)
				 * {
				 * client2software.put(clientId, new LinkedList<>());
				 * }
				 */

				if (fetchAll)
					client2software.clear();

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
					
					// client " + clientId);

					SWAuditClientEntry clientEntry = new SWAuditClientEntry(SWAuditClientEntry.DB_COLUMN_NAMES, row,
							persist);

					
					swIdent = clientEntry.getSWident();

					/*
					 * if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-440") > -1)
					 * {
					 * logging.info(this, " retrieveSoftwareAuditOnClient clientId : swIdent " +
					 * clientId + " : " + swIdent);
					 * }
					 */

					/*
					 * if (clientEntry.getSWid() == -1)
					 * {
					 * missingEntries++;
					 * logging.info("Missing auditSoftware entry for swIdent " +
					 * SWAuditClientEntry.DB_COLUMN_NAMES + " for values: " +
					 * SWAuditClientEntry.produceSWident(SWAuditClientEntry.DB_COLUMN_NAMES, row)
					 * );
					 * 
					 * 
					 * //item.put(SWAuditEntry.WINDOWSsOFTWAREid, "MISSING");
					 * }
					 * else
					 */
					{
						java.util.Set<String> clientsWithThisSW = softwareIdent2clients.get(swIdent);
						if (clientsWithThisSW == null) {
							clientsWithThisSW = new HashSet<>();

							softwareIdent2clients.put(swIdent, clientsWithThisSW);
						}

						/*
						 * 
						 * if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)
						 * {
						 * logging.info(this, "having this subversion " + clientId);
						 * }
						 * 
						 */

						clientsWithThisSW.add(clientId);

						entries.add(clientEntry);
					}

					/*
					 * 
					 * if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)
					 * {
					 * logging.info(this,
					 * " retrieveSoftwareAuditOnClient softwareIdent2clients.get(swIdent) " +
					 * " size " + softwareIdent2clients.get(swIdent).size() + " :: " +
					 * softwareIdent2clients.get(swIdent) );
					 * 
					 * 
					 * }
					 */

				}

				logging.info(this, "retrieveSoftwareAuditOnClients needed for 1st part of constructing entries "
						+ SWAuditClientEntry.summillis1stPartOfConstructor);
				logging.info(this, "retrieveSoftwareAuditOnClients needed for 2nd part of constructing entries "
						+ SWAuditClientEntry.summillis2ndPartOfConstructor);

				newClients.removeAll(client2software.keySet());
				// the remaining clients are without software entry

				for (String clientId : newClients) {
					client2software.put(clientId, new LinkedList<>());
				}

			}

			
			

			logging.info(this, "retrieveSoftwareAuditOnClients used memory on end "
					+ Runtime.getRuntime().totalMemory() / 1000000 + " MB");
			
			

			persist.notifyDataRefreshedObservers("softwareConfig");
		}

		logging.info(this, " retrieveSoftwareAuditOnClients reports missingEntries " + missingEntries
				+ " whereas softwareList has entries " + softwareList.size());
	}

	// ===================================================

	// ===================================================

	/*
	 * getAuditSoftwareUsage
	 * 
	 * select count(*) as Anzahl, name, version, subversion, language, architecture
	 * from SOFTWARE_CONFIG group by name, version, subversion, language,
	 * architecture order by name, version, subversion, language, architecture ;
	 */

	// ===================================================

	/*
	 * in superclass
	 * protected Map<String, Map<String, Object>> hostConfigs;
	 * protected java.sql.Time CONFIG_STATE_last_entry = null;
	 * 
	 * @Override
	 * public void hostConfigsRequestRefresh()
	 * {
	 * logging.info(this, "hostConfigsRequestRefresh");
	 * hostConfigs= null;
	 * }
	 * 
	 * @Override
	 * public Map<String, Map<String, Object>> getConfigs()
	 * {
	 * retrieveHostConfigs();
	 * return hostConfigs;
	 * }
	 */

	@Override
	protected void retrieveHostConfigs() {
		
		
		
		// + classCounter + ": " + (hostConfigs == null) );

		if (hostConfigs != null)
			return;

		logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " config state");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();
		logging.info(this, "  retrieveHostConfigs ( CONFIG_STATE )  start a request");

		
		
		
		// json parsing for integer value false thereforw we omit the ID column

		String columns = ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.OBJECT_ID + ", "
				+ ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.CONFIG_ID + ", "
				+ ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.VALUES;

		String query = "select " + columns + " from " + ConfigStateEntry.DB_TABLE_NAME + " ";
		// + " where state = 1 ";

		logging.info(this, "retrieveHostConfigs, query " + query);

		List<List<java.lang.String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query })

				);

		logging.info(this, "retrieveHostConfigs, finished a request");

		hostConfigs = new HashMap<>();

		if (rows == null || rows.isEmpty()) {
			logging.warning(this, "no host config rows " + rows);
		} else {
			logging.info(this, "retrieveHostConfigs rows size " + rows.size());

			for (List<String> row : rows) {
				String hostId = row.get(0);
				

				Map<String, Object> configs1Host = hostConfigs.get(hostId);
				if (configs1Host == null) {
					configs1Host = new HashMap<>();
					hostConfigs.put(hostId, configs1Host);
				}

				/*
				 * Map<String, List<Object>> configValues1Host =
				 * hostConfigValues.get(hostId) ;
				 * if (configValues1Host == null)
				 * {
				 * configValues1Host = new HashMap<>();
				 * hostConfigValues.put(hostId, configValues1Host);
				 * }
				 */

				String configId = row.get(1);

				

				// get values as String
				String valueString = row.get(2);
				

				// parse String and produce list
				
				List values = new ArrayList<>();
				try {
					values = (new JSONArray(valueString)).toList();

				} catch (Exception ex) {
					logging.warning(this, "retrieveHostConfigs, error when json parsing database string \n"
							+ valueString + " for configId " + configId);
				}

				// put into host configs
				configs1Host.put(configId, values);

				// configValues1Host.put( configId, (List) configs1Host.get( configId
				

			}
		}

		timeCheck.stop();
		logging.info(this, "retrieveHostConfigs retrieved ");
		// hostConfigs.keySet()
		persist.notifyDataRefreshedObservers("configState");
		
	}

	// =================================================== client2HwRows

	private String maxTime(String time0, String time1) {
		if (time0 == null && time1 == null)
			return null;

		if (time0 == null || time0.equals(""))
			return time1;

		if (time1 == null || time1.equals(""))
			return time0;

		if (time0.compareTo(time1) < 0)
			return time1;

		return time0;
	}

	private Map<String, Map<String, Object>> client2HwRowsForHwClass(String hwClass) {
		logging.info(this, "client2HwRowsForHwClass " + hwClass);

		if (client2HwRows == null)
			return null;

		// z.B. hwClass is DISK_PARTITION

		Vector<String> specificColumns = new Vector<>(); // columns specific for the class
		specificColumns.add(Host.idColumn);

		StringBuffer buf = new StringBuffer("select HOST.hostId, ");
		StringBuffer cols = new StringBuffer("");

		String deviceTable = persist.hwInfo_DEVICE + hwClass;
		String configTable = persist.hwInfo_CONFIG + hwClass;

		String lastseenCol = configTable + "." + persist.lastseenColName;
		specificColumns.add(lastseenCol);
		buf.append(lastseenCol);
		buf.append(", ");

		boolean foundAnEntry = false;

		// build and collect database columnnames
		for (String hwInfoCol : persist.getClient2HwRowsColumnNames()) {
			if (hwInfoCol.startsWith("HOST.") || hwInfoCol.equals(persist.lastseenVisibleColName))
				continue; // these already are in the collection

			logging.info(this, "hwInfoCol " + hwInfoCol + " look for " + persist.hwInfo_DEVICE + " as well as "
					+ persist.hwInfo_CONFIG);
			String part0 = hwInfoCol.substring(0, persist.hwInfo_DEVICE.length());
			
			// + ", hwInfoCol.substring( part0.length()) " + hwInfoCol.substring(
			

			boolean colFound = false;
			// check if colname is from a CONFIG or a DEVICE table
			if (hwInfoCol.substring(part0.length()).startsWith(hwClass)) {
				colFound = true;
				// we found a DEVICE column name
			} else {

				part0 = hwInfoCol.substring(0, persist.hwInfo_CONFIG.length());

				if (!hwInfoCol.substring(part0.length()).startsWith(hwClass)) {
					
					
					
				} else {
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
			logging.info(this, "no columns found for hwClass " + hwClass);
			return null;
		}

		String colsS = cols.toString();
		buf.append(colsS.substring(0, colsS.length() - 1));

		buf.append(" \nfrom HOST ");

		buf.append(", ");
		buf.append(deviceTable);
		buf.append(", ");
		buf.append(configTable);

		buf.append("\n where ");
		
		// buf.append(" 'vbrupertwin7-64.uib.local' ");

		// buf.append("\n AND ");
		buf.append(Host.idColumn);
		buf.append(" = ");
		buf.append(configTable);
		buf.append(persist.hostIdField);

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(persist.hardwareIdField);
		buf.append(" = ");
		buf.append(deviceTable);
		buf.append(persist.hardwareIdField);

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(stateIs1);

		String query = buf.toString();

		logging.info(this, "retrieveClient2HwRows, query " + query);

		List<List<java.lang.String>> rows = persist.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query })

				);
		logging.info(this, "retrieveClient2HwRows, finished a request");
		logging.info(this, "retrieveClient2HwRows, got rows for class " + hwClass);
		logging.info(this, "retrieveClient2HwRows, got rows,  size  " + rows.size());

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
				if (valInRow == null || valInRow.equals("null"))
					valInRow = "";

				if (value == null) {
					value = valInRow;
				} else {
					value = value + CELL_SEPARATOR + valInRow;
				}

				if (specificColumns.get(i).equals(lastseenCol)) {
					String timeS = maxTime((String) value, row.get(i));
					rowMap.put(persist.lastseenVisibleColName, timeS);
				} else
					rowMap.put(specificColumns.get(i), value);

			}

		}

		logging.info(this, "retrieveClient2HwRows, got clientInfo, with size " + clientInfo.size());
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
		 * and HARDWARE_CONFIG_DISK_PARTITION.state=1 ;
		 * 
		 */

	}

	@Override
	protected void retrieveClient2HwRows(String[] hosts) {
		logging.info(this, "retrieveClient2HwRows( hosts )  for hosts " + hosts.length);

		if (client2HwRows != null) {
			logging.info(this, "retrieveClient2HwRows client2HwRows.size() " + client2HwRows.size());
			return;
		}

		client2HwRows = new HashMap<>();

		// set default rows
		for (String host : persist.getHostInfoCollections().getOpsiHostNames()) {
			Map<String, Object> nearlyEmptyHwRow = new HashMap<>();
			nearlyEmptyHwRow.put(Host.idColumn, host);

			String hostDescription = "";
			String macAddress = "";
			if (persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host) != null) {
				hostDescription = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host).getDescription();
				macAddress = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(host).getMacAddress();
			}
			nearlyEmptyHwRow.put(Host.descriptionColumn, hostDescription);
			nearlyEmptyHwRow.put(Host.hwAddressColumn, macAddress);

			client2HwRows.put(host, nearlyEmptyHwRow);
		}

		persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " hardware");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveClient2HwRows all ");
		timeCheck.start();

		// Map<String, Map<String, Object>> client2AllInfos = new HashMap<String,
		

		for (String hwClass : persist.getHwInfoClassNames()) {
			logging.info(this, "retrieveClient2HwRows hwClass " + hwClass);

			Map<String, Map<String, Object>> client2ClassInfos = client2HwRowsForHwClass(hwClass);

			
			

			if (client2ClassInfos != null) {

				for (String client : client2ClassInfos.keySet()) {
					Map<String, Object> allInfosForAClient = client2HwRows.get(client);
					// find max lastseen time as last scan time
					{
						String lastseen1 = (String) allInfosForAClient.get(persist.lastseenVisibleColName);
						String lastseen2 = (String) client2ClassInfos.get(client).get(persist.lastseenVisibleColName);
						if (lastseen1 != null && lastseen2 != null)
							client2ClassInfos.get(client).put(persist.lastseenVisibleColName,
									maxTime(lastseen1, lastseen2));
					}

					allInfosForAClient.putAll(client2ClassInfos.get(client));
				}
			}
		}

		logging.info(this, "retrieveClient2HwRows result size " + client2HwRows.size());

		timeCheck.stop();
		logging.info(this, "retrieveClient2HwRows finished  ");
		// client2HwRows.keySet()
		persist.notifyDataRefreshedObservers("client2HwRows");

	}

	// ===================================================
	/*
	 * String query = "select * from user";
	 * 
	 * logging.info(this, "test, query " + query);
	 * 
	 * boolean result
	 * = persist.exec.doCall(
	 * new OpsiMethodCall(
	 * "getRawData",
	 * new Object[]{query}
	 * )
	 * );
	 * 
	 * test user table
	 * Opsi service error: [ProgrammingError] (1146,
	 * "Table 'opsi.user' doesn't exist")
	 */
}
