/*
  * LogEvent
	* organization: uib.de
 * @author  R. Roeder 
 */
 
package de.uib.utilities.logging;

public class LogEvent
{
	String info = "";
	int level = -1;
	boolean showOnGui = false;
	Object source = null;
	
	
	public LogEvent()
	{
	}
	
	public LogEvent(Object source, String info, int level, boolean show)
	{
		this.info = info;
		this.level = level;
		this.showOnGui = show;
		this.source = source;
	}
	
	public Object getSource()
	{
		return source;
	}
	
	public String getInfo()
	{
		return info;
	}
	
	public int getLevel()
	{
		return level;	
	}
	
	public boolean showOnGui()
	{
		return showOnGui;
	}
	
}
