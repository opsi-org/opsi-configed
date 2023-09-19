/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.HostDataService;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;

public class ReconciliationDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private SoftwareDataService softwareDataService;
	private LicenseDataService licenseDataService;
	private HostDataService hostDataService;

	public ReconciliationDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setSoftwareDataService(SoftwareDataService softwareDataService) {
		this.softwareDataService = softwareDataService;
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	public void setHostDataService(HostDataService hostDataService) {
		this.hostDataService = hostDataService;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
		softwareDataService.produceLicenceStatisticsPD();

		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		softwareDataService.retrieveInstalledSoftwareInformationPD();

		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_IDENT_TO_CLIENTS);

		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL);
		softwareDataService.retrieveRelationsAuditSoftwareToLicencePoolsPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		licenseDataService.retrieveLicencepoolsPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSES);
		licenseDataService.retrieveLicencesPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		licenseDataService.retrieveLicenceUsabilitiesPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
		licenseDataService.retrieveLicenceUsagesPD();

		hostDataService.getHostInfoCollectionsPD().opsiHostsRequestRefresh();
	}
}
