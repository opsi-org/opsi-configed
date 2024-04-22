/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.DepotDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;

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
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS);
		cacheManager.clearCachedData(CacheIdentifier.POSSIBLE_ACTIONS);
		productDataService.checkProductGlobalInfosPD(depotDataService.getDepot());

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_IDS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		productDataService.retrieveProductIdsAndDefaultStatesPD();
	}
}
