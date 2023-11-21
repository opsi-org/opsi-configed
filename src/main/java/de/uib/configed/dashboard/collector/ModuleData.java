/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public final class ModuleData {
	private static List<String> modules = new ArrayList<>();
	private static List<String> activeModules = new ArrayList<>();
	private static List<String> expiredModules = new ArrayList<>();

	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ModuleData() {
	}

	public static List<String> getModules() {
		return new ArrayList<>(modules);
	}

	private static void retrieveModules() {
		if (!modules.isEmpty()) {
			return;
		}

		for (Map<String, Object> moduleInfo : persistenceController.getModuleDataService().getModules()) {
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

		List<Map<String, Object>> modules = persistenceController.getModuleDataService().getModules();

		for (Map<String, Object> moduleInfo : modules) {
			String moduleId = moduleInfo.get("module_id").toString();

			if ("valid".equals(moduleInfo.get("_state").toString())) {
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
