/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class OpsiLicenseReloadHandler extends AbstractReloadHandler {
	public OpsiLicenseReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();

		cacheManager.clearCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN);
		executor.runInParallel(dataServices.module::retrieveOpsiLicensingInfoOpsiAdminPD);

		cacheManager.clearCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN);
		executor.runInParallel(dataServices.module::retrieveOpsiLicensingInfoNoOpsiAdminPD);

		executor.waitForCompletion();
	}
}
