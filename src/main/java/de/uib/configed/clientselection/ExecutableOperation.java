package de.uib.configed.clientselection;

/**
 * Classes implementing this interface can check whether specific
 * clients match a certain condition. These are the only operation
 * classes that can be used in a search.
 */
public interface ExecutableOperation
{
    /**
     * Checks whether the client does match the given criteria. You may need to set the data
     * first.
     */
    public boolean doesMatch( Client client );
}
