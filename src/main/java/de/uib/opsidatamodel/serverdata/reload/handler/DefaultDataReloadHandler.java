/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HardwareDataService;
import de.uib.opsidatamodel.serverdata.dataservice.LicenseDataService;
import de.uib.opsidatamodel.serverdata.dataservice.SoftwareDataService;

/**
 * Implementation of {@link ReloadHandler} which is responsible for reloading
 * one entry in the internal cache.
 * <p>
 * This {@link ReloadHandler} implementation is triggered by
 * {@link CacheIdentifier}. Not all {@link CacheIdentifier} have to be handled
 * in this {@link ReloadHandler}, only those that are required.
 */
public class DefaultDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;
	private HardwareDataService hardwareDataService;
	private GroupDataService groupDataService;
	private LicenseDataService licenseDataService;
	private SoftwareDataService softwareDataService;

	private Map<String, Consumer<Void>> eventHandlers;

	public DefaultDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
		this.eventHandlers = new HashMap<>();
		registerHandlers();
	}

	private void registerHandlers() {
		eventHandlers.put(CacheIdentifier.LICENSE_USAGE.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.LICENSE_USAGE);
			licenseDataService.retrieveLicenceUsagesPD();
		});
		eventHandlers.put(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST);
			hardwareDataService.retrieveHardwareOnClientPD();
		});
		eventHandlers.put(CacheIdentifier.FOBJECT_TO_GROUPS.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.FOBJECT_TO_GROUPS);
			groupDataService.retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId",
					CacheIdentifier.FGROUP_TO_MEMBERS);
		});
		eventHandlers.put(CacheIdentifier.HOST_GROUPS.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS);
			groupDataService.retrieveHostGroupsPD();
		});
		eventHandlers.put(CacheIdentifier.HOST_CONFIGS.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.HOST_CONFIGS);
			configDataService.retrieveHostConfigsPD();
		});
		eventHandlers.put(CacheIdentifier.PRODUCT_PROPERTIES.toString(),
				(Void v) -> cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTIES));
		eventHandlers.put(CacheIdentifier.ALL_DATA.toString(), (Void v) -> cacheManager.clearAllCachedData());
		eventHandlers.put(CacheIdentifier.LICENSES.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.LICENSES);
			licenseDataService.retrieveLicencesPD();
		});
		eventHandlers.put(CacheIdentifier.LICENSE_POOLS.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOLS);
			licenseDataService.retrieveLicencepoolsPD();
		});
		eventHandlers.put(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT);
			licenseDataService.retrieveLicencePoolXOpsiProductPD();
		});
		eventHandlers.put(CacheIdentifier.ROWS_LICENSES_RECONCILIATION.toString(), (Void v) -> {
			cacheManager.clearCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION);
			softwareDataService.retrieveLicenseStatisticsPD();
		});
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	public void setSoftwareDataService(SoftwareDataService softwareDataService) {
		this.softwareDataService = softwareDataService;
	}

	@Override
	public void handle(String event) {
		eventHandlers.get(event).accept(null);
	}
}
