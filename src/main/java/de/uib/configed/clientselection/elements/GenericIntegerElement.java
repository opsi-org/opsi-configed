package de.uib.configed.clientselection.elements;

import java.util.*;
import de.uib.configed.clientselection.*;
import de.uib.configed.clientselection.operations.*;


public class GenericIntegerElement extends SelectElement
{
	public GenericIntegerElement( String[] name, String... localizedName )
	{
		super(name, localizedName);
	}

	public List<SelectOperation> supportedOperations()
	{
		List<SelectOperation> result = new LinkedList<SelectOperation>();
		result.add(new IntLessThanOperation(this));
		result.add(new IntLessOrEqualOperation(this));
		result.add(new IntEqualsOperation(this));
		result.add(new IntGreaterOrEqualOperation(this));
		result.add(new IntGreaterThanOperation(this));

		return result;
	}
}