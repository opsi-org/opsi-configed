/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.SoftwareDataService;

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
