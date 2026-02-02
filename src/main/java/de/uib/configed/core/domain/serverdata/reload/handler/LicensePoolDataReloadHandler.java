/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicensePoolDataReloadHandler extends AbstractReloadHandler {
	public LicensePoolDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT);
		dataServices.license.retrieveLicensePoolsPD();
	}
}
