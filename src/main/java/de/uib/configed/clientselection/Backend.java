package de.uib.configed.clientselection;

import java.util.*;
import de.uib.utilities.logging.logging;

/**
 * Each backend represents a data source, which can be used find out which clients should be selected.
 * It also creates all operations, as the implementation may differ between the data sources.
 */
public abstract class Backend
{
    private static Backend currentBackend=null;
    
    /* These variables tell you which data you have to fetch. E.g. if hasSoftware is true, there is an software
     * operation and so you need to get the data about software.
     */
    protected boolean hasSoftware;
    protected boolean hasHardware;
    protected boolean hasSwAudit;
    protected boolean reloadRequested;
    
    /**
     * Goes through the list of clients and filters them with operation. The boolean arguments give hints which data
     * is needed.
     */
    public List<String> checkClients( ExecutableOperation operation, boolean hasSoftware, boolean hasHardware, boolean hasSwAudit )
    {
        logging.debug( this, "Starting the filtering.. , operation " + operation );
        this.hasSoftware = hasSoftware;
        this.hasHardware = hasHardware;
        this.hasSwAudit = hasSwAudit;
        List<Client> clients = getClients();
        logging.debug( this, "Number of clients to filter: " + clients.size() );
        
        List<String> matchingClients = new LinkedList<String>();
        for( Client client: clients )
        {
            //logging.debug( this, "Checking client " +client.getId()+"..." );
            if( operation.doesMatch( client ) )
            {
                //logging.debug( this, "Client did match!" );
                matchingClients.add( client.getId() );
            }
        }
        return matchingClients;
    }
    
    /**
     * This function translates the operations tree with the root operation into an executable operation tree
     * by replacing the non-executable operations with their backend-specific executable operations.
     */
    public ExecutableOperation createExecutableOperation( SelectOperation operation )
    {
        logging.debug(this, "createFromOperationData " + operation.getClassName());
    	
        if( operation instanceof SelectGroupOperation )
        {
            SelectGroupOperation groupOperation = (SelectGroupOperation) operation;
            List<SelectOperation> children = new LinkedList<SelectOperation>();
            for( SelectOperation child: groupOperation.getChildOperations() )
                children.add( (SelectOperation) createExecutableOperation( child ) );
            return (ExecutableOperation) createGroupOperation( groupOperation, children );
        }
        else
        {
            return (ExecutableOperation) createOperation( operation );
        }
    }
    
    /**
     *  sets the property
     */
    public void setReloadRequested()
    {
    	reloadRequested = true;
    }
    
    
    /**
     * Create a backend specific executable operation based on this operation.
     */
    abstract protected SelectOperation createOperation( SelectOperation operation );
    
    /**
     * Creates a backend specific executable operation based on this group operation and the list of backend specific children.
     */
    abstract protected SelectGroupOperation createGroupOperation( SelectGroupOperation operation, List<SelectOperation> operations );
    
    /**
     * Get a list of all clients. These will be filtered later.
     */
    abstract protected List<Client> getClients(); 
    
    /**
     * Get a list of all groups of this opsi installation.
     */
    abstract public List<String> getGroups();
    
    /**
     * Get a list of product IDs of the opsi products.
     */
    abstract public TreeSet<String> getProductIDs();
    
    /**
     * Get a map, with the hardware as key and a list of properties as value. The key is in english.
     */
    abstract public Map<String, List<SelectElement> > getHardwareList();
    
    /**
     * Get a map, with the hardware as key and a list of properties as value. The key is localized.
     */
    abstract public Map<String, List<SelectElement> > getLocalizedHardwareList();
}