package de.uib.configed.clientselection;

import de.uib.utilities.logging.logging;
import java.math.BigInteger;

/**
 * Any kind of data needed for the operations.
 */
public class SelectData
{
	private DataType dataType;
	private Object dataObject;

	public enum DataType { TextType, IntegerType, BigIntegerType, DoubleType, EnumType, DateType, NoneType }

	public SelectData( Object data, DataType type )
	{
		dataType = type;
		dataObject = data;
		
		logging.debug(this, "got data, type " + data + ", " + type);

		switch( type )
		{
		case TextType:
			if( !(data instanceof String) )
			{
				logging.error( this, "Data is no String" );
				throw new IllegalArgumentException( "Data is no String" );
			}
			break;
		case IntegerType:
			if( !(data instanceof Integer) )
			{
				logging.error( this, "Data is no Integer" );
				throw new IllegalArgumentException( "Data is no Integer" );
			}
			break;
		case BigIntegerType:
			if( !(data instanceof Long) )
			{
				logging.error( this, "Data is no Long" );
				throw new IllegalArgumentException( "Data is no Long" );
			}
			break;

		case DateType:
			{
				/*
				if( data instanceof String )
				{
					java.sql.Date d = java.sql.Date.valueOf( (String) data );
					//throws IllegalArgumentException
				}
				else
				*/
				
				if( !(data instanceof String ) )
				
					throw new IllegalArgumentException( "Data is not a (date) string" );
				

				break;
			}

		case DoubleType:
			if( !(data instanceof Double) )
			{
				logging.error( this, "Data is no Double" );
				throw new IllegalArgumentException( "Data is no Double" );
			}
			break;
		case EnumType:
			if( !(data instanceof String) )
			{
				logging.error( this, "Data is no String" );
				throw new IllegalArgumentException( "Data is no String" );
			}
			break;
		}
	}

	public DataType getType()
	{
		return dataType;
	}

	public Object getData()
	{
		return dataObject;
	}
}
