/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class StatisticsDataReloadHandler extends AbstractReloadHandler {
	private CacheManager cacheManager;

	public StatisticsDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.cacheManager = CacheManager.getInstance();
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS);
		dataServices.software.retrieveLicenseStatisticsPD();
	}
}
