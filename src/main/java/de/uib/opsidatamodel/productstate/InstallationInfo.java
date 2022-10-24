package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.*;
import de.uib.configed.Globals;


public class InstallationInfo
{
	public final static String KEY = "installationInfo";
	
	//valid states
	public final static int NONE = 0;
	public final static int FAILED= 2;
	
	public final static String NONEstring = "";
	public final static String FAILEDstring= "failed";
	public final static String SUCCESSstring= "success";
	
	public final static String NONEdisplayString = "none";
	public final static String FAILEDdisplayString= "failed";
	public final static String SUCCESSdisplayString= "success";
	
	public final static String MANUALLY = "manually set";
	
	public final static LinkedHashSet<String> defaultDisplayValues = new LinkedHashSet<String>();
	static{
		defaultDisplayValues.add(NONEdisplayString);
		defaultDisplayValues.add(SUCCESSdisplayString);
		defaultDisplayValues.add(FAILEDdisplayString);
	}

	
}
			
			
			
		
