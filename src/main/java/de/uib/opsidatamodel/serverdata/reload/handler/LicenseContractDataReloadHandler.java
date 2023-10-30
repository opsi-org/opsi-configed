/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;

public class LicenseContractDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private LicenseDataService licenseDataService;

	public LicenseContractDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS);
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY);
		licenseDataService.retrieveLicenseContractsPD();
	}
}
