package de.uib.opsidatamodel.productstate;

import java.util.Map;

public final class Config {

	public static final Map<String, String> requiredActionForStatus = Map.ofEntries(
			Map.entry(InstallationStatus.KEY_INSTALLED, "setup"),
			Map.entry(InstallationStatus.KEY_NOT_INSTALLED, "uninstall"));

	private static Config instance;

	private Config() {
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}

		return instance;
	}

}
