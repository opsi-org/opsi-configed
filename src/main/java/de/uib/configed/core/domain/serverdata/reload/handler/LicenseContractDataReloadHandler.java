/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicenseContractDataReloadHandler extends AbstractReloadHandler {
	public LicenseContractDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY);
		dataServices.license.retrieveLicenseContractsPD();
	}
}
