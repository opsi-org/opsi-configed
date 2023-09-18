/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.opsidatamodel.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ReloadHandler;

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
	 * return type {@code Map<String, Map<String, String>>}
	 */
	PRODUCT_GROUPS("product_groups"),

	/**
	 * return type {@code Map<String, Map<String, String>>} or
	 * {@code HostGroups}
	 */
	HOST_GROUPS("host_groups"),

	/**
	 * return type {@code Map<String, Set<String>>}
	 */
	FOBJECT_TO_GROUPS("fObject_to_groups"),

	/**
	 * return type
	 * {@code Map<String, List<Map<String, List<Map<String, Object>>>>>}
	 */
	HW_AUDIT_CONF("hw_audit_conf"),

	/**
	 * return type {@code List<Map<String, Object>>}
	 */
	RELATIONS_AUDIT_HARDWARE_ON_HOST("relations_audit_hardware_on_host"),

	/**
	 * return type {@code List<String>}
	 */
	ALL_LOCALBOOT_PRODUCT_NAMES("all_localboot_product_names"),

	/**
	 * return type {@code List<String>}
	 */
	ALL_NETBOOT_PRODUCT_NAMES("all_netboot_product_names"),

	/**
	 * return type {@code Set<String>}
	 */
	PERMITTED_PRODUCTS("permitted_products"),

	/**
	 * return type {@code Map<String, OpsiHwAuditDeviceClass>}
	 */
	HW_AUDIT_DEVICE_CLASSES("hw_audit_device_classes"),

	/**
	 * return type {@code Map<String, ListCellOptions>}
	 */
	CONFIG_LIST_CELL_OPTIONS("config_list_cell_options"),

	/**
	 * return type {@code Map<String, ConfigOption>}
	 */
	CONFIG_OPTIONS("config_options"),

	/**
	 * return type {@code: Map<String, List<Object>>}
	 */
	CONFIG_DEFAULT_VALUES("config_default_values"),

	/**
	 * return type {@code Map<String, List<Object>>}
	 */
	WAN_CONFIGURATION("wan_configuration"),

	/**
	 * return type {@code Map<String, List<Object>>}
	 */
	NOT_WAN_CONFIGURATION("not_wan_configuration"),

	/**
	 * return type {@code Map<String, Object>}
	 */
	OPSI_LICENSING_INFO_NO_OPSI_ADMIN("opsi_licensing_info_no_opsi_admin"),

	/**
	 * return type {@code Map<String, Object>}
	 */
	OPSI_LICENSING_INFO_OPSI_ADMIN("opsi_licensing_info_opsi_admin"),

	/**
	 * return type {@code Map<String, List<String>>}
	 */
	MAP_OF_METHOD_SIGNATURES("map_of_method_signatures"),

	/**
	 * return type {@code List<String>}
	 */
	OPSI_HW_CLASS_NAMES("opsi_hw_class_names"),

	/**
	 * return type {@code Map<String, RemoteControl>} or {@code: RemoteControls}
	 */
	REMOTE_CONTROLS("remote_controls"),

	/**
	 * return type {@code: SavedSearches}
	 */
	SAVED_SEARCHES("saved_searches"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_LICENSE_MANAGEMENT("with_license_management"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_LOCAL_IMAGING("with_local_imaging"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_MY_SQL("with_my_sql"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_UEFI("with_uefi"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_WAN("with_wan"),

	/**
	 * return type {@code Boolean}
	 */
	WITH_USER_ROLES("with_user_roles"),

	/**
	 * return type {@code Boolean}
	 */
	ACCEPT_MY_SQL("accept_my_sql"),

	/**
	 * return type {@code Boolean}
	 */
	HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED("has_is_opsi_user_admin_been_checked"),

	/**
	 * return type {@code Boolean}
	 */
	IS_OPSI_ADMIN_USER("is_opsi_admin_user"),

	/**
	 * return type {@code Map<String, Boolean>}
	 */
	OPSI_MODULES("opsi_modules"),

	/**
	 * return type {@code Map<String, Object>}
	 */
	OPSI_INFORMATION("opsi_information"),

	/**
	 * return type {@code Map<String, Object>}
	 */
	OPSI_MODULES_DISPLAY_INFO("opsi_modules_display_info"),

	/**
	 * return type {@code Boolean}
	 */
	HAS_OPSI_LICENSING_BEEN_CHECKED("has_opsi_licensing_been_checked"),

	/**
	 * return type {@code Boolean}
	 */
	IS_OPSI_LICENSING_AVAILABLE("is_opsi_licensing_available"),

	/**
	 * retrung type {@code Map<String, TreeSet<OpsiPackage>}
	 */
	DEPOT_TO_PACKAGES("depot_to_packages"),

	/**
	 * return type {@code List<List<Object>>}
	 */
	PRODUCT_ROWS("product_rows"),

	/**
	 * return type {@code Map<String, Map<String, List<String>>}
	 */
	PRODUCT_TO_VERSION_INFO_TO_DEPOTS("product_to_version_info_to_depots"),

	/**
	 * return type {@code Object2Product2VersionList}
	 */
	DEPOT_TO_LOCALBOOT_PRODUCTS("depot_to_localboot_products"),

	/**
	 * return type {@code Object2Product2VersionList}
	 */
	DEPOT_TO_NETBOOT_PRODUCTS("depot_to_netboot_products"),

	/**
	 * return type {@code Map<String, Map<String, OpsiProductInfo>>}
	 */
	PRODUCT_TO_VERSION_INFO_TO_INFOS("product_to_version_info_to_infos"),

	/**
	 * return type {@code Map<String, Map<String, Object>>}
	 */
	CLIENT_TO_HW_ROWS("client_to_hw_rows"),

	/**
	 * return type {@code List<String>}
	 */
	CLIENT_TO_HW_ROWS_COLUMN_NAMES("client_to_hw_rows_column_names"),

	/**
	 * return type {@code List<String>}
	 */
	HW_INFO_CLASS_NAMES("hw_info_class_names"),

	/**
	 * return type {@code List<String>}
	 */
	CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES("client_to_hw_rows_java_class_names"),

	/**
	 * return type {@code List<String>}
	 */
	HOST_COLUMN_NAMES("host_column_names"),

	/**
	 * return type
	 * {@code Map<String, Map<String, Map<String, ListCellOptions>>>}
	 */
	DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS("depot_to_product_to_property_definitions"),

	/**
	 * return type
	 * {@code Map<String, Map<String, Map<String, ConfigName2ConfigValue>>>}
	 */
	DEPOT_TO_PRODUCT_TO_PROPERTIES("depot_to_product_to_properties"),

	/**
	 * return type {@code Map<String, Map<String, List<Map<String, String>>>>}
	 */
	DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS("depot_to_product_to_dependency_infos"),

	/**
	 * return type {@code List<Map<String, Object>>}
	 */
	PRODUCT_PROPERTY_STATES("product_property_states"),

	/**
	 * return type {@code List<Map<String, Object>>}
	 */
	PRODUCT_PROPERTY_DEPOT_STATES("product_property_depot_states"),

	/**
	 * return type {@code List<String>}
	 */
	SOFTWARE_LIST("software_list"),

	/**
	 * return type {@code NavigableMap<String, Integer>}
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
	 * return type {@code boolean}
	 */
	GLOBAL_READ_ONLY("global_read_only"),

	/**
	 * return type {@code boolean}
	 */
	SERVER_FULL_PERMISION("server_full_permission"),

	/**
	 * return type {@code boolean}
	 */
	DEPOTS_FULL_PERMISSION("depots_full_permission"),

	/**
	 * return type {@code boolean}
	 */
	HOST_GROUPS_ONLY_IF_EXPLICITLY_STATED("host_groups_only_if_explicitly_stated"),

	/**
	 * return type {@code boolean}
	 */
	PRODUCT_GROUPS_FULL_PERMISSION("product_groups_full_permission"),

	/**
	 * return type {@code boolean}
	 */
	CREATE_CLIENT_PERMISSION("create_client_permission"),

	/**
	 * return type {@code boolean}
	 */
	KEY_USER_REGISTER_VALUE("key_user_register_value"),

	/**
	 * return type {@code boolean}
	 */
	APPLY_USER_SPECIALIZED_CONFIG("apply_user_specialized_config"),

	/**
	 * return type {@code Set<String>}
	 */
	DEPOTS_PERMITTED("depots_permitted"),

	/**
	 * return type {@code Set<String>}
	 */
	HOST_GROUPS_PERMITTED("host_groups_permitted"),

	/**
	 * return type {@code String}
	 */
	USER_CONFIG_PART("user_config_part"),

	/**
	 * return type {@code Map<String, Set<String>>}
	 */
	FPRODUCT_GROUP_TO_MEMBERS("fproduct_group_to_members"),

	/**
	 * return type {@code String}
	 */
	OPSI_DEFAULT_DOMAIN("opsi_default_domain"),

	/**
	 * return type {@code Map<String, Strin>}
	 */
	LOG_FILES("log_files"),

	/**
	 * return type {@code Map<String, Map<String, Object>>}
	 */
	PRODUCT_GLOBAL_INFOS("product_global_infos"),

	/**
	 * return type {@code Map<String, List<String>>}
	 */
	POSSIBLE_ACTIONS("possible_actions"),

	/**
	 * return type {@code NavigableSet<Set>}
	 */
	PRODUCT_IDS("product_ids"),

	/**
	 * return type {@code Map<String, Map<String, String>>}
	 */
	PRODUCT_DEFAULT_STATES("product_default_states"),

	/**
	 * return type {@code Map<String, Map<String, ConfigName2ConfigValue>>}
	 */
	PRODUCT_PROPERTIES("product_properties"),

	/**
	 * return type {@code Map<String, Boolean>}
	 */
	PRODUCT_HAVING_CLIENT_SPECIFIC_PROPERTIES("product_having_client_specific_properties"),

	/**
	 * return type {@code Map<String, Map<String, ListCellOptions>>}
	 */
	PRODUCT_PROPERTY_DEFINITIOS("product_property_definitions"),

	/**
	 * return type {@code NavigableSet<Object>}
	 */
	SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL("software_without_associated_license_pool"),

	/**
	 * return type {@code Map<String, List<String>>}
	 */
	FLICENSE_POOL_TO_SOFTWARE_LIST("flicense_pool_to_software_list"),

	/**
	 * return type {@code Map<String, List<String>>}
	 */
	FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST("flicense_pool_to_unknonwn_software_list"),

	/**
	 * return type {@code Map<String, String>}
	 */
	FSOFTWARE_TO_LICENSE_POOL("fsoftware_to_license_pool"),

	/**
	 * return type {@code Map<String, Boolean>}
	 */
	HOST_DISPLAY_FIELDS("host_display_fields"),

	/**
	 * return type {@code Map<String, Boolean>}
	 */
	PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_NETBOOT_PRODUCTS("product_on_client_display_fields_netboot_products"),

	/**
	 * return type {@code Map<String, Boolean>}
	 */
	PRODUCT_ON_CLIENTS_DISPLAY_FIELDS_LOCALBOOT_PRODUCTS("product_on_client_display_fields_localboot_products"),

	/**
	 * return type {@code Map<String, LicenceUsageEntry>}
	 */
	ROWS_LICENSE_USAGE("rows_license_usage"),

	/**
	 * return type {@code Map<String, List<LicenceUsageEntry>>}
	 */
	FCLIENT_TO_LICENSES_USAGE_LIST("fclient_to_licenses_usage_list"),

	/**
	 * return type {@code Map<String, Set<String>>}
	 */
	FGROUP_TO_MEMBERS("fgroup_to_members"),

	/**
	 * return type {@code Map<String, Map<String, Object>>}
	 */
	ROWS_LICENSES_RECONCILIATION("rows_licenses_reconcilation"),

	/**
	 * return type {@code String}
	 */
	DEPOT("depot"),

	/**
	 * return type {@code String}
	 */
	CONFIGED_WORKBENCH_DEFAULT_VALUE("configed_workbench_default_value"),

	/**
	 * return type {@code String}
	 */
	PACKAGE_SERVER_DIRECTORY("package_server_directory"),

	/**
	 * return type {@code boolean}
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
