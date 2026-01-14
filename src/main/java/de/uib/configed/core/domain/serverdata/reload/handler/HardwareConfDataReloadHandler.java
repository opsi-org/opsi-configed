/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class HardwareConfDataReloadHandler extends AbstractReloadHandler {
	public HardwareConfDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		dataServices.cacheManager.clearCachedData(CacheIdentifier.HW_AUDIT_CONF);
		executor.runInParallel(dataServices.hardware::retrieveOpsiHWAuditConfPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		executor.runInParallel(dataServices.config::retrieveConfigOptionsPD);
	}
}
