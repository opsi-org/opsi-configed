/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.HardwareDataService;

public class ClientHardwareDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private HardwareDataService hardwareDataService;

	public ClientHardwareDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.HOST_COLUMN_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES);
		hardwareDataService.retrieveClient2HwRowsColumnNamesPD();

		cacheManager.clearCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS);
	}
}
