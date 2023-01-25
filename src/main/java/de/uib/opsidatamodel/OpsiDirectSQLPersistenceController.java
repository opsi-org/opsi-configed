/**
 *   PersistenceController
 *   implementation for the New Object Model (opsi 4.0) overwritten by direct sql
 *   description: instances of PersistenceController serve 
 *   as proxy objects which give access to remote objects (and buffer the data)
 * 
 *  A  PersistenceController retrieves its data from a server that is compatible with the  
 *  opsi data server.
 *  It has a Executioner component that transmits requests to the opsi server and receives the responses.
 *  There are several classes which implement the Executioner methods in different ways 
 *  dependent on the used means and protocols
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2013-2015
 *  organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.opsidatamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsicommand.DbConnect;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;

public class OpsiDirectSQLPersistenceController extends OpsiserviceRawDataPersistenceController {

	OpsiDirectSQLPersistenceController(String server, String user, String password) {
		super(server, user, password);
		DbConnect.getConnection(server, user, password);
	}

	@Override
	protected void initMembers() {
		dataStub = new DataStubDirectSQL(this);
	}

	@Override
	public List<Map<java.lang.String, java.lang.Object>> hostRead() {

		Logging.info(this, "HOST_read ");
		String query = "select *  from HOST";
		TimeCheck timer = new TimeCheck(this, "HOST_read").start();

		Logging.notice(this, "HOST_read");
		List<Map<java.lang.String, java.lang.Object>> opsiHosts = exec
				.getListOfMaps(new OpsiMethodCall("getData", new Object[] { query }));
		timer.stop();

		return opsiHosts;
	}

	private String giveWhereOR(String colName, String[] values) {
		if (values == null || values.length == 0)
			return "true";

		StringBuilder result = new StringBuilder(colName + " = '" + values[0] + "'");

		int lineCount = 0;

		for (int i = 1; i < values.length; i++) {
			result.append(" OR ");
			result.append(colName);
			result.append(" = '");
			result.append(values[i]);
			result.append("'      ");
			lineCount++;
			if (lineCount == 100) {
				result.append("\n");
				lineCount = 0;
			}
		}

		return result.toString();
	}

	@Override
	protected Map<String, List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds) {

		String columns = Arrays.toString((ProductState.DB_COLUMN_NAMES).toArray(new String[] {}));
		columns = columns.substring(1);
		columns = columns.substring(0, columns.length() - 1);

		columns = "clientId, " + columns;

		String query = "select " + columns + " from PRODUCT_ON_CLIENT " + " where  productType = 'LocalbootProduct'"
				+ " AND \n" + " ( " + giveWhereOR("clientId", clientIds) + ") ";

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		Connection sqlConn = DbConnect.getConnection();

		try (Statement stat = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

			ResultSet rs = stat.executeQuery(query);

			while (rs.next()) {
				String client = rs.getString("clientId");

				List<Map<String, String>> states1Client = result.get(client);
				if (states1Client == null) {
					states1Client = new ArrayList<>();
					result.put(client, states1Client);
				}

				Map<String, String> rowMap = new HashMap<>();

				for (String col : ProductState.DB_COLUMN_NAMES) {
					if (rs.getString(col) == null)
						rowMap.put(col, "");

					else
						rowMap.put(col, rs.getString(col));
				}

				states1Client.add(new ProductState(rowMap, true));

			}
		} catch (SQLException e) {
			Logging.info(this, "getLocalBootProductStatesNOM sql Error  in:\n" + query);
			Logging.error("getLocalBootProductStatesNOM sql Error " + e.toString());
		}

		return result;
	}

	private String sqlQuote(String r) {
		String s = r.replace("'", "''");
		return s.replace("\\", "\\\\");
	}

	@Override
	public void cleanUpAuditSoftware() {
		java.sql.Connection sqlConn = DbConnect.getConnection();

		TreeMap<String, Map<String, String>> rowsSoftwareOnClients = new TreeMap<>();

		String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString();
		columns = columns.substring(1);
		columns = columns.substring(0, columns.length() - 1);

		String query = "select " + columns + " from " + SWAuditClientEntry.DB_TABLE_NAME + " \n" + " where  state = 1 ";
		Logging.info(this, "cleanUpAuditSoftware query " + query);

		try (Statement stat = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

			ResultSet rs = stat.executeQuery(query);

			int counter = 0;
			boolean logNext = true;

			while (rs.next()) {
				counter++;

				String ident = Globals
						.pseudokey(new String[] { rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.NAME)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.VERSION)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.SUB_VERSION)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.LANGUAGE)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE)) });

				if (rowsSoftwareOnClients.get(ident) == null) {
					Map<String, String> rowmap = new HashMap<>();

					rowmap.put("name", rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.NAME)));

					rowmap.put("version", rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.VERSION)));
					rowmap.put("subVersion", rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.SUB_VERSION)));
					rowmap.put("language", rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.LANGUAGE)));
					rowmap.put("architecture",
							rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE)));

					rowsSoftwareOnClients.put(ident, rowmap);

					if (logNext) {
						Logging.info(this,
								"retrieveSoftwareAuditOnClients logging first ident: rowmap " + ident + " : " + rowmap);
						logNext = false;
					}

				}

			}
			Logging.info(this, "retrieveSoftwareAuditOnClients, entries read " + counter);
			Logging.info(this, "retrieveSoftwareAuditOnClients, idents  " + rowsSoftwareOnClients.size());

		}

		catch (SQLException e) {
			Logging.error("cleanUpAuditSoftware sql Error " + e.toString());
		}

		java.util.Set<String> swIdentsOnClients = rowsSoftwareOnClients.keySet();

		TreeMap<String, Map<String, String>> rowsSOFTWARE = new TreeMap<>();

		query = "select  name, version, subVersion, language, architecture from SOFTWARE";

		Logging.info(this, "cleanUpAuditSoftware, select from SOFTWARE " + " using query: " + query);

		try (Statement stat = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

			ResultSet rs = stat.executeQuery(query);

			int counter = 0;

			while (rs.next()) {
				counter++;

				String ident = Globals.pseudokey(new String[] { rs.getString("name"), rs.getString("version"),
						rs.getString("subversion"), rs.getString("language"), rs.getString("architecture") });

				if (rowsSOFTWARE.get(ident) == null) {
					Map<String, String> rowmap = new HashMap<>();

					rowmap.put("name", rs.getString("name"));
					rowmap.put("version", rs.getString("version"));
					rowmap.put("subVersion", rs.getString("subVersion"));
					rowmap.put("language", rs.getString("language"));
					rowmap.put("architecture", rs.getString("architecture"));

					rowsSOFTWARE.put(ident, rowmap);
				}

			}
			Logging.info(this, "retrieveSoftware, entries read " + counter);
			Logging.info(this, "retrieveSoftware, idents size " + rowsSOFTWARE.size());
		}

		catch (SQLException e) {
			Logging.error("cleanUpAuditSoftware sql Error " + e.toString());
		}

		Set<String> swIdentsOnlyInSoftware = rowsSOFTWARE.keySet();

		swIdentsOnlyInSoftware.removeAll(swIdentsOnClients);

		Logging.info(this, "cleanUpAuditSoftware  idents in SOFTWARE not on CLIENTS " + swIdentsOnlyInSoftware.size());

		int sizeOfAllRemoves = swIdentsOnlyInSoftware.size();

		List<String> removes = new ArrayList<>(swIdentsOnlyInSoftware);

		final int portionSize = 10;

		int portionStart = 0;
		int portionEnd;
		if (portionStart + portionSize <= sizeOfAllRemoves)
			portionEnd = portionStart + portionSize;
		else
			portionEnd = sizeOfAllRemoves;

		boolean goOn = (sizeOfAllRemoves > 0);

		while (goOn) {
			Logging.info(this, "cleanUpAuditSoftware remove entries from " + portionStart);

			StringBuilder condition = new StringBuilder();

			boolean logNext = true;

			for (int i = portionStart; i < portionEnd; i++) {

				String ident = removes.get(i);

				Map<String, String> rowmap = rowsSOFTWARE.get(ident);
				if (logNext) {
					Logging.info(this, "cleanUpAuditSoftware  ident in SOFTWARE not on CLIENTS, ident " + ident
							+ " rowmap \n" + rowmap);
					logNext = false;
				}

				condition.append("(");
				condition.append("name = " + "'" + sqlQuote(rowmap.get("name")) + "' and ");
				condition.append("version = " + "'" + sqlQuote(rowmap.get("version")) + "' and ");
				condition.append("subVersion = " + "'" + rowmap.get("subVersion") + "' and ");
				condition.append("language = " + "'" + rowmap.get("language") + "' and ");
				condition.append("architecture = " + "'" + rowmap.get("architecture") + "'");
				condition.append(") \n or \n ");

			}

			condition.append(" false ");

			Logging.info(this, "cleanUpAuditSoftware, delete SOFTWARE records");
			query = "delete  from SOFTWARE where " + condition.toString();
			Logging.debug(this, "cleanUpAuditSoftware, delete SOFTWARE records  by query: \n" + query);

			try (Statement stat = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY)) {
				int affectedRows = stat.executeUpdate(query);

				Logging.info(this, "cleanUpAuditSoftware, deleted " + affectedRows + " in Table SOFTWARE");
			} catch (SQLException e) {
				Logging.error("cleanUpAuditSoftware sql Error " + e.toString());
			}

			goOn = (portionEnd < sizeOfAllRemoves);

			portionStart = portionEnd;
			if (portionStart + portionSize <= sizeOfAllRemoves)
				portionEnd = portionStart + portionSize;
			else
				portionEnd = sizeOfAllRemoves;

			Logging.info(this, "cleanUpAuditSoftware removed entries up to (not including) " + portionStart);

		}

	}

}
