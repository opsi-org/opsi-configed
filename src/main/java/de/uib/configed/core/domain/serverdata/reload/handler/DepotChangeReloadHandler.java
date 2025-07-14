/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DepotDataService;
import de.uib.configed.core.domain.serverdata.dataservice.ProductDataService;

public class DepotChangeReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ProductDataService productDataService;
	private DepotDataService depotDataService;

	public DepotChangeReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	public void setDepotDataService(DepotDataService depotDataService) {
		this.depotDataService = depotDataService;
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS);
		cacheManager.clearCachedData(CacheIdentifier.POSSIBLE_ACTIONS);
		executor.runInParallel(() -> productDataService.checkProductGlobalInfosPD(depotDataService.getDepot()));

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_IDS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		executor.runInParallel(() -> productDataService.retrieveProductIdsAndDefaultStatesPD());

		executor.waitForCompletion();
	}
}
