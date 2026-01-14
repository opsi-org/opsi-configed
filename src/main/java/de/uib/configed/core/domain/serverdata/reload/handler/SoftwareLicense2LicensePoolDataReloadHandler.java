/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class SoftwareLicense2LicensePoolDataReloadHandler extends AbstractReloadHandler {
	private CacheManager cacheManager;

	public SoftwareLicense2LicensePoolDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.cacheManager = CacheManager.getInstance();
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		cacheManager.clearCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL);
		dataServices.license.retrieveSoftwareLicense2LicensePoolPD();
	}
}
