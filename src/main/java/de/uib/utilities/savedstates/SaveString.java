package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;



public class SaveString extends SaveState
{
	SaveString(String key, SavedStates states)
	{
		super(key, "", states);
	}
	
	@Override
	public void serialize(Object  value)
	{
		states.setProperty(key, (String) value);
		states.store();
	}
	
	@Override
	public String deserialize()
	{
		if (states.getProperty(key, (String) defaultValue).equals(defaultValue))
			return null;
		
		return states.getProperty(key, (String) defaultValue);
	}

}
