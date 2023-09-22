/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;

public class HostConfigDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;

	public HostConfigDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		configDataService.retrieveConfigOptionsPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_CONFIGS);
		configDataService.retrieveHostConfigsPD();
	}
}
