/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.ConfigDataService;
import de.uib.configed.core.domain.serverdata.dataservice.HardwareDataService;

public class HardwareConfDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;
	private HardwareDataService hardwareDataService;

	public HardwareConfDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		cacheManager.clearCachedData(CacheIdentifier.HW_AUDIT_CONF);
		executor.runInParallel(() -> hardwareDataService.retrieveOpsiHWAuditConfPD());

		cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		executor.runInParallel(() -> configDataService.retrieveConfigOptionsPD());
	}
}
