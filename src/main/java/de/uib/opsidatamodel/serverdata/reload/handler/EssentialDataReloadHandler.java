/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.configed.type.Object2GroupEntry;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.DepotDataService;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HardwareDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HostDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ModuleDataService;
import de.uib.opsidatamodel.serverdata.dataservice.ProductDataService;
import de.uib.opsidatamodel.serverdata.dataservice.UserRolesConfigDataService;

/**
 * Implementation of {@link ReloadHandler} which is responsible for reloading
 * essential data in the internal cache.
 * <p>
 * Essential data is data that is required to run the configed or required by
 * sub-data and cannot be lazily loaded.
 */
public class EssentialDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private ProductDataService productDataService;
	private DepotDataService depotDataService;
	private GroupDataService groupDataService;
	private ModuleDataService moduleDataService;
	private HardwareDataService hardwareDataService;
	private HostDataService hostDataService;
	private HostInfoCollections hostInfoCollections;

	public EssentialDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setProductDataService(ProductDataService productDataService) {
		this.productDataService = productDataService;
	}

	public void setDepotDataService(DepotDataService depotDataService) {
		this.depotDataService = depotDataService;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public void setHardwareDataService(HardwareDataService hardwareDataService) {
		this.hardwareDataService = hardwareDataService;
	}

	public void setHostDataService(HostDataService hostDataService) {
		this.hostDataService = hostDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.OPSI_MODULES);
		cacheManager.clearCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO);
		cacheManager.clearCachedData(CacheIdentifier.OPSI_INFORMATION);
		cacheManager.clearCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT);
		cacheManager.clearCachedData(CacheIdentifier.WITH_LOCAL_IMAGING);
		cacheManager.clearCachedData(CacheIdentifier.WITH_MY_SQL);
		cacheManager.clearCachedData(CacheIdentifier.WITH_UEFI);
		cacheManager.clearCachedData(CacheIdentifier.WITH_USER_ROLES);
		cacheManager.clearCachedData(CacheIdentifier.WITH_WAN);
		moduleDataService.retrieveOpsiModules();

		cacheManager.clearCachedData(CacheIdentifier.GLOBAL_READ_ONLY);
		cacheManager.clearCachedData(CacheIdentifier.SERVER_FULL_PERMISION);
		cacheManager.clearCachedData(CacheIdentifier.DEPOTS_FULL_PERMISSION);
		cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GROUPS_FULL_PERMISSION);
		cacheManager.clearCachedData(CacheIdentifier.CREATE_CLIENT_PERMISSION);
		cacheManager.clearCachedData(CacheIdentifier.KEY_USER_REGISTER_VALUE);
		userRolesConfigDataService.checkConfigurationPD();

		cacheManager.clearCachedData(CacheIdentifier.HW_AUDIT_CONF);
		cacheManager.clearCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES);
		hardwareDataService.retrieveHwClassesPD(hardwareDataService
				.getOpsiHWAuditConfPD(Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry()));

		cacheManager.clearCachedData(CacheIdentifier.REMOTE_CONTROLS);
		cacheManager.clearCachedData(CacheIdentifier.SAVED_SEARCHES);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_OPTIONS);
		cacheManager.clearCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES);
		configDataService.retrieveConfigOptionsPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GROUPS);
		groupDataService.retrieveProductGroupsPD();

		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		hostInfoCollections.retrieveOpsiHostsPD();

		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		hostInfoCollections.retrieveFNode2TreeparentsPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS);
		configDataService.retrieveHostConfigsPD();

		cacheManager.clearCachedData(CacheIdentifier.FOBJECT_TO_GROUPS);
		groupDataService.retrieveFObject2GroupsPD();

		cacheManager.clearCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS);
		groupDataService.retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId",
				CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS);

		cacheManager.clearCachedData(CacheIdentifier.OPSI_DEFAULT_DOMAIN);
		configDataService.retrieveOpsiDefaultDomainPD();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_GLOBAL_INFOS);
		cacheManager.clearCachedData(CacheIdentifier.POSSIBLE_ACTIONS);
		productDataService.checkProductGlobalInfosPD(depotDataService.getDepot());

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_IDS);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_PROPERTIES);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_DEFAULT_STATES);
		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_INFOS);
		productDataService.retrieveProductIdsAndDefaultStatesPD();

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS);
		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS);
		productDataService.retrieveProductsAllDepotsPD();

		cacheManager.clearCachedData(CacheIdentifier.ALL_LOCALBOOT_PRODUCT_NAMES);
		cacheManager.clearCachedData(CacheIdentifier.ALL_NETBOOT_PRODUCT_NAMES);
		depotDataService.retrieveProductsPD();

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS);
		productDataService.retrieveAllProductPropertyDefinitionsPD();

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTIES);
		productDataService.retrieveDepotProductPropertiesPD();

		cacheManager.clearCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS);
		productDataService.retrieveAllProductDependenciesPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_DISPLAY_FIELDS);
		hostDataService.retrieveHostDisplayFields();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_LOCALBOOT_PRODUCTS);
		productDataService.retrieveProductOnClientsDisplayFieldsLocalbootProducts();

		cacheManager.clearCachedData(CacheIdentifier.PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_NETBOOT_PRODUCTS);
		productDataService.retrieveProductOnClientsDisplayFieldsNetbootProducts();

		cacheManager.clearCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST);
		hardwareDataService.retrieveHardwareOnClientPD();

		// Request health data to be reloaded later on - lazy initialization.
		cacheManager.clearCachedData(CacheIdentifier.HEALTH_CHECK_DATA);
		cacheManager.clearCachedData(CacheIdentifier.DIAGNOSTIC_DATA);
	}
}
