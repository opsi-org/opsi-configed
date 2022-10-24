package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;

public class SoftwareNameElement extends GenericTextElement
{
    public SoftwareNameElement()
    {
        super(new String[] {de.uib.opsidatamodel.OpsiProduct.NAME, "Name"}, new String[] {configed.getResourceValue("ClientSelectionDialog.softwareName"),
                configed.getResourceValue("InstallationStateTableModel.productId")});
    }
    
     
    public SoftwareNameElement( Set<String> proposedData )
    {
        super(proposedData, 
        			new String[] {de.uib.opsidatamodel.OpsiProduct.NAME, "Name"}, new String[] {configed.getResourceValue("ClientSelectionDialog.softwareName"),
                configed.getResourceValue("InstallationStateTableModel.productId")});
        
        logging.debug(this, "proposed " + proposedData);
    }
    
}