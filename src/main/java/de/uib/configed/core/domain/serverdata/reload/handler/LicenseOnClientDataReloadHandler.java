/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicenseOnClientDataReloadHandler extends AbstractReloadHandler {
	public LicenseOnClientDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSE_USAGE);
		cacheManager.clearCachedData(CacheIdentifier.FCLIENT_TO_LICENSES_USAGE_LIST);
		dataServices.license.retrieveLicensesUsagePD();
	}
}
