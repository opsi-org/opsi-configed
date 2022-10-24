package de.uib.opsidatamodel.productstate;

import java.util.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

public class ProductState extends HashMap<String, String>
{
	
	private static ProductState DEFAULT;
	
	public static ProductState getDEFAULT()
	{
		if (DEFAULT == null)
			DEFAULT = new ProductState(null);
		return DEFAULT;
	}
	
	public final static List<String> SERVICE_KEYS = new ArrayList<String>();
	static {//from 30_configed.conf
		SERVICE_KEYS.add("modificationTime"); //lastStateChange");
		SERVICE_KEYS.add("productId");
		SERVICE_KEYS.add("productVersion");
		SERVICE_KEYS.add("packageVersion");
		SERVICE_KEYS.add("targetConfiguration");
		SERVICE_KEYS.add("lastAction");
		SERVICE_KEYS.add("installationStatus");
		SERVICE_KEYS.add("actionRequest");
		SERVICE_KEYS.add("actionProgress");
		SERVICE_KEYS.add("actionResult");
		SERVICE_KEYS.add("priority");
		SERVICE_KEYS.add("actionSequence");
	}
	
	
	public final static LinkedHashMap<String, String> DB_COLUMNS = new LinkedHashMap<String, String>();
	static {
		DB_COLUMNS.put("productId", "VARCHAR(50)");
		DB_COLUMNS.put("productVersion", "VARCHAR(32)");
		DB_COLUMNS.put("packageVersion", "VARCHAR(16)");
		
		DB_COLUMNS.put("targetConfiguration", "VARCHAR(16)");
		DB_COLUMNS.put("lastAction", "VARCHAR(16)");
		DB_COLUMNS.put("installationStatus", "VARCHAR(16)");
		DB_COLUMNS.put("actionRequest", "VARCHAR(16)");
		DB_COLUMNS.put("actionProgress", "VARCHAR(255)");
		DB_COLUMNS.put("actionResult", "VARCHAR(16)");
		
		DB_COLUMNS.put("modificationTime", "TIMESTAMP"); //lastStateChange");  
	}
	
	
	public final static List<String> DB_COLUMN_NAMES = new ArrayList<String>(DB_COLUMNS.keySet()); 
	
	public final static int columnIndexLastStateChange = DB_COLUMN_NAMES.indexOf("modificationTime");
	
	//directly taken values
	public final static String KEY_lastStateChange = "stateChange";
	public final static String KEY_productVersion ="productVersion";
	public final static String KEY_packageVersion = "packageVersion";
	public final static String KEY_targetConfiguration = TargetConfiguration.KEY;
	public final static String KEY_lastAction =LastAction.KEY;
	public final static String KEY_installationStatus = InstallationStatus.KEY;
	public final static String KEY_actionRequest = ActionRequest.KEY;
	public final static String KEY_actionProgress =ActionProgress.KEY;
	public final static String KEY_actionResult = ActionResult.KEY;
	public final static String KEY_productId = "productId";

	public final static String KEY_productPriority = "priority";
	public final static String KEY_actionSequence = ActionSequence.KEY;
	
	//transformed values
	public final static String KEY_installationInfo =InstallationInfo.KEY;
	public final static String KEY_versionInfo = "versionInfo";
	
	//additional values
	public final static String KEY_position = "position";
	public final static String KEY_productName = "productName";

	
	
	
	final protected Map retrieved;
	
	public final static List<String> KEYS = new ArrayList<String>();
	static {
		KEYS.add(KEY_productId);
		KEYS.add(KEY_productName);
			
		KEYS.add(KEY_targetConfiguration);
		KEYS.add(KEY_installationStatus);
			
			
		KEYS.add(KEY_installationInfo);
			
		KEYS.add(KEY_actionResult);
		KEYS.add(KEY_actionProgress);
		KEYS.add(KEY_lastAction);
			
			
		KEYS.add(KEY_productPriority);
		KEYS.add(KEY_actionSequence);
		KEYS.add(KEY_actionRequest);
			
			
		KEYS.add(KEY_versionInfo);
			
		KEYS.add(KEY_productVersion);
		KEYS.add(KEY_packageVersion);
			
		KEYS.add(KEY_position);
			
		KEYS.add(KEY_lastStateChange);
	}
	
	
	public final static Map<String, String> key2servicekey = new HashMap<String, String>();
	static {
		key2servicekey.put(KEY_productId, "productId");
		//key2servicekey.put(KEY_productName
			
		key2servicekey.put(KEY_targetConfiguration, "targetConfiguration");
		key2servicekey.put(KEY_installationStatus, "installationStatus");
			
			
		//key2servicekey.put(KEY_installationInfo
			
		key2servicekey.put(KEY_actionResult, "actionResult");
		key2servicekey.put(KEY_actionProgress, "actionProgress");
		key2servicekey.put(KEY_lastAction, "lastAction");
			
			
		key2servicekey.put(KEY_position, "priority");
		key2servicekey.put(KEY_actionSequence, "actionSequence");
		key2servicekey.put(KEY_actionRequest, "actionRequest");
			
			
		//key2servicekey.put(KEY_versionInfo
			
		key2servicekey.put(KEY_productVersion, "productVersion");
		key2servicekey.put(KEY_packageVersion, "packageVersion");
			
		//key2servicekey.put(KEY_position
			
		key2servicekey.put(KEY_lastStateChange, "modificationTime");
	}
	
	
	
	private void readRetrieved()
	{
		//logging.debug(this, "retrieved " + retrievedState);
			
		put(KEY_productId, getRetrievedValue(key2servicekey.get(KEY_productId)) );
		
		put(KEY_targetConfiguration, getRetrievedValue(key2servicekey.get(KEY_targetConfiguration)));
		put(KEY_installationStatus, getRetrievedValue(key2servicekey.get(KEY_installationStatus)));
		
		put(KEY_actionResult, getRetrievedValue(key2servicekey.get(KEY_actionResult)));
		put(KEY_actionProgress, getRetrievedValue(key2servicekey.get(KEY_actionProgress)));
		put(KEY_lastAction, getRetrievedValue(key2servicekey.get(KEY_lastAction)));
		
		put(KEY_actionRequest, getRetrievedValue(key2servicekey.get(KEY_actionRequest)));
		
		put(KEY_productPriority, getRetrievedValue(key2servicekey.get(KEY_position)));
		put(KEY_actionSequence, getRetrievedValue(key2servicekey.get(KEY_actionSequence)));
		
		put(KEY_productVersion, getRetrievedValue(key2servicekey.get(KEY_productVersion)));
		put(KEY_packageVersion, getRetrievedValue(key2servicekey.get(KEY_packageVersion)));
		
		put(KEY_lastStateChange, getRetrievedValue(key2servicekey.get(KEY_lastStateChange)));
	}
	
	public ProductState(Map retrievedState, boolean transform)
	{
		super();
		this.retrieved = retrievedState;
		if (retrieved == null)
		{
			setDefaultValues();
		}
		else
		{
			readRetrieved();//retrievedState);
		}
		
		if (transform)
			setTransforms();
	}
			
	
	public ProductState(Map retrievedState)
	{
		this(retrievedState, true);
	}
	
	@Override 
	public String put(String key, String value)
	{
		assert !(KEYS.indexOf(key) < 0) : "key " + key + " not known, value was " + value + " , " + KEYS;
		return super.put(key, value);
	}
		
	private void setTransforms()
	{
		//logging.debug(this, "setTransforms on " + this);
		
		//format
		/*
		String stateChange = get(KEY_lastStateChange);
		logging.debug(this, "setTransforms stateChange " + stateChange);
		if (!stateChange.equals("null") &!stateChange.equals(""))
		{
			String[] sc = stateChange.split("");
			
			logging.debug(this, "setTransforms stateChange " + Arrays.toString(sc));
			
			if (sc.length >= 15)
				stateChange = sc[1]+sc[2]+sc[3]+sc[4]+'-'+sc[5]+sc[6]+'-'+sc[7]+sc[8]+' '+sc[9]+sc[10]+':'+sc[11]+sc[12]+':'+sc[13]+sc[14];
			
			logging.debug(this, "setTransforms stateChange " + Arrays.toString(sc));
		}
		put(KEY_lastStateChange, stateChange);
		*/
		
		//transformed values
		StringBuffer installationInfo = new StringBuffer();
				//the reverse will be found in in setInstallationInfo in InstallationStateTableModel
		
		LastAction lastAction = LastAction.produceFromLabel(get(KEY_lastAction));
		
		if ( !get(KEY_actionProgress).equals("") )
		{
			ActionResult result = ActionResult.produceFromLabel(get(KEY_actionResult));
			if (result.getVal() == ActionResult.FAILED) 
			{
				installationInfo.append (ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(": ");
			}
			installationInfo.append(get(KEY_actionProgress));
			installationInfo.append(" ( ");
			if (lastAction.getVal() > 0)
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
			installationInfo.append(" ) ");
			
			if (result.getVal() == ActionResult.FAILED) 
			{
				installationInfo.append (ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(" ");
			}
			
		}
		else
		{
			ActionResult result = ActionResult.produceFromLabel(get(KEY_actionResult));
			if (result.getVal() == ActionResult.SUCCESSFUL || result.getVal() == ActionResult.FAILED) 
			{
				installationInfo.append("");
				installationInfo.append (ActionResult.getDisplayLabel(result.getVal()));
			}
			//else
			//	installationInfo.append(" ");
			
			if (lastAction.getVal() > 0)
			{
				installationInfo.append(" (");
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
				installationInfo.append(")");
			}
		}	
		
		put(KEY_installationInfo, installationInfo.toString());
		
		String versionInfo = "";
		//logging.debug(this, "setTransforms get(KEY_productVersion) " +get(KEY_productVersion));
		if (!get(KEY_productVersion).equals(""))
			versionInfo = get(KEY_productVersion) + Globals.ProductPackageVersionSeparator.forDisplay() + get(KEY_packageVersion) ;
		//logging.debug(this, "setTransforms version info " + versionInfo);
		put(KEY_versionInfo, versionInfo );
	
	}
	
	private void setDefaultValues()
	{
		put(KEY_productId, "");
		put(KEY_productName, "");
		
		put(KEY_targetConfiguration, TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED));
		put(KEY_installationStatus, InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED));
		
		put(KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
		put(KEY_actionProgress, "");
		put(KEY_lastAction,  LastAction.getLabel(LastAction.NONE));
		
		put(KEY_actionRequest, ActionRequest.getLabel(ActionRequest.NONE));
		
		put(KEY_productPriority, "");
		put(KEY_actionSequence, "");
		
		put(KEY_productVersion, "");
		put(KEY_packageVersion, "");
		
		put(KEY_lastStateChange, "");
		
	}
	
	
	private String getRetrievedValue(String key)
	{
		
		assert !(SERVICE_KEYS.indexOf(key) < 0) : "service key " + key + " not known"; 
		/*
		if (retrieved.get(key) != null) 
			logging.info(this, "getRetrievedValue key" + key  
			 + " value  "  +  retrieved.get(key) + " class " +  retrieved.get(key).getClass());;
		*/
		if (retrieved.get(key) == null  
			|| 
			(retrieved.get(key) instanceof String && retrieved.get(key).equals("null"))
			)
			return "";
		
			
		String value = retrieved.get(key).toString();
		String predefValue = null;
		
		/*
		//we reduce the result to the predefined static strings if possible
		
		switch (key) 
		{
			case KEY_installationStatus:
				{
					
					
					break;
				}
			case KEY_actionRequest:
				{
					predefValue = ActionRequest.getStaticString(value);
					break;
				}
			case KEY_lastAction
		*/	
		
		
		if (predefValue != null)
			return predefValue;
		
		return value;
	}
	
}
	
	
	
	
	
