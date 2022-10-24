package de.uib.opsidatamodel.datachanges;

import de.uib.opsidatamodel.*;
import java.util.Map;
import de.uib.utilities.logging.*;

public class HostUpdate  extends MapUpdate
{
	public HostUpdate(PersistenceController persis, Map newdata)
	{
		super(persis, newdata);
	}
	
    public void doCall()
    {
    		logging.debug(this, "doCall, newdata " + newdata);
    		persis.setHostValues(newdata);
    }
}

