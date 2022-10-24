package de.uib.configed.clientselection.serializers;

import java.util.*;
import java.io.*;
import de.uib.utilities.logging.logging;

/**
 * This is a small parser for the JSON-like syntax of the OpsiDataSerializer.
 * Things different from JSON:
 * - values of the type SelectData.DataType are not in quotes
 * - all other values are in quotes
 */
class JsonParser
{
    private StringReader reader;
    private PositionType currentPosition=PositionType.JsonValue;
    private String currentValue=null;
    private boolean inList = false;
    Deque<PositionType> stack;
    
    public enum PositionType { ObjectBegin, ObjectEnd, ListBegin, ListEnd, JsonName, JsonValue };
    
    public JsonParser( String input )
    {
        reader = new StringReader( input );
        stack = new LinkedList<PositionType>();
    }
    
    public boolean next() throws IOException
    {
        while( true )
        {
            int i = reader.read();
            if( i == -1 )
                return false;
            logging.debug( this, (char) i + " " + currentPosition.toString() );
            managePosition();
            char c = (char) i;
            if( Character.isWhitespace( c ) )
                continue;
            if( c == ':' && currentPosition == PositionType.JsonName )
            {
                currentPosition = PositionType.JsonValue;
                continue;
            }
            if( c == ',' && currentPosition == PositionType.JsonValue )
            {
                if( !inList )
                    currentPosition = PositionType.JsonName;
                continue;
            }
            if( c == '{' && currentPosition == PositionType.JsonValue )
            {
                currentPosition = PositionType.ObjectBegin;
                inList = false;
                return true;
            }
            if( c == '}' && currentPosition == PositionType.JsonValue )
            {
                currentPosition = PositionType.ObjectEnd;
                return true;
            }
            if( c == '[' && currentPosition == PositionType.JsonValue )
            {
                currentPosition = PositionType.ListBegin;
                inList = true;
                return true;
            }
            if( c == ']' && currentPosition == PositionType.JsonValue )
            {
                currentPosition = PositionType.ListEnd;
                inList = false;
                return true;
            }
            if( ( c == '"' || Character.isLetter( c ) ) && (currentPosition == PositionType.JsonValue || currentPosition == PositionType.JsonName) )
            {
                currentValue = getNextValue( c );
                return true;
            }
            throw new IllegalArgumentException( "Unexpected character: " + c );
        }
    }
    
    public PositionType getPositionType()
    {
        return currentPosition;
    }
    
    public String getValue()
    {
        return currentValue;
    }
    
    private String getNextValue( char c ) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        builder.append( c );
        int i = reader.read();
        if( c == '"' )
        {
            while( i != -1 && ((char) i) != '"' )
            {
                builder.append( (char) i );
                i = reader.read();
            }
            if( i == -1 )
                throw new IllegalArgumentException( "Unexpected EOF" );
            builder.append( (char) i );
            return builder.toString();
        }
        else
        {
            while( i != -1 && (Character.isLetterOrDigit((char) i) || ((char) i) == '.' ) )
            {
                builder.append( (char) i );
                i = reader.read();
            }
            reader.skip(-1);
            return builder.toString();
        }
    }
    
    private void managePosition()
    {
        if( currentPosition == PositionType.ObjectBegin )
        {
            stack.push( PositionType.ObjectBegin );
            currentPosition = PositionType.JsonName;
        }
        else if( currentPosition == PositionType.ListBegin )
        {
            stack.push( PositionType.ListBegin );
            currentPosition = PositionType.JsonValue;
        }
        else if( currentPosition == PositionType.ObjectEnd || currentPosition == PositionType.ListEnd )
        {
            stack.pop();
            currentPosition = stack.peek();
            logging.debug( this, "managePosition: " + currentPosition.toString() );
            if( currentPosition == PositionType.ObjectBegin )
            {
                inList = false;
            }
            else
            {
                inList = true;
            }
            currentPosition = PositionType.JsonValue;
        }
    }
}
    