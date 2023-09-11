/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

/**
 * Identifiers for internally cached data.
 * <p>
 * {@link CacheIdentifier} is also used in {@link DefaultDataReloadHandler}, for
 * reloading single data. Not all identifiers should have implementation for
 * realoding data, only those that are required. The reason is to reduce the
 * size of {@link DefaultDataReloadHandler} and for adherence to the principle
 * of YAGNI (You Aren't Gonna Need It). Also, the cached data is usually
 * reloaded using specific {@link ReloadHandler} implementations, that are
 * triggered by {@link ReloadEvent}.
 */
public enum CacheIdentifier {
	/**
	 * return type: {@code Map<String, Map<String, String>>}
	 */
	PRODUCT_GROUPS("product_groups"),

	/**
	 * return type: {@code Map<String, Map<String, String>>} or
	 * {@code HostGroups}
	 */
	HOST_GROUPS("host_groups"),

	/**
	 * return type: {@code Map<String, Set<String>>}
	 */
	FOBJECT_TO_GROUPS("fObject_to_groups"),

	/**
	 * return type:
	 * {@code Map<String, List<Map<String, List<Map<String, Object>>>>>}
	 */
	HW_AUDIT_CONF("hw_audit_conf"),

	/**
	 * return type: {@code List<Map<String, Object>>}
	 */
	RELATIONS_AUDIT_HARDWARE_ON_HOST("reloations_audit_hardware_on_host"),

	/**
	 * return type: {@code List<String>}
	 */
	ALL_LOCALBOOT_PRODUCT_NAMES("all_localboot_product_names"),

	/**
	 * return type: {@code List<String>}
	 */
	ALL_NETBOOT_PRODUCT_NAMES("all_netboot_product_names"),

	/**
	 * return type: {@code Set<String>}
	 */
	PERMITTED_PRODUCTS("permitted_products"),

	/**
	 * return type: {@code Map<String, OpsiHwAuditDeviceClass>}
	 */
	HW_AUDIT_DEVICE_CLASSES("hw_audit_device_classes"),

	/**
	 * return type: {@code Map<String, ListCellOptions>}
	 */
	CONFIG_LIST_CELL_OPTIONS("config_list_cell_options"),

	/**
	 * return type: {@code Map<String, ConfigOption>}
	 */
	CONFIG_OPTIONS("config_options"),

	/**
	 * return type: {@code: Map<String, List<Object>>}
	 */
	CONFIG_DEFAULT_VALUES("config_default_values"),

	/**
	 * return type: {@code Map<String, List<Object>>}
	 */
	WAN_CONFIGURATION("wan_configuration"),

	/**
	 * return type: {@code Map<String, List<Object>>}
	 */
	NOT_WAN_CONFIGURATION("not_wan_configuration"),

	/**
	 * return type: {@code Map<String, Object>}
	 */
	OPSI_LICENSING_INFO_NO_OPSI_ADMIN("opsi_licensing_info_no_opsi_admin"),

	/**
	 * return type: {@code Map<String, Object>}
	 */
	OPSI_LICENSING_INFO_OPSI_ADMIN("opsi_licensing_info_opsi_admin"),

	/**
	 * return type: {@code Map<String, List<String>>}
	 */
	MAP_OF_METHOD_SIGNATURES("map_of_method_signatures"),

	/**
	 * return type: {@code List<String>}
	 */
	OPSI_HW_CLASS_NAMES("opsi_hw_class_names"),

	/**
	 * return type: {@code Map<String, RemoteControl>} or
	 * {@code: RemoteControls}
	 */
	REMOTE_CONTROLS("remote_controls"),

	/**
	 * return type: {@code: SavedSearches}
	 */
	SAVED_SEARCHES("saved_searches"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_LICENSE_MANAGEMENT("with_license_management"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_LOCAL_IMAGING("with_local_imaging"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_MY_SQL("with_my_sql"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_UEFI("with_uefi"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_WAN("with_wan"),

	/**
	 * return type: {@code boolean}
	 */
	WITH_USER_ROLES("with_user_roles"),

	/**
	 * return type: {@code boolean}
	 */
	ACCEPT_MY_SQL("accept_my_sql"),

	/**
	 * return type: {@code boolean}
	 */
	HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED("has_is_opsi_user_admin_been_checked"),

	/**
	 * return type: {@code boolean}
	 */
	IS_OPSI_ADMIN_USER("is_opsi_admin_user"),

	/**
	 * return type: {@code LicensingInfoMap}
	 */
	LICENSING_INFO_MAP("licensing_info_map"),

	/**
	 * return type: {@code Map<String, Boolean>}
	 */
	OPSI_MODULES("opsi_modules"),

	/**
	 * return type: {@code Map<String, Object>}
	 */
	OPSI_INFORMATION("opsi_information"),

	/**
	 * return type: {@code Map<String, Object>}
	 */
	OPSI_MODULES_DISPLAY_INFO("opsi_modules_display_info"),

	/**
	 * return type: {@code Boolean}
	 */
	HAS_OPSI_LICENSING_BEEN_CHECKED("has_opsi_licensing_been_checked"),

	/**
	 * return type: {@code Boolean}
	 */
	IS_OPSI_LICENSING_AVAILABLE("is_opsi_licensing_available"),

	/**
	 * retrung type: {@code Map<String, TreeSet<OpsiPackage>}
	 */
	DEPOT_TO_PACKAGES("depot_to_packages"),

	/**
	 * return type: {@code List<List<Object>>}
	 */
	PRODUCT_ROWS("product_rows"),

	/**
	 * return type: {@code Map<String, Map<String, List<String>>}
	 */
	PRODUCT_TO_VERSION_INFO_TO_DEPOTS("product_to_version_info_to_depots"),

	/**
	 * return type: {@code Object2Product2VersionList}
	 */
	DEPOT_TO_LOCALBOOT_PRODUCTS("depot_to_localboot_products"),

	/**
	 * return type: {@code Object2Product2VersionList}
	 */
	DEPOT_TO_NETBOOT_PRODUCTS("depot_to_netboot_products"),

	/**
	 * return type: {@code Map<String, Map<String, OpsiProductInfo>>}
	 */
	PRODUCT_TO_VERION_INFO_TO_INFOS("product_to_version_info_to_infos"),

	/**
	 * return type: {@code Map<String, Map<String, Object>>}
	 */
	CLIENT_TO_HW_ROWS("client_to_hw_rows"),

	/**
	 * return type: {@code List<String>}
	 */
	CLIENT_TO_HW_ROWS_COLUMN_NAMES("client_to_hw_rows_column_names"),

	/**
	 * return type: {@code List<String>}
	 */
	HW_INFO_CLASS_NAMES("hw_info_class_names"),

	/**
	 * return type: {@code List<String>}
	 */
	CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES("client_to_hw_rows_java_class_names"),

	/**
	 * return type: {@code List<String>}
	 */
	HOST_COLUMN_NAMES("host_column_names"),

	/**
	 * return type:
	 * {@code Map<String, Map<String, Map<String, ListCellOptions>>>}
	 */
	DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS("depot_to_product_to_property_definitions"),

	/**
	 * return type: {@code Map<String, Map<String, List<Map<String, String>>>>}
	 */
	DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS("depot_to_product_to_dependency_infos"),

	/**
	 * return type: {@code List<Map<String, Object>>}
	 */
	PRODUCT_PROPERTY_STATES("product_property_states"),

	/**
	 * return type: {@code List<Map<String, Object>>}
	 */
	PRODUCT_PROPERTY_DEPOT_STATES("product_property_depot_states"),

	/**
	 * return type: {@code List<String>}
	 */
	SOFTWARE_LIST("software_list"),

	/**
	 * return type: {@code NavigableMap<String, Integer>}
	 */
	SOFTWARE_TO_NUMBER("software_to_number"),

	/**
	 * return type {@code NavigableMap<String, SWAuditEntry>}
	 */
	INSTALLED_SOFTWARE_INFORMATION("installed_software_information"),

	/**
	 * return type {@code NavigableMap<String, SWAuditEntry>}
	 */
	INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING("installed_software_information_for_licensing"),

	/**
	 * return type {@code NavigableMap<String, Map<String, String>>}
	 */
	INSTALLED_SOFTWARE_NAME_TO_SW_INFO("installed_software_name_to_sw_info"),

	/**
	 * return type {@code NavigableMap<String, Set<String>>}
	 */
	NAME_TO_SW_IDENTS("name_to_sw_idents"),

	/**
	 * return type {@code Map<String, List<SWAuditClientEntry>>}
	 */
	CLIENT_TO_SOFTWARE("client_to_software"),

	/**
	 * return type {@code Map<String, Set<String>>}
	 */
	SOFTWARE_IDENT_TO_CLIENTS("software_ident_to_clients"),

	/**
	 * return type {@code AuditSoftwareXLicencePool}
	 */
	AUDIT_SOFTWARE_XL_LICENSE_POOL("audit_software_xl_license_pool"),

	/**
	 * return type {@code Map<String, Map<String, Object>>}
	 */
	HOST_CONFIGS("host_configs"),

	/**
	 * return type {@code Map<String LicencepoolEntry>}
	 */
	LICENSE_POOLS("license_pools"),

	/**
	 * return type {@code Map<String, LicenceContractEntry>}
	 */
	LICENSE_CONTRACTS("license_contracts"),

	/**
	 * return type {@code NavigableMap<String, NavigableSet<String>>}
	 */
	LICENSE_CONTRACTS_TO_NOTIFY("license_contracts_to_notify"),

	/**
	 * return type {@code Map<String, LicenceEntry>}
	 */
	LICENSES("licenses"),

	/**
	 * return type {@code List<LicenceUsableForEntry>}
	 */
	LICENSE_USABILITIES("license_usabilites"),

	/**
	 * return type {@code List<LicenceUsageEntry>}
	 */
	LICENSE_USAGE("license_usage"),

	/**
	 * return type {@code LicencePoolXOpsiProduct}
	 */
	LICENSE_POOL_X_OPSI_PRODUCT("license_pool_x_opsi_product"),

	/**
	 * return type {@code List<Map<String, Object>>}
	 */
	HEALTH_CHECK_DATA("health_check_data"),

	/**
	 * return type {@code Map<String, Object>}
	 */
	DIAGNOSTIC_DATA("diagnostic_data"),

	/**
	 * return type {@code HostInfoCollections}
	 */
	HOST_INFO_COLLECTIONS("host_info_collections"),

	/**
	 * return type: {@code boolean}
	 */
	MFA_ENABLED("mfa_enabled");

	private final String displayName;

	CacheIdentifier(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
