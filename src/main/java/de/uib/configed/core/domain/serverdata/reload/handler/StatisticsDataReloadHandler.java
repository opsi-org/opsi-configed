/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class StatisticsDataReloadHandler extends AbstractReloadHandler {
	public StatisticsDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS);
		dataServices.software.retrieveLicenseStatisticsPD();
	}
}
