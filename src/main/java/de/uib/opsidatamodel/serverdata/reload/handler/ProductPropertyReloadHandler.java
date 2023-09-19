/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;

public class ProductPropertyReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ProductDataService productDataService;

	public ProductPropertyReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTIES);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEFINITIONS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS);
		productDataService.retrieveAllProductPropertyDefinitionsPD();
	}
}
