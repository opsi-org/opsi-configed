package de.uib.opsidatamodel.productstate;

import java.util.*;

public class Config
{
	
	public HashMap<String, String> requiredActionForStatus;
	
	
	private static Config instance;
	
	
	
	private Config()
	{
		requiredActionForStatus = new HashMap<String, String>();
		requiredActionForStatus.put("installed", "setup");
		requiredActionForStatus.put("not_installed", "uninstall");
	}
	
	
	public static Config getInstance()
	{
		if (instance == null)
			instance = new Config();
	
		return instance;
	}
	
}

