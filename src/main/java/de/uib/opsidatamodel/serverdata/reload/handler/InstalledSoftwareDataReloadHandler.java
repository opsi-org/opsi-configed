/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;

public class InstalledSoftwareDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private SoftwareDataService softwareDataService;

	public InstalledSoftwareDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setSoftwareDataService(SoftwareDataService softwareDataService) {
		this.softwareDataService = softwareDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		softwareDataService.retrieveInstalledSoftwareInformationPD();
	}
}
