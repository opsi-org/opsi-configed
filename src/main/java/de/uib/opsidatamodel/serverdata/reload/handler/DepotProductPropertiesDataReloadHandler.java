/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;

public class DepotProductPropertiesDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ProductDataService productDataService;

	public DepotProductPropertiesDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		productDataService.retrieveProductInfosPD();
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PACKAGES);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS);
		productDataService.retrieveProductsAllDepotsPD();
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS);
		productDataService.retrieveAllProductPropertyDefinitionsPD();
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		productDataService.retrieveDepotProductPropertiesPD();
	}
}
