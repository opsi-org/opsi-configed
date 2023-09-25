package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;

public class LicenseDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private LicenseDataService licenseDataService;
	private SoftwareDataService softwareDataService;
	private HostInfoCollections hostInfoCollections;

	public LicenseDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	public void setSoftwareDataService(SoftwareDataService softwareDataService) {
		this.softwareDataService = softwareDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
		licenseDataService.retrieveLicenceUsagesPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
		licenseDataService.retrieveLicencepoolsPD();

		cacheManager.clearCachedData(CacheIdentifier.LICENSE_USABILITIES);
		licenseDataService.retrieveLicenceUsabilitiesPD();

		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL);
		softwareDataService.retrieveRelationsAuditSoftwareToLicencePoolsPD();

		cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
		softwareDataService.retrieveLicenceStatisticsPD();

		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_LIST);
		cacheManager.clearCachedData(CacheIdentifier.NAME_TO_SW_IDENTS);
		cacheManager.clearCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING);
		cacheManager.clearCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO);
		softwareDataService.retrieveInstalledSoftwareInformationPD();

		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		hostInfoCollections.retrieveOpsiHostsPD();

		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		hostInfoCollections.retrieveFNode2TreeparentsPD();
	}

}
