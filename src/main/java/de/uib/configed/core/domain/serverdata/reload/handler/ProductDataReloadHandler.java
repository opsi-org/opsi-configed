/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.GroupDataService;
import de.uib.configed.core.domain.serverdata.dataservice.ProductDataService;

public class ProductDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ProductDataService productDataService;
	private GroupDataService groupDataService;

	public ProductDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PACKAGES);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS);
		executor.runInParallel(productDataService::retrieveProductsAllDepotsPD);

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS);
		executor.runInParallel(productDataService::retrieveAllProductDependenciesPD);

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		executor.runInParallel(productDataService::retrieveDepotProductPropertiesPD);

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GROUPS);
		executor.runInParallel(groupDataService::retrieveProductGroupsPD);

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTIES);

		executor.waitForCompletion();
	}
}
