/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class InstalledSoftwareDataReloadHandler extends AbstractReloadHandler {
	public InstalledSoftwareDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		dataServices.cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		dataServices.software.retrieveInstalledSoftwareInformationPD();
	}
}
