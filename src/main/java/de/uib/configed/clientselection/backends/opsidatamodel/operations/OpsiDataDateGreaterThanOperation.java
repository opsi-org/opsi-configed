package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiDataDateGreaterThanOperation extends DateGreaterThanOperation implements ExecutableOperation
{

	private OpsiDataDateMatcher matcher;

	public OpsiDataDateGreaterThanOperation( String map, String key, String data, SelectElement element )
	{
		super(element);


		matcher = new OpsiDataDateMatcher(map, key, data, element)
		          {
			          @Override
			          protected boolean compare(java.sql.Date date, java.sql.Date realdate)
			          {
				          return realdate.after(date);
			          }
		          }
		          ;
	}


	public boolean doesMatch( Client client )
	{
		return matcher.doesMatch(client);
	}
}
