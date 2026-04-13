/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class DepotChangeReloadHandler extends AbstractReloadHandler {
	public DepotChangeReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.POSSIBLE_ACTIONS);
		executor.runInParallel(() -> dataServices.product.checkProductGlobalInfosPD(dataServices.depot.getDepot()));

		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_IDS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		executor.runInParallel(dataServices.product::retrieveProductIdsAndDefaultStatesPD);

		executor.waitForCompletion();
	}
}
