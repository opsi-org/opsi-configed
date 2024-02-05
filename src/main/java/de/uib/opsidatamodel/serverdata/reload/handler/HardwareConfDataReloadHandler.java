/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HardwareDataService;

public class HardwareConfDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;
	private HardwareDataService hardwareDataService;

	public HardwareConfDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.HW_AUDIT_CONF);
		hardwareDataService.retrieveOpsiHWAuditConfPD();

		cacheManager.clearCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES);
		hardwareDataService.retrieveHwAuditDeviceClassesPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_COLUMN_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES);
		hardwareDataService.retrieveClient2HwRowsColumnNamesPD();

		cacheManager.clearCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES);
		hardwareDataService.retrieveHwClassesPD(hardwareDataService.getOpsiHWAuditConfPD());

		cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		configDataService.retrieveConfigOptionsPD();
	}
}
