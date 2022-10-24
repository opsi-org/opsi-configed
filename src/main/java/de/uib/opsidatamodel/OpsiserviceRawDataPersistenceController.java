/**
 *   PersistenceController
 *   implementation for the New Object Model (opsi 4.0) overwritten by directer sql access method
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

import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;


public class OpsiserviceRawDataPersistenceController extends OpsiserviceNOMPersistenceController
{
	
	java.sql.Time PRODUCT_ON_CLIENT_last_read = null;
	
	OpsiserviceRawDataPersistenceController (String server, String user, String password)
	{
		super(server, user, password);
	}
	
	@Override
	protected void initMembers()
	{
		if (dataStub == null)
			dataStub = new DataStubRawData(this);
	}
	
	
	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
			
		logging.debug(this, "HOST_read ");
		String query = "select *  from HOST";
		
		//test for depot_restriction:
			// SELECT CONFIG_VALUE.configId, CONFIG_STATE.objectId, CONFIG_STATE.values from CONFIG_VALUE, CONFIG_STATE where CONFIG_STATE.configId = CONFIG_VALUE.configId and CONFIG_STATE.configId =  'clientconfig.depot.id';
		
		
		TimeCheck timer= new TimeCheck(this, "HOST_read").start();
		
		logging.check(this, "HOST_read, query " + query);
		java.util.List<Map<java.lang.String,java.lang.Object>> opsiHosts = exec.getListOfMaps(
				new OpsiMethodCall(
					"getData",
					new Object[]{query}
				)
			)
			;
		timer.stop();
		
		for (Map<java.lang.String,java.lang.Object> entry : opsiHosts)
		{
			//logging.info(this, "HOST_read " + entry);
			Host.db2ServiceRowMap(entry);
			//logging.info(this, "HOST_read " + entry);
		}
		
		//logging.info(this, "OpsiserviceRawDataPersistenceController opsiHosts " + opsiHosts);
		
		//opsiHosts = super.HOST_read(); //test
		
		//logging.info(this, "OpsiserviceNOMPersistenceController opsiHosts " + opsiHosts);
		
		
		return opsiHosts;
	}
	
	
	
	
}



