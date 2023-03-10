/**
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2014
 *  organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.opsidatamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsicommand.DbConnect;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;

public class DataStubDirectSQL extends DataStubRawData
// only for testing purposes
// called by OpsiDirectSQLPersistenceController which may have more methods with
// direct sql access
{
	public DataStubDirectSQL(OpsiserviceNOMPersistenceController controller) {
		super(controller);
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

		if (client2software == null || !newClients.isEmpty()) {

			String clientSelection = null;

			if (newClients.size() >= 50) {
				clientSelection = "";
			} else {
				clientSelection = " AND ( " + giveWhereOR("clientId", newClients) + ") ";
			}

			if (client2software == null)
				client2software = new HashMap<>();

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " softwareConfig ");

			Logging.info(this, "retrieveSoftwareAuditOnClients/ SOFTWARE_CONFIG, start a request");

			String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString();
			columns = columns.substring(1);
			columns = columns.substring(0, columns.length() - 1);

			String query = "select " + columns + " from " + SWAuditClientEntry.DB_TABLE_NAME + " \n"
					+ " where  state = 1 " + clientSelection;

			Logging.info(this, "retrieveSoftwareAuditOnClients, query " + query);

			Connection sqlConn = DbConnect.getConnection();

			try (Statement stat = sqlConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY)) {
				TimeCheck timeCheck = new TimeCheck(this, "execute Query   " + query);
				timeCheck.start();

				ResultSet rs = stat.executeQuery(query);

				LinkedList<String> row;

				while (rs.next()) {

					String clientId = rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditClientEntry.CLIENT_ID));

					List<SWAuditClientEntry> entries = client2software.get(clientId);
					if (entries == null) {
						entries = new LinkedList<>();
						client2software.put(clientId, entries);
					}

					row = new LinkedList<>();

					for (int i = 0; i < SWAuditClientEntry.DB_COLUMN_NAMES.size(); i++) {
						row.add(rs.getString(i + 1));
					}

					SWAuditClientEntry clientEntry = new SWAuditClientEntry(SWAuditClientEntry.DB_COLUMN_NAMES, row,
							persist);

					if (clientEntry.getSWid() == -1) {
						Logging.info("Missing auditSoftware entry for swIdent " + SWAuditClientEntry.DB_COLUMN_NAMES
								+ "for values"
								+ SWAuditClientEntry.produceSWident(SWAuditClientEntry.DB_COLUMN_NAMES, row));

					} else {
						entries.add(clientEntry);
					}
				}
				timeCheck.stop("result set  ready ");

				newClients.removeAll(client2software.keySet());
				// the remaining clients are without software entry

				for (String clientId : newClients) {
					client2software.put(clientId, new LinkedList<>());
				}
			}

			catch (SQLException e) {
				Logging.info(this, "retrieveSoftwareAuditOnClients sql Error  in:\n" + query);
				Logging.error("retrieveSoftwareAuditOnClients sql Error " + e.toString());
			}

			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Globals.usedMemory());

			persist.notifyDataRefreshedObservers("softwareConfig");
		}
	}

}
