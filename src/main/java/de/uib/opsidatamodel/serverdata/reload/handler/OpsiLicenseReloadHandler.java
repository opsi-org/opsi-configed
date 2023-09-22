/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ModuleDataService;

public class OpsiLicenseReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ModuleDataService moduleDataService;

	public OpsiLicenseReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN);
		moduleDataService.retrieveOpsiLicensingInfoOpsiAdminPD();

		cacheManager.clearCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN);
		moduleDataService.retrieveOpsiLicensingInfoNoOpsiAdminPD();
	}
}
