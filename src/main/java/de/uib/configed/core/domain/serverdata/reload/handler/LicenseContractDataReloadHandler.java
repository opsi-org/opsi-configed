/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicenseContractDataReloadHandler extends AbstractReloadHandler {
	private CacheManager cacheManager;

	public LicenseContractDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.cacheManager = CacheManager.getInstance();
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS);
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY);
		dataServices.license.retrieveLicenseContractsPD();
	}
}
