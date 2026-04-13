/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class RelationsASWToLPDataReloadHandler extends AbstractReloadHandler {
	public RelationsASWToLPDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL);
		dataServices.software.retrieveRelationsAuditSoftwareToLicensePoolsPD();
	}
}
