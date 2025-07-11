/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.ConfigDataService;

public class ConfigOptionsDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;

	public ConfigOptionsDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		configDataService.retrieveConfigOptionsPD();
	}
}
