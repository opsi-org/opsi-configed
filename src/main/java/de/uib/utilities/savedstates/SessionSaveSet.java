package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

	
public class SessionSaveSet<T> extends SaveState 
{
	Set<T> saveObject;
	
	public SessionSaveSet()
	{
	}
	
	public void serialize(Object ob)
	{
		if (ob == null)
			saveObject = null;
		else
			saveObject = (Set<T>) ob;
	}
	
	public Object deserialize()
	{
		return saveObject;
	}
}
	