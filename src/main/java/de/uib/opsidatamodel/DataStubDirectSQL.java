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

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.productstate.*;
import de.uib.utilities.table.*;
import java.sql.*;

public class DataStubDirectSQL extends DataStubRawData
//only for testing purposes
//called by OpsiDirectSQLPersistenceController which may have more methods with direct sql access
{
	public DataStubDirectSQL(OpsiserviceNOMPersistenceController controller)
	{
		super(controller);
	}
	
	@Override
	protected void retrieveSoftwareAuditOnClients(final java.util.List<String> clients)
	{
		logging.info(this,  "retrieveSoftwareAuditOnClients used memory on start " + de.uib.utilities.Globals.usedMemory());
		retrieveInstalledSoftwareInformation();
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  clients count ======  " + clients.size());
		
		java.util.List<String> newClients = new ArrayList<String>(clients);
		
		if (client2software!= null)
		{
			logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " 
			+ "   +++  " + client2software.keySet().size());
			
			newClients.removeAll(client2software.keySet());
		}
		
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  new clients count  ====== " + newClients.size());
		
		int stepSize = 100;
		
		if (client2software == null || newClients.size() > 0)	
		{
			
				String clientSelection = null;
				
				if (newClients.size() >= 50)
				{
					clientSelection = "";
				}
				else
				{
					clientSelection =
					 " AND ( "  
					+ giveWhereOR("clientId", newClients)
					+ ") ";
				}
				
			
				//logging.info(this, "retrieveSoftwareAuditOnClients for " + clientListForCall.size()  + " clients " + clientListForCall);
				
				//client2software = new HashMap<String, java.util.List<String>>();
				if (client2software == null) client2software = new HashMap<String, java.util.List<SWAuditClientEntry>>();
				
				persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " softwareConfig ");
					//, step " + step);
				
				logging.info(this, "retrieveSoftwareAuditOnClients/ SOFTWARE_CONFIG, start a request");
				
				String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString(); 
				columns = columns.substring(1);
				columns = columns.substring(0, columns.length()-1);
				
				String query = "select " + columns + " from " +  SWAuditClientEntry.DB_TABLE_NAME + " \n" 
					+ " where  state = 1 " 
					+ clientSelection
					;
						
				logging.info(this, "retrieveSoftwareAuditOnClients, query " + query);
				
				
						
				java.sql.Connection sqlConn = DbConnect.getConnection();
				
				try
				{
					TimeCheck timeCheck = new TimeCheck(this, "execute Query   " + query);
					timeCheck.start();
		
					java.sql.Statement stat = sqlConn.createStatement(
										ResultSet.TYPE_SCROLL_INSENSITIVE,
										ResultSet.CONCUR_READ_ONLY
										);
				
					
					
					ResultSet rs = stat.executeQuery(query);
					
					LinkedList<String> row;
					
					while (rs.next())
					{
						
						String clientId = rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditClientEntry.CLIENT_ID));
						
						java.util.List<SWAuditClientEntry> entries = client2software.get(clientId);
						if (entries == null)
						{
							entries = new LinkedList<SWAuditClientEntry>();
							client2software.put(clientId, entries);
						}
						
						row = new LinkedList<String>();
						
						for (int i = 0; i < SWAuditClientEntry.DB_COLUMN_NAMES.size(); i++)
						{
							row.add(rs.getString(i+1));
						}
						/*
						for (String col : SWAuditClientEntry.DB_COLUMN_NAMES)
						{
							row.add(rs.getString(col));
						}
						*/
						
						
						SWAuditClientEntry clientEntry = 
						new SWAuditClientEntry( SWAuditClientEntry.DB_COLUMN_NAMES, row, persist);
						
						if (clientEntry.getSWid() == -1)
						{
							logging.info("Missing auditSoftware entry for swIdent " + SWAuditClientEntry.DB_COLUMN_NAMES + "for values" +
								SWAuditClientEntry.produceSWident(SWAuditClientEntry.DB_COLUMN_NAMES, row)
								);
							//item.put(SWAuditEntry.WINDOWSsOFTWAREid, "MISSING");
						}
						else
						{
							entries.add(clientEntry);
						}
					}
					
					stat.close();
					
					timeCheck.stop("result set  ready ");
	
					
					newClients.removeAll(client2software.keySet());
					//the remaining clients are without software entry
					
					for (String  clientId : newClients)
					{
						client2software.put(clientId, new LinkedList<SWAuditClientEntry>() );
					}
						
					
				}
				
				catch( SQLException e )
				{
					logging.info(this,"retrieveSoftwareAuditOnClients sql Error  in:\n" +query);
					logging.error("retrieveSoftwareAuditOnClients sql Error " +e.toString());
				}
				
				
				
				//logging.info(this, "retrieveSoftwareAuditOnClients client2software " + client2software);
				
				logging.info(this,  "retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory()); 
				//System.gc();
				//logging.info(this,  "retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory()); 
				//step++;
			
			
			
			persist.notifyDataRefreshedObservers("softwareConfig");
		}
	}
	
}


