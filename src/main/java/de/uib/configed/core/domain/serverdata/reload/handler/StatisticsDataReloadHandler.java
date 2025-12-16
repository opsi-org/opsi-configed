/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.SoftwareDataService;

public class StatisticsDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private SoftwareDataService softwareDataService;

	public StatisticsDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setSoftwareDataService(SoftwareDataService softwareDataService) {
		this.softwareDataService = softwareDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS);
		softwareDataService.retrieveLicenseStatisticsPD();
	}
}
