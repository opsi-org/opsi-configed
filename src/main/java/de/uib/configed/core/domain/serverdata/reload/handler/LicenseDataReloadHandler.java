/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import java.util.List;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicenseDataReloadHandler extends AbstractReloadHandler {
	public LicenseDataReloadHandler(DataServices dataServices) {
		super(dataServices);
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		dataServices.cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL);
		executor.runInParallel(dataServices.software::retrieveRelationsAuditSoftwareToLicensePoolsPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL);
		executor.runInParallel(dataServices.software::retrieveAuditSoftwareXLicensePoolPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSES);
		executor.runInParallel(dataServices.license::retrieveLicensesPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
		executor.runInParallel(dataServices.license::retrieveLicenseUsagesPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT);
		executor.runInParallel(dataServices.license::retrieveLicensePoolsPD);

		dataServices.cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		dataServices.cacheManager.clearCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL);
		dataServices.license.retrieveSoftwareLicense2LicensePoolPD();
		executor.runInParallel(dataServices.license::retrieveSoftwareLicense2LicensePoolPD);
		if (dataServices.cacheManager.isDataCached(
				List.of(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, CacheIdentifier.ROWS_LICENSES_STATISTICS))) {
			// This must be cleared so that the clients for the depots are updated
			dataServices.cacheManager.clearCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS);

			// Reload this to update the clients that we have to get audit data from (for statistics and reconciliation)
			dataServices.cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
			executor.runInParallel(dataServices.hostInfoCollections::retrieveOpsiHostsPD);

			dataServices.cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
			dataServices.cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS);
			executor.runInParallel(dataServices.software::retrieveLicenseStatisticsPD);
		}

		executor.waitForCompletion();
	}
}
