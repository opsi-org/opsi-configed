/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import java.util.Arrays;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;

public class LicenseDataReloadHandler extends AbstractReloadHandler {
	private CacheManager cacheManager;

	public LicenseDataReloadHandler(DataServices dataServices) {
		super(dataServices);
		this.cacheManager = CacheManager.getInstance();
	}

	@Override
	public void handle(String event) {
		ParallelTaskExecutor executor = new ParallelTaskExecutor();
		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL);
		executor.runInParallel(dataServices.software::retrieveRelationsAuditSoftwareToLicensePoolsPD);

		cacheManager.clearCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL);
		executor.runInParallel(dataServices.software::retrieveAuditSoftwareXLicensePoolPD);

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS);
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY);
		cacheManager.clearCachedData(CacheIdentifier.LICENSES);
		executor.runInParallel(dataServices.license::retrieveLicensesPD);

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
		executor.runInParallel(dataServices.license::retrieveLicenseUsagesPD);

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT);
		executor.runInParallel(dataServices.license::retrieveLicensePoolsPD);

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		cacheManager.clearCachedData(CacheIdentifier.RELATIONS_SOFTWARE_L_TO_L_POOL);
		dataServices.license.retrieveSoftwareLicense2LicensePoolPD();
		executor.runInParallel(dataServices.license::retrieveSoftwareLicense2LicensePoolPD);
		if (cacheManager.isDataCached(Arrays.asList(CacheIdentifier.ROWS_LICENSES_RECONCILIATION,
				CacheIdentifier.ROWS_LICENSES_STATISTICS))) {
			// This must be cleared so that the clients for the depots are updated
			cacheManager.clearCachedData(CacheIdentifier.CLIENTS_FOR_DEPOTS);

			// Reload this to update the clients that we have to get audit data from (for statistics and reconciliation)
			cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
			executor.runInParallel(dataServices.hostInfoCollections::retrieveOpsiHostsPD);

			cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
			cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS);
			executor.runInParallel(dataServices.software::retrieveLicenseStatisticsPD);
		}

		executor.waitForCompletion();
	}
}
