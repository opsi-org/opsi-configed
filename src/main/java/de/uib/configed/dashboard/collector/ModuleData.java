package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public class ModuleData {
	private static List<String> modules = new ArrayList<>();
	private static List<String> activeModules = new ArrayList<>();
	private static List<String> expiredModules = new ArrayList<>();

	private static PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	private ModuleData() {
	}

	public static List<String> getModules() {
		return new ArrayList<>(modules);
	}

	private static void retrieveModules() {
		if (!modules.isEmpty()) {
			return;
		}

		modules.clear();

		for (Map<String, Object> moduleInfo : persist.getModules()) {
			modules.add(moduleInfo.get("module_id").toString());
		}
	}

	public static List<String> getActiveModules() {
		return new ArrayList<>(activeModules);
	}

	public static List<String> getExpiredModules() {
		return new ArrayList<>(expiredModules);
	}

	private static void retrieveModuleState() {
		if (!activeModules.isEmpty() && !expiredModules.isEmpty()) {
			return;
		}

		activeModules.clear();
		expiredModules.clear();

		List<Map<String, Object>> modules = persist.getModules();

		for (Map<String, Object> moduleInfo : modules) {
			String moduleId = moduleInfo.get("module_id").toString();

			if ((moduleInfo.get("_state")).toString().equals("valid")) {
				activeModules.add(moduleId);
			} else {
				expiredModules.add(moduleId);
			}
		}
	}

	public static void clear() {
		modules.clear();
		activeModules.clear();
		expiredModules.clear();
	}

	public static void retrieveData() {
		retrieveModules();
		retrieveModuleState();
	}
}
