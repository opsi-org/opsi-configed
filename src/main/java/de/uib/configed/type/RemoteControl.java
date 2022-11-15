package de.uib.configed.type;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;

public class RemoteControl 
{
	
	public static final String CONFIG_KEY = "configed.remote_control";
	public static final String COMMAND_KEY = "command";
	public static final String DESCRIPTION_KEY = "description";
	public static final String EDITABLE_KEY = "editable";
	
	public String name = "";
	public String command = "";
	public String description = "";
	public String editable = "true";
	
	
	
	public RemoteControl()
	{
	}
	
	public void setName(Object s)
	{
		name = "" + s;
	}
	
	
	
	public void setCommand(Object s)
	{
		command = "" + s;
	}
	
	public void setDescription(Object s)
	{
		description = "" + s;
	}
	
	public void setEditable(Object s)
	{
		editable = "" + s;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getEditable()
	{
		return editable;
	}
	
	public String toString()
	{
		return getName() + ": " + getCommand() + " ( " + getDescription() + ") editable " + getEditable();
	}
}
