package de.uib.opsidatamodel.productstate;

import java.util.HashMap;
import java.util.Map;

public class Config {

	public Map<String, String> requiredActionForStatus;

	private static Config instance;

	private Config() {
		requiredActionForStatus = new HashMap<>();
		requiredActionForStatus.put("installed", "setup");
		requiredActionForStatus.put("not_installed", "uninstall");
	}

	public static Config getInstance() {
		if (instance == null)
			instance = new Config();

		return instance;
	}

}
