package de.uib.configed.type;
import java.util.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.datastructure.*;

public class OpsiHwAuditDevicePropertyType
{
	protected String opsiDbColumnName;
	protected String opsiDbColumnType;
	
	protected String reportFunction;
	protected String uiName;
	protected String hwClassName;
	
	protected Boolean displayed;
	
	public OpsiHwAuditDevicePropertyType( String hwClass ) 
	{
		this.hwClassName = hwClass;
	}
	
	public String getHwClassName()
	{
		return hwClassName;
	}
	
	public void setOpsiDbColumnName( String s )
	{
		opsiDbColumnName = s;
	}
	
	
	public String getOpsiDbColumnName()
	{
		return opsiDbColumnName;
	}
	
	public void setOpsiDbColumnType( String s )
	{
		opsiDbColumnType = s;
	}
	
	public String getOpsiDbColumnType()
	{
		return opsiDbColumnType;
	}
	
	public void setUiName( String s )
	{
		uiName = s;
	}
	
	public String getUiName()
	{
		return uiName;
	}
	
	public void setDisplayed( boolean b )
	{
		displayed = b;
	}
	
	public Boolean getDisplayed()
	{
		return displayed;
	}
	
	
	
	
	@Override
	public String toString()
	{
		return hwClassName + ": " + opsiDbColumnName + " (" + displayed + ")";
	}
	
	
	
	
}
	
	
	