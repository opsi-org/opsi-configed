package de.uib.configed.tree;

import javax.swing.tree.*;

public class GroupNode extends IconNode
{
	protected String description;
	protected boolean childsArePersistent = true;
	protected boolean allowingOnlyGroupChilds = false;
	protected boolean allowingSubGroups = true;
	protected boolean immutable = false;
	protected boolean fixed = false;  
	// fixed means "is generated structure" 
	
	
	public GroupNode(Object userObject, String description)
	{
		super(userObject, true);
		this.description = description;
	}
	
	public GroupNode(Object userObject)
	{
		this(userObject, "" + userObject);
	}
	
	public void setChildsArePersistent(boolean b)
	{
		childsArePersistent = b;
	}
	
	public boolean getChildsArePersistent()
	{
		return childsArePersistent;
	}
	
	public void setAllowsOnlyGroupChilds(boolean b)
	{
		allowingOnlyGroupChilds = b;
	}
	
	public boolean allowsOnlyGroupChilds()
	{
		return allowingOnlyGroupChilds;
	}
	
	public void setAllowsSubGroups(boolean b)
	{
		allowingSubGroups = b;
	}
	
	public boolean allowsSubGroups()
	{
		return allowingSubGroups;
	}
	
	public void setImmutable(boolean b)
	{
		immutable = b;
	}
	
	public boolean isImmutable()
	{
		return immutable;
	}
	
	public void setFixed(boolean b)
	{
		fixed = b;
	}
	
	public boolean isFixed()
	{
		return fixed;
	}
	
	/*
	public void setGeneratedStructure(boolean b)
	{
		generatedStructure = b;
	}
	
	public boolean isGeneratedStructure()
	{
		return generatedStructure;
	}
	*/
	
	
	
	
}
		