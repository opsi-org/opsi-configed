package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public abstract class SaveState
{
	String key;
	Object defaultValue;
	SavedStates states;
	
	SaveState()
	{
	}
	
	public SaveState(String key, Object defaultValue, SavedStates states)
	{
		this.key = key;
		this.defaultValue = defaultValue;
		this.states = states;
		// states.addKey(key, ""); //for classifiedpropertiesstore
	}
	
	public void setDefaultValue(Object val)
	{
		defaultValue = val;
	}
	
	public void serialize(Object ob)
	{
		states.store();
		//we store every time when we add an object
	}
	
	public Object deserialize()
	{
		return null;
	}
}