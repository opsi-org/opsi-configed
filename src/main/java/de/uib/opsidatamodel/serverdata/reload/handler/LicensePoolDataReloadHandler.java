/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;

public class LicensePoolDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private LicenseDataService licenseDataService;

	public LicensePoolDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT);
		licenseDataService.retrieveLicensePoolsPD();
	}
}
