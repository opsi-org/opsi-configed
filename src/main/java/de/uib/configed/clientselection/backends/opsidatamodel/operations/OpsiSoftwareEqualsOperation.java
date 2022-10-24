package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;
import de.uib.configed.clientselection.backends.opsidatamodel.*;
import de.uib.utilities.logging.logging;
import java.util.*;

public class OpsiSoftwareEqualsOperation extends OpsiDataStringEqualsOperation //(implements ExecutableOperation)
{
    public OpsiSoftwareEqualsOperation( String key, String data, SelectElement element )
    {
        super(OpsiDataClient.SOFTWARE_MAP,key, data, element);
        logging.debug(this, "created  for key, data, element " + key + ", " + data + ", " + element);
    }
  
}
  
