package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class SaveBoolean extends SaveState
{
	public SaveBoolean(String key, Object defaultValue, SavedStates states)
	{
		super(key, defaultValue, states);
		if (!(defaultValue instanceof Boolean))
			logging.error("default value must be a Boolean");
	}
	
	
	@Override
	public void serialize(Object  value)
	{
		if (value == null)
		{
			states.removeProperty( key );
		}
		else
		{
			states.setProperty(key, value.toString());
		}
		states.store();
		
	}
	
	@Override
	public String deserialize()
	{
		logging.info(this, "deserialize states" + states);
		logging.info(this, "deserialize  getProperty " + states.getProperty(key, defaultValue.toString()));
		return states.getProperty(key, defaultValue.toString());
	}
	
	public Boolean deserializeAsBoolean()
	{
		Boolean result= null;
		try
		{
			result = Boolean.valueOf( deserialize() );
		}
		catch(Exception ex)
		{
			logging.warning(this, "deserializeAsBoolean error " + ex);
		}
		if (result == null)
			result = (Boolean) defaultValue;
		
		return result;
	}
	
}
	