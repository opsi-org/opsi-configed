/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class DepotProductPropertiesDataReloadHandler extends AbstractReloadHandler {
	private CacheManager cacheManager;

	public DepotProductPropertiesDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.cacheManager = CacheManager.getInstance();
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PACKAGES);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS);
		executor.runInParallel(dataServices.product::retrieveProductsAllDepotsPD);

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS);
		executor.runInParallel(dataServices.product::retrieveAllProductPropertyDefinitionsPD);

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		executor.runInParallel(dataServices.product::retrieveDepotProductPropertiesPD);

		executor.waitForCompletion();
	}
}
