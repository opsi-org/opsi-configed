package de.uib.utilities.datastructure;

import java.util.*;


//very similar to TableEntry
public class RelationElement<S , O> extends HashMap<S, O>
{
	
	protected java.util.List<S> allowedAttributes;
	protected java.util.List<S> minimalAttributes;
	
	public RelationElement()
	{
		super();
	}
	
	public RelationElement(RelationElement<S, O> rowmap)
	{
		super(rowmap);
	}
	
	public void setAllowedAttributes(java.util.List<S> allowedAttributes)
	{
		this.allowedAttributes = allowedAttributes;
	}
	
	@Override 
	public O get(Object key)
	{
		assert allowedAttributes == null || allowedAttributes.indexOf(key) >= 0:  /* if not, we get the error message: */  "key " + key + " not allowed";
		
		return super.get(key);
	}		
			
	
	public java.util.List<S> getAllowedAttributes()
	{
		return allowedAttributes;
	}
	

	
	
}



