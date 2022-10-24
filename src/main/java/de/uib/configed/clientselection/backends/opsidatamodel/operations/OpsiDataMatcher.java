package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
//import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public abstract class OpsiDataMatcher
{
	protected String map;
	protected String key;
	protected String data;

	public OpsiDataMatcher(  String map, String key, String data, SelectElement element )
	{
		logging.debug(this, "created:  maptype, key, data: " + map + ", " + key + ", " + data );
		
		this.map = map;
		this.key = key;
		this.data = data;
	}
	
	public boolean doesMatch( Client client )
	{
		OpsiDataClient oClient = (OpsiDataClient) client;
		logging.debug(this, "doesMatch client " + oClient);
		//logging.debug(this, "doesMatch interesting map, key  "   + map + ", " + key);
		Map realMap = oClient.getMap( map );
		//logging.debug( this, "doesMatch " + realMap.toString() );
		if( !realMap.containsKey(key) || realMap.get(key) == null )
		{
			//logging.debug(this, "key '" + key + "' not found!");
			return false;
		}

		String realData = realMap.get(key).toString();
		//logging.debug( this, "doesMatch realData " + realData);
		return checkData( realData );
	}

	abstract protected boolean checkData(final String realdata );
}
	


