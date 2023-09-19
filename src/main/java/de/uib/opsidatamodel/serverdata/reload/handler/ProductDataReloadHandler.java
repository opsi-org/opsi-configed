/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;

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
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		productDataService.retrieveProductInfosPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_ROWS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PACKAGES);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS);
		productDataService.retrieveProductsAllDepotsPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTY_STATES);

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEPOT_STATES);

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS);
		productDataService.retrieveAllProductDependenciesPD();

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		productDataService.retrieveDepotProductPropertiesPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GROUPS);
		groupDataService.retrieveProductGroupsPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTIES);
	}
}
