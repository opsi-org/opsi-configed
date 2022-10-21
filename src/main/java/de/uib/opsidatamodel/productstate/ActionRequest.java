package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import de.uib.configed.Globals;


public class ActionRequest
{
	public final static String KEY = "actionRequest";
	
	//conflicting entries from several clients	
	public final static int	CONFLICT = -4;
	
	//no valid entry from service
	public final static int INVALID = -2;
	
	//product offers no actions
	public final static int	NOT_AVAILABLE = -6;
	
	//valid service states
	public final static int NONE = 0;
	public final static int SETUP= 1;
	public final static int UPDATE= 3;
	public final static int UNINSTALL = 5;
	public final static int ALWAYS= 7;
	public final static int ONCE= 8;
	public final static int CUSTOM= 11;
	public final static int LAST = CUSTOM;
	 
	//textcolors
	public final static Color NONEcolor = Globals.INVISIBLE;
	public final static Color SETUPcolor= Globals.actionRed;
	public final static Color UPDATEcolor= Globals.actionRed;
	public final static Color UNINSTALLcolor = Color.blue;
	public final static Color ALWAYScolor= Globals.actionRed;
	public final static Color ONCEcolor= Globals.actionRed;
	public final static Color CUSTOMcolor= Globals.actionRed;
	public final static Color LASTcolor = Color.black;
	
	
	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> label2displayLabel;
	private static Map<String, String> displayLabel2label;
	private static Map<String, Color> label2textColor;
	private static Map<String, Integer> serviceValue2state;
	
	private static Vector<Integer> states;
	private static Vector<Integer> possibleRequests;
	private static Vector<String> labels;
	private static String[] choiceLabels;
	private static Vector<String> serviceValues;
	private static Vector<String> scriptKeys;
	
	private static Map<String, String> scriptKey2label;
	
	
	
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
		states.add(SETUP);
		states.add(UPDATE);
		states.add(UNINSTALL);
		states.add(ALWAYS);
		states.add(ONCE);
		states.add(CUSTOM);
		
		possibleRequests = new Vector<Integer>();
		possibleRequests.add(SETUP);
		possibleRequests.add(UPDATE);
		possibleRequests.add(UNINSTALL);
		possibleRequests.add(ALWAYS);
		possibleRequests.add(ONCE);
		possibleRequests.add(CUSTOM);
		
		labels = new Vector<String>();
		labels.add(Globals.CONFLICTSTATEstring);
		labels.add(Globals.NOVALIDSTATEstring);
		labels.add("not_available");
		labels.add("none");
		labels.add("setup");
		labels.add("update");
		labels.add("uninstall");
		labels.add("always");
		labels.add("once");
		labels.add("custom");
		
		
		
		state2label = new HashMap<Integer, String>();
		state2label.put(CONFLICT, Globals.CONFLICTSTATEstring);
		state2label.put(INVALID, Globals.NOVALIDSTATEstring);
		state2label.put(NOT_AVAILABLE, "not_available");
		state2label.put(NONE, "none");
		state2label.put(SETUP, "setup");
		state2label.put(UPDATE, "update");
		state2label.put(UNINSTALL, "uninstall");
		state2label.put(ALWAYS, "always");
		state2label.put(ONCE, "once");
		state2label.put(CUSTOM, "custom");
		
		
		serviceValue2state = new LinkedHashMap<String, Integer>();
		serviceValue2state.put("setup", SETUP);
		serviceValue2state.put("update", UPDATE);
		serviceValue2state.put("uninstall", UNINSTALL);
		serviceValue2state.put("always", ALWAYS);
		serviceValue2state.put("once", ONCE);
		serviceValue2state.put("custom", CUSTOM);
		
		serviceValues = new Vector<String> (serviceValue2state.keySet());
		scriptKeys = new Vector<String>();
		//scriptKey2state = new HashMap<String, Integer>();
		scriptKey2label = new HashMap<String, String>();
		
		for (String request : serviceValues)
		{
			scriptKeys.add(request + "Script");
			//scriptKey2state.put(request + "Script", serviceValue2state.get(request));
			scriptKey2label.put(request + "Script",
				state2label.get(serviceValue2state.get(request) )
				);
		}
		
		
		label2state = new HashMap<String, Integer>();
		label2state.put(Globals.CONFLICTSTATEstring, CONFLICT);
		label2state.put(Globals.NOVALIDSTATEstring, INVALID);
		label2state.put("not_available", NOT_AVAILABLE);
		label2state.put("none", NONE);
		label2state.put("setup", SETUP);
		label2state.put("update", UPDATE);
		label2state.put("uninstall", UNINSTALL);
		label2state.put("always", ALWAYS);
		label2state.put("once", ONCE);
		label2state.put("custom", CUSTOM);
		
		label2displayLabel = new HashMap<String, String>();
		label2displayLabel.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		label2displayLabel.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		label2displayLabel.put("not_available", "not_available");
		label2displayLabel.put("none", "none");
		label2displayLabel.put("setup", "setup");
		label2displayLabel.put("update", "update");
		label2displayLabel.put("uninstall", "uninstall");
		label2displayLabel.put("always", "always");
		label2displayLabel.put("once", "once");
		label2displayLabel.put("custom", "custom");
		
		displayLabel2label = new HashMap<String, String>();
		displayLabel2label.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		displayLabel2label.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		displayLabel2label.put("not_available", "not_available");
		displayLabel2label.put("none", "none");
		displayLabel2label.put("setup", "setup");
		displayLabel2label.put("update", "update");
		displayLabel2label.put("uninstall", "uninstall");
		displayLabel2label.put("always", "always");
		displayLabel2label.put("once", "once");
		displayLabel2label.put("custom", "custom");
			
		choiceLabels = new String[]{
			label2displayLabel.get("none"),
			label2displayLabel.get("setup"),
			label2displayLabel.get("update"),
			label2displayLabel.get("uninstall"),
			label2displayLabel.get("always"),
			label2displayLabel.get("once"),
			label2displayLabel.get("custom"),
		};
		
		label2textColor = new HashMap<String, Color>();
		label2textColor.put("none", NONEcolor);
		label2textColor.put("setup", SETUPcolor);
		label2textColor.put("update", UPDATEcolor);
		label2textColor.put("uninstall", UNINSTALLcolor);
		label2textColor.put("always", ALWAYScolor);
		label2textColor.put("once", ONCEcolor);
		label2textColor.put("custom", CUSTOMcolor);
		
		
		
		
	}
	
	public static Vector<Integer> getPossibleRequests()
	{
		checkCollections();
		return possibleRequests;
	}
	
	public static Vector<String> getServiceValues()
	{
		checkCollections();
		return serviceValues;
	}
	
	public static Vector<String> getScriptKeys()
	{
		checkCollections();
		return scriptKeys;
	}
	
	public static Map<String, String> getScriptKey2Label()
	{
		checkCollections();
		return scriptKey2label;
	}

	public static Map<String, String> getLabel2DisplayLabel()
	{
		checkCollections();
		
		return label2displayLabel;
	}
	
	public static Map<Integer, String> getState2Label()
	{	
		checkCollections();
		return state2label;
	}
	
	
	public static Map<String, Color> getLabel2TextColor()
	{
		checkCollections();
		
		return label2textColor;
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
	
	public static String getStaticString(String label)
	{
		Integer state = label2state.get(label);
		
		if (state == null)
			return null;
		
		return state2label.get(state);
	}
	
	public static String[] getDisplayLabelsForChoice()
	{
		checkCollections();
		
		return choiceLabels;
		/*
		ArrayList<String> list = new ArrayList<String>();
		
		for (int i = NONE; i <= LAST; i++)
		{
			if (states.indexOf(i) >= 0 )
				list.add(getDisplayLabel(i));
		}
		
		return list;
		*/
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
	public static ActionRequest produceFromDisplayLabel(String display)
	{
		return produceFromLabel(
						displayLabel2label.get(display)
						);
	}
	
	public static ActionRequest produceFromLabel(String label)
	{
		checkCollections();
		
		if (label == null)
			return new ActionRequest(NONE); //NOT_AVAILABLE);
		
		if (!labels.contains(label))
			return new ActionRequest(INVALID);
		
		return new ActionRequest(getVal(label));
	}
	
	
	// constructor
	public ActionRequest()
	{
	}
	
	public ActionRequest(int t)
	{
		if (existsState(t))
			state = t;
		else
			state = NOT_AVAILABLE;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(" test ActionRequest.java");
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
			
			
			
		
