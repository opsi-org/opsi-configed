package de.uib.configed.clientselection;

/**
 * A client represents one managed computer. Backends may use implementations of this interface to
 * save and share data between the operations.
 */
public interface Client
{
    /**
     * Get a representing string for a client.
     */
    public String getId();
}