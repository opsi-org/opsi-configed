/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class DepotPropertiesDataReloadHandler extends AbstractReloadHandler {
	public DepotPropertiesDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		dataServices.cacheManager.clearCachedData(CacheIdentifier.ALL_DEPOTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.CONFIG_SERVER);
		executor.runInParallel(dataServices.hostInfoCollections::retrieveOpsiHostsPD);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		executor.runInParallel(dataServices.config::retrieveConfigOptionsPD);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOTS_PERMITTED);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION);
		executor.runInParallel(dataServices.userRoles::checkConfigurationPD);

		executor.waitForCompletion();
	}
}
