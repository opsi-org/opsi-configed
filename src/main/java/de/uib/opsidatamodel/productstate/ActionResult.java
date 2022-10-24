package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import de.uib.configed.Globals;


public class ActionResult
{
	public final static String KEY = "actionResult";
	
	//conflicting entries from several clients	
	public final static int	CONFLICT = -4;
	
	//no valid entry from service
	public final static int INVALID = -2;
	
	//product offers no entry
	public final static int	NOT_AVAILABLE = -6;
	
	//valid service states
	public final static int NONE = 0;
	public final static int FAILED= 2;
	public final static int SUCCESSFUL = 4;
	
	
	
	 
	
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
		states.add(FAILED);
		states.add(SUCCESSFUL);
		
		labels = new Vector<String>();
		labels.add(Globals.CONFLICTSTATEstring);
		labels.add(Globals.NOVALIDSTATEstring);
		labels.add("not_available");
		labels.add("none");
		labels.add("failed");
		labels.add("successful");
		
		
		
		state2label = new HashMap<Integer, String>();
		state2label.put(CONFLICT, Globals.CONFLICTSTATEstring);
		state2label.put(INVALID, Globals.NOVALIDSTATEstring);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(FAILED, "failed");
		state2label.put(SUCCESSFUL, "successful");
		
		label2state = new HashMap<String, Integer>();
		label2state.put(Globals.CONFLICTSTATEstring, CONFLICT);
		label2state.put(Globals.NOVALIDSTATEstring, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("failed", FAILED);
		label2state.put("successful", SUCCESSFUL);
		
		label2displayLabel = new HashMap<String, String>();
		label2displayLabel.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		label2displayLabel.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		label2displayLabel.put("not_available", "not_available");
		label2displayLabel.put("none", "none");
		label2displayLabel.put("failed", "failed");
		label2displayLabel.put("successful", "success");
		
		displayLabel2label = new HashMap<String, String>();
		displayLabel2label.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		displayLabel2label.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		displayLabel2label.put("not_available", "not_available");
		displayLabel2label.put("none", "none");
		displayLabel2label.put("always", "always");
		displayLabel2label.put("failed", "failed");
		displayLabel2label.put("success", "successful");
			
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
	public static ActionResult produceFromDisplayLabel(String display)
	{
		return produceFromLabel(
						displayLabel2label.get(display)
						);
	}
	
	public static ActionResult produceFromLabel(String label)
	{
		checkCollections();
		
		if (label == null)
			return new ActionResult(NOT_AVAILABLE);
		
		if (!labels.contains(label))
			return new ActionResult(INVALID);
		
		return new ActionResult(getVal(label));
	}
	
	
	// constructor
	public ActionResult()
	{
	}
	
	public ActionResult(int t)
	{
		if (existsState(t))
			state = t;
		else
			state = NOT_AVAILABLE;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(" test ActionResult.java");
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
			
			
			
		
