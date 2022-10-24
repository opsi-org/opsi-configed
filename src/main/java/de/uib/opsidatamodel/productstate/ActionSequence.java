package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import de.uib.configed.Globals;


public class ActionSequence
{
	public final static String KEY = "actionSequence";
	
	private static Map<String, String> displayLabel2label;
	
	private static void checkCollections()
	{
		if (displayLabel2label == null)
			displayLabel2label = new HashMap<String, String>();
			
		for (int i = -100; i <= 100; i++)
		{
			String st = "" + i;
			displayLabel2label.put(st, st);
		}
		
		displayLabel2label.put(""+0, "");
	}
	
	public static Map<String, String> getLabel2DisplayLabel()
	{
		checkCollections();
		return displayLabel2label; 
	}
}
			
			
			
		
