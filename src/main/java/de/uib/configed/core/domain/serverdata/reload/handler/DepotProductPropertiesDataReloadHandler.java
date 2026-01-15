/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class DepotProductPropertiesDataReloadHandler extends AbstractReloadHandler {
	public DepotProductPropertiesDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PACKAGES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS);
		executor.runInParallel(dataServices.product::retrieveProductsAllDepotsPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS);
		executor.runInParallel(dataServices.product::retrieveAllProductPropertyDefinitionsPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		executor.runInParallel(dataServices.product::retrieveDepotProductPropertiesPD);

		executor.waitForCompletion();
	}
}
