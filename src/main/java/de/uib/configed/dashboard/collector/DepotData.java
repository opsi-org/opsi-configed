package de.uib.configed.dashboard.collector;

import java.util.HashMap;
import java.util.Map;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;

public class DepotData {
	private static Map<String, Map<String, Object>> depots = new HashMap<>();

	private static PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	private DepotData() {
	}

	public static Map<String, Map<String, Object>> getDepots() {
		return new HashMap<>(depots);
	}

	private static void retrieveDepots() {
		if (!depots.isEmpty()) {
			return;
		}

		persist.getHostInfoCollections().opsiHostsRequestRefresh();
		depots = persist.getHostInfoCollections().getDepots();
	}

	public static void clear() {
		depots.clear();
	}

	public static void retrieveData() {
		retrieveDepots();
	}
}
