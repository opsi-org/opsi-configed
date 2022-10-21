package de.uib.utilities.savedstates;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import de.uib.opsidatamodel.PersistenceController;



public class SavedStates extends PropertiesStore
{
	public SaveInteger savedMaxShownLogLevel;
	public SaveInteger saveUsageCount;
	public SaveInteger saveMainLocationX;
	public SaveInteger saveMainLocationY;
	public SaveInteger saveMainLocationWidth;
	public SaveInteger saveMainLocationHeight;
	public SaveBoolean saveRegisterUser;
	
	public SaveDepotSelection saveDepotSelection;
	public SaveString saveGroupSelection;
	public SaveString saveSWauditKindOfExport;
	public SaveString saveSWauditExportDir;
	public SaveString saveSWauditExportFilePrefix;
	
	//public final SaveSet saveLocalbootproductFilterset;
	public SessionSaveSet<String> saveLocalbootproductFilter;
	public SessionSaveSet<String> saveNetbootproductFilter;
	//public final SessionSaveSet<String> saveLocalbootproductSelection;
		//up to now not used
	
	public Map<String, SaveString> saveServerConfigs; 
	
	public SavedStates(File store)
	{
		super(store);
		savedMaxShownLogLevel = new SaveInteger("savedMaxShownLogLevel", 0, this);
		saveUsageCount = new SaveInteger("saveUsageCount", 0, this);
		saveMainLocationX = new SaveInteger("saveMainLocationX", 0 , this);
		saveMainLocationY = new SaveInteger("saveMainLocationY", 0 , this);
		saveMainLocationWidth = new SaveInteger("saveMainLocationWidth", 0 , this);
		saveMainLocationHeight = new SaveInteger("saveMainLocationHeight", 0 , this);
		saveDepotSelection = new SaveDepotSelection(this);
		saveGroupSelection = new SaveString("groupname",  this);
		saveSWauditKindOfExport = new SaveString("swaudit_kind_of_export", this); 
		saveSWauditExportDir = new SaveString("swaudit_export_dir", this); 
		saveSWauditExportFilePrefix = new SaveString("swaudit_export_file_prefix", this); 
		
		saveRegisterUser = new SaveBoolean( PersistenceController.KEY_USER_REGISTER, false, this);
		//we memorize it locally in order to signal if the config has changed
		saveLocalbootproductFilter = new SessionSaveSet();
		saveNetbootproductFilter = new SessionSaveSet();
		//saveLocalbootproductSelection = new SessionSaveSet();
		saveServerConfigs = new HashMap<String, SaveString>();
		
		
	}
	
	
	public void store()
	{
		try{
			super.store(null);
		}
		catch(IOException iox)
		{
			logging.warning(this, "could not store saved states, " + iox);
		}
	}
		

}
