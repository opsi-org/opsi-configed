/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.collector;

import java.util.HashMap;
import java.util.Map;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;

public final class DepotData {
	private static Map<String, Map<String, Object>> depots = new HashMap<>();

	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private DepotData() {
	}

	public static Map<String, Map<String, Object>> getDepots() {
		return new HashMap<>(depots);
	}

	private static void retrieveDepots() {
		if (!depots.isEmpty()) {
			return;
		}

		persistenceController.reloadData(ReloadEvent.OPSI_HOST_DATA_RELOAD.toString());
		depots = persistenceController.getHostInfoCollections().getDepots();
	}

	public static void clear() {
		depots.clear();
	}

	public static void retrieveData() {
		retrieveDepots();
	}
}
