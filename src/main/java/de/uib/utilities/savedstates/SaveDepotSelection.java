package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;


public class SaveDepotSelection extends SaveState
{
	public SaveDepotSelection(SavedStates states)
	{
		super("selectedDepots", "", states);
	}
	
	@Override
	public void serialize(Object selectedDepots)
	{
		states.setProperty(key, Arrays.toString(
			(String[]) selectedDepots)
			);
		states.store();
	}
	
	@Override
	public String[] deserialize()
	{
		String s = states.getProperty(key, (String) defaultValue);
		if (s.equals(""))
			return null;
		
		s = s.substring(1);
		s = s.substring(0, s.length()-1);
		
		String[] parts = s.split(",");
		for (int i = 0; i < parts.length; i++)
		{
			parts[i] = parts[i].trim();
		}
		
		return parts;
	}
}	
	