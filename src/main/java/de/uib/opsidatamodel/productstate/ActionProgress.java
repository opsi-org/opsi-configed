package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import de.uib.configed.Globals;


public class ActionProgress
{
	public final static String KEY = "actionProgress";
	
	//conflicting entries from several clients	
	public final static int	CONFLICT = -4;
	
	//no valid entry from service
	public final static int INVALID = -2;
	
	//product offers no entry
	public final static int	NOT_AVAILABLE = -6;
	
	//valid service states
	public final static int NONE = 0;
	public final static int INSTALLING = 1;
	public final static int CACHED = 2;
	
	 
	
	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> label2displayLabel;
	private static Map<String, String> displayLabel2label;
	
	private static Vector<Integer> states;
	private static Vector<String> labels;
	private static String[] choiceLabels;
	
	
	// instance variable
	private int state = INVALID;
	
	
	
	private static void checkCollections()
	{
		if (states != null)
			return;
		
		states = new Vector<Integer>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(NOT_AVAILABLE);
		states.add(NONE);
		states.add(INSTALLING);
		states.add(CACHED);
		
		labels = new Vector<String>();
		labels.add(Globals.CONFLICTSTATEstring);
		labels.add(Globals.NOVALIDSTATEstring);
		labels.add("not_available");
		labels.add("none");
		labels.add("installing");
		labels.add("cached");
		
		
		
		state2label = new HashMap<Integer, String>();
		state2label.put(CONFLICT, Globals.CONFLICTSTATEstring);
		state2label.put(INVALID, Globals.NOVALIDSTATEstring);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(INSTALLING, "installing");
		state2label.put(CACHED, "cached");
		
		label2state = new HashMap<String, Integer>();
		label2state.put(Globals.CONFLICTSTATEstring, CONFLICT);
		label2state.put(Globals.NOVALIDSTATEstring, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("installing", INSTALLING);
		label2state.put("cached", CACHED);
		
		label2displayLabel = new HashMap<String, String>();
		label2displayLabel.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		label2displayLabel.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		label2displayLabel.put("not_available", "not_available");
		label2displayLabel.put("none", "no process reported");
		//abel2displayLabel.put("none", "none");
		label2displayLabel.put("installing", "installing");
		label2displayLabel.put("cached", "cached");
		
		displayLabel2label = new HashMap<String, String>();
		displayLabel2label.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		displayLabel2label.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		displayLabel2label.put("not_available", "not_available");
		displayLabel2label.put("no process reported", "none");
		//displayLabel2label.put("none", "none");
		displayLabel2label.put("installing", "installing");
		displayLabel2label.put("cached", "cached");
			
		choiceLabels = new String[]{
			label2displayLabel.get("none")
		};
		
		
	}

	public static Map<String, String> getLabel2DisplayLabel()
	{
		checkCollections();
		
		return label2displayLabel;
	}
		
	public static boolean existsState(int state)
	{
		checkCollections();
		
		return (states.contains(state));
	}
	
	public static boolean existsLabel(String label)
	{
		checkCollections();
		
		return (labels.contains(label));
	}
	
	public static String getLabel( int state )
	{
		checkCollections();
		
		if (!existsState(state)) 
			return null;
		
		return state2label.get(state);
	}
	
	public static Vector<String> getLabels()
	{
            checkCollections();
            
            return labels;
        }
	
	public static Integer getVal(String label)
	{
		checkCollections();
		
		if (label == null || label.equals(""))
			return NONE;
		
		if (!existsLabel(label)) 
			return null;
		
		return label2state.get(label);
	}
	
	public static String getDisplayLabel(int state)
	{
		checkCollections();
		
		return label2displayLabel.get(getLabel( state));
	}
	
	//instance methods
	
	public int getVal()
	{
		return state;
	}
	
	public String getString()
	{
		return getLabel(state);
	}
	
	public String toString()
	{
		return getLabel(state);
	}
	
	
	// getting instances
	public static ActionProgress produceFromDisplayLabel(String display)
	{
		return produceFromLabel(
						displayLabel2label.get(display)
						);
	}
	
	public static ActionProgress produceFromLabel(String label)
	{
		checkCollections();
		
		if (label == null)
			return new ActionProgress(NOT_AVAILABLE);
		
		if (!labels.contains(label))
			return new ActionProgress(INVALID);
		
		return new ActionProgress(getVal(label));
	}
	
	
	// constructor
	public ActionProgress()
	{
	}
	
	public ActionProgress(int t)
	{
		if (existsState(t))
			state = t;
		else
			state = NOT_AVAILABLE;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(" test ActionProgress.java");
		checkCollections();
		Iterator iter = states.iterator();
		
		int i = 0;
		
		while (iter.hasNext())
		{
			i++;
			int state = (Integer) iter.next();
			//System.out.println("state " + i + " : " + state + " label " + getLabel(state));
		}
	}
			
	
	
}
			
