package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;


public class GenericDateElement extends SelectElement
{
	public GenericDateElement( String[] name, String... localizedName )
	{
		super(name, localizedName);
	}

	public List<SelectOperation> supportedOperations()
	{
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		
		result.add(new DateGreaterThanOperation(this));
		result.add(new DateGreaterOrEqualOperation(this));
		result.add(new DateEqualsOperation(this));
		result.add(new DateLessOrEqualOperation(this));
		result.add(new DateLessThanOperation(this));
		
		return result;
	}

	//     public SelectOperation createOperation( String operation, SelectData data )
	//     {
	//         return Backend.getBackend().createOperation( operation, data, this );
	//     }
}