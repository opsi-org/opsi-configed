package de.uib.configed.clientselection.serializers;

/**
 * Thrown if the data of a saved search is too old or new.
 */
public class WrongVersionException extends Exception
{
    WrongVersionException( int old, int current )
    {
        super("Error: The data is of version "+old+", but the current version is "+current);
    }
}
