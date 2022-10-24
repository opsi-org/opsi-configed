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

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.configed.type.*;
import de.uib.utilities.table.*;

import java.sql.*;
import de.uib.opsidatamodel.productstate.*;
import de.uib.opsicommand.*;


public class OpsiDirectSQLPersistenceController extends OpsiserviceRawDataPersistenceController
{
	
	java.sql.Time PRODUCT_ON_CLIENT_last_read = null;
	
	OpsiDirectSQLPersistenceController (String server, String user, String password)
	{
		super(server, user, password);
		DbConnect.getConnection(server, user, password);
	}
	
	@Override
	protected void initMembers()
	{
		dataStub = new DataStubDirectSQL(this);
	}
	
	
	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
			
		logging.info(this, "HOST_read ");
		String query = "select *  from HOST";
		TimeCheck timer= new TimeCheck(this, "HOST_read").start();
		
		logging.check(this, "HOST_read");
		java.util.List<Map<java.lang.String,java.lang.Object>> opsiHosts = exec.getListOfMaps(
				new OpsiMethodCall(
					"getData",
					new Object[]{query}
				)
			)
			;
		timer.stop();
		
		return opsiHosts;
	}
	
	private String giveWhereOR(String colName, String[] values)
	{
		if (values == null || values.length == 0)
			return "true";
		
		StringBuffer result = new StringBuffer(colName + " = '" + values[0] + "'");
		
		int lineCount = 0;
		
		for (int i = 1; i < values.length; i++)
		{
			result.append(" OR ");
			result.append(colName);
			result.append(" = '");
			result.append(values[i]);
			result.append("'      ");
			lineCount++;
			if (lineCount == 100)
			{
				result.append("\n");
				lineCount = 0;
			}
		}
		
		return result.toString();
	}		
		
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		
		java.util.List clients = java.util.Arrays.asList(clientIds);
		
		String columns = Arrays.toString(	(ProductState.DB_COLUMN_NAMES).toArray( new String[]{} )  ) ;
		columns = columns.substring(1);
		columns = columns.substring(0, columns.length()-1);
		
		columns = "clientId, " + columns;
		
		
		String query = "select " + columns + " from PRODUCT_ON_CLIENT " 
			+ " where  productType = 'LocalbootProduct'"
			+ " AND \n" 
			+ " ( "  
			+ giveWhereOR("clientId", clientIds)
			+ ") ";
			
		//System.out.println(query);
		//System.exit(0);
		
		
		
		//TimeCheck timer= new TimeCheck(this, "getLocalBootProductStatesNOM").start();
		
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>(); 
			
		
		java.sql.Connection sqlConn = DbConnect.getConnection();
		
		try
		{
			
			java.sql.Statement stat = sqlConn.createStatement(
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY
								);
		
			
			
			ResultSet rs = stat.executeQuery(query);
			
			
			while (rs.next())
			{
				String client = rs.getString("clientId");
				
				java.util.List<Map<String, String>>states1Client = result.get(client);
				if (states1Client == null)
				{
					states1Client = new ArrayList<Map<String, String>>();
					result.put(client, states1Client);
				}
				
				Map<String, String> rowMap = new HashMap<String, String>();
				
				
				for (String col : ProductState.DB_COLUMN_NAMES)
				{
					if (rs.getString(col) == null)
						rowMap.put(col, "");
					
					else
						rowMap.put(col, rs.getString(col));
				}
				
				states1Client.add(new ProductState(rowMap,true));
				
			}
		}
		catch( SQLException e )
		{
			logging.info(this,"getLocalBootProductStatesNOM sql Error  in:\n" +query);
			logging.error("getLocalBootProductStatesNOM sql Error " +e.toString());
		}
		
		//timer.stop();
		
		return result;
	}
	
	private String produceNotNull(Map<String, String> m, String k)
	{
		if (m.get(k) == null)
		{
			logging.warning(this, " null value for key " + k);
			
			return "";
		}
		
		return m.get(k);
	}
	
	private String sqlQuote(String r)
	{
		String s = r.replace("'", "''");
		String t = s.replace("\\", "\\\\");
		return t;
	}
	
	@Override
	public void cleanUpAuditSoftware()
	{
		java.sql.Connection sqlConn = DbConnect.getConnection();
		
	
		TreeMap<String, Map<String, String>>  rowsSOFTWARE_ON_CLIENTS = new TreeMap<String, Map<String, String>>();
		
		
		String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString(); 
				columns = columns.substring(1);
				columns = columns.substring(0, columns.length()-1);
		
		String query = "select " + columns + " from " +  SWAuditClientEntry.DB_TABLE_NAME + " \n" 
					+ " where  state = 1 ";
		logging.info(this, "cleanUpAuditSoftware query " + query);
		
		try
		{
			
			java.sql.Statement stat = sqlConn.createStatement(
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY
								);
		
			
			
			ResultSet rs = stat.executeQuery(query);
			
			int counter = 0;
			boolean logNext = true;
			
			while (rs.next())
			{
				counter++;
				
				String ident = de.uib.utilities.Globals.pseudokey(new String[]{
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.NAME)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.VERSION)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.SUBVERSION)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.LANGUAGE)),
								rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE))
								}
							);
				
				
				
								
				if (rowsSOFTWARE_ON_CLIENTS.get(ident) == null)
				{
					Map<String, String> rowmap = new HashMap<String, String>();
					
					rowmap.put("name",
						rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.NAME)));
						
					rowmap.put("version", 
						rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.VERSION)) );
					rowmap.put("subVersion",
						rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.SUBVERSION)) );
					rowmap.put("language", 
						rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.LANGUAGE)) );
					rowmap.put("architecture",
						rs.getString(SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE)) );
					
					rowsSOFTWARE_ON_CLIENTS.put(ident, rowmap);
					
					
				
					if (logNext)
					{
						logging.info(this, "retrieveSoftwareAuditOnClients logging first ident: rowmap " + ident + " : " + rowmap);
						logNext = false;
					}
					
				}
				
				
			}
			logging.info(this, "retrieveSoftwareAuditOnClients, entries read " + counter);
			logging.info(this, "retrieveSoftwareAuditOnClients, idents  " + rowsSOFTWARE_ON_CLIENTS.size());
			
			stat.close();

		}
		
		catch( SQLException e )
		{
			logging.error("cleanUpAuditSoftware sql Error " +e.toString());
		}
		
		
		java.util.Set<String> swIdentsOnClients = rowsSOFTWARE_ON_CLIENTS.keySet();
		
		TreeMap<String, Map<String, String>> rowsSOFTWARE = new TreeMap<String, Map<String, String>>();
		
		query = "select  name, version, subVersion, language, architecture from SOFTWARE"; 
					
		logging.info(this, "cleanUpAuditSoftware, select from SOFTWARE " 
			+ " using query: " + query);
		
		try
		{
			java.sql.Statement stat = sqlConn.createStatement(
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY
								);
		
			
			
			ResultSet rs = stat.executeQuery(query);
			
			int counter = 0;
			
			while (rs.next())
			{
				counter++;
				
				String ident = de.uib.utilities.Globals.pseudokey(new String[]{
								rs.getString("name"),
								rs.getString("version"),
								rs.getString("subversion"),
								rs.getString("language"),
								rs.getString("architecture")
				}
								);
			
				
				if (rowsSOFTWARE.get(ident) == null)
				{
					Map<String, String> rowmap = new HashMap<String, String>();
					
					rowmap.put("name",  
						rs.getString("name") );
					rowmap.put("version", 
						rs.getString("version") );
					rowmap.put("subVersion", 
						rs.getString("subVersion") );
					rowmap.put("language", 
						rs.getString("language") );
					rowmap.put("architecture", 
						rs.getString("architecture") );
					
					rowsSOFTWARE.put(ident, rowmap);
				}
				
				
			}
			logging.info(this, "retrieveSoftware, entries read " + counter);
			logging.info(this, "retrieveSoftware, idents size " + rowsSOFTWARE.size());
			
			
			stat.close();

		}
		
		catch( SQLException e )
		{
			logging.error("cleanUpAuditSoftware sql Error " +e.toString());
		}
		
		Set<String> swIdentsOnlyInSoftware = rowsSOFTWARE.keySet(); 
		
		swIdentsOnlyInSoftware.removeAll(swIdentsOnClients);
		
		logging.info(this, "cleanUpAuditSoftware  idents in SOFTWARE not on CLIENTS " + swIdentsOnlyInSoftware.size());
		//logging.info(this, "cleanUpAuditSoftware  idents in SOFTWARE not on CLIENTS " + rowsSOFTWARE_ON_CLIENTS.get("LHCommon.NET 9.60;9.60.344;;;x86"));
		
	
		
		int sizeOfAllRemoves = swIdentsOnlyInSoftware.size();
		
		ArrayList<String> removes = new ArrayList<String> (swIdentsOnlyInSoftware);
		
		final int portionSize = 10;
		
		int portionStart = 0;
		int portionEnd;
		if (portionStart + portionSize <= sizeOfAllRemoves)
			portionEnd =portionStart + portionSize;
		else
			portionEnd = sizeOfAllRemoves;
		
		boolean goOn = (sizeOfAllRemoves > 0);
		
		while ( goOn )
		{
			logging.info(this, "cleanUpAuditSoftware remove entries from " + portionStart);
			
			StringBuffer condition  = new StringBuffer();
				
			boolean logNext = true;
			
			for (int i = portionStart; i < portionEnd; i++)
			{
				
				String ident = removes.get(i);
				
				Map<String, String> rowmap = rowsSOFTWARE.get(ident);
				if (logNext)
				{
					logging.info(this, "cleanUpAuditSoftware  ident in SOFTWARE not on CLIENTS, ident " + ident + " rowmap \n" 
						+ rowmap);
					logNext = false;
				}
				
				
				condition.append("(");
				condition.append("name = " + "'" + sqlQuote(rowmap.get("name")) + "' and ");
				condition.append("version = " + "'" + sqlQuote(rowmap.get("version")) + "' and "); 
				condition.append("subVersion = " + "'" + rowmap.get("subVersion") + "' and "); 
				condition.append("language = " + "'" + rowmap.get("language") + "' and ");
				condition.append("architecture = " + "'" + rowmap.get("architecture") + "'" );
				condition.append(") \n or \n ");
					
					
					
			}
			
			condition.append (" false ");
			
		
			//logging.debug(this, "cleanUpAuditSoftware  idents in SOFTWARE not on CLIENTS  cleaning condition " + condition);
				
			
			logging.info(this, "cleanUpAuditSoftware, delete SOFTWARE records");
			query = "delete  from SOFTWARE where " + condition.toString();
			logging.debug(this, "cleanUpAuditSoftware, delete SOFTWARE records  by query: \n" + query);
			
			try
			{
				
				java.sql.Statement stat = sqlConn.createStatement(
									ResultSet.TYPE_SCROLL_INSENSITIVE,
									ResultSet.CONCUR_READ_ONLY
									);
			
				
				int affectedRows = stat.executeUpdate(query);
				
				logging.info(this, "cleanUpAuditSoftware, deleted " + affectedRows  + " in Table SOFTWARE");
				
				stat.close();
			
			}
			catch( SQLException e )
			{
				logging.error("cleanUpAuditSoftware sql Error " +e.toString());
			}
			
			
			goOn = (portionEnd < sizeOfAllRemoves);
			
			portionStart = portionEnd;
			if (portionStart + portionSize <= sizeOfAllRemoves)
				portionEnd =portionStart + portionSize;
			else
				portionEnd = sizeOfAllRemoves;
			
			logging.info(this, "cleanUpAuditSoftware removed entries up to (not including) " + portionStart);
			
		}
	
	
		
		
		
		
		
	
		/*
		
		logging.info(this, "retrieveSoftwareAuditOnClients, idents  " + rowsSOFTWARE.keySet());
		
		
		try
		{
		
			java.sql.Statement stat = sqlConn.createStatement(
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY
								);
			
			int affectedRows = 0;
			
			int count = 0;
			
			for (String ident : rowsSOFTWARE.keySet())
			{
				count++;
				Map<String, String> rowmap = rowsSOFTWARE.get(ident);
				
				query = "insert into SOFTWARE "  
						+ "("  
						+ "name" + ", "
						+ "version" + ", "
						+ "subVersion" + ", "
						+ "language" + ", "
						+ "architecture" 
						+ ")"
						+ "VALUES ("
						+ "'" + rowmap.get( SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.NAME) ) + "'" + ", "
						+ "'" + rowmap.get( SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.VERSION) ) + "'" + ", "
						+ "'" + rowmap.get( SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.SUBVERSION) ) + "'" + ", "
						+ "'" + rowmap.get( SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.LANGUAGE) ) + "'" + ", "
						+ "'" + rowmap.get( SWAuditClientEntry.DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE) )+ "'" 
						+ ")"
						;
						
				logging.info(this, "cleanUpAuditSoftware,  insert by query: \n" + query);
				
				
				int newly = stat.executeUpdate(query);
				
				logging.info(this, "cleanUpAuditSoftware,  inserted  " + newly);
				
				affectedRows = affectedRows + newly;
				
				if (count > 2) break;
				
			}
			
			stat.close();
			
			logging.info(this, "cleanUpAuditSoftware,  inserted " + rowsSOFTWARE.keySet() + " in Table SOFTWARE");
			
			
		}
		catch( SQLException e )
		{
			logging.error("cleanUpAuditSoftware sql Error " +e.toString());
		}
		*/
		
	}
	
						
	
}



