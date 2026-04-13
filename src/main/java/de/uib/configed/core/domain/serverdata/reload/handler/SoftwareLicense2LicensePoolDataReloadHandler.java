/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class SoftwareLicense2LicensePoolDataReloadHandler extends AbstractReloadHandler {
	public SoftwareLicense2LicensePoolDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL);
		dataServices.license.retrieveSoftwareLicense2LicensePoolPD();
	}
}
