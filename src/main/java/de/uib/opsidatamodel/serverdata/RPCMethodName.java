/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

/**
 * Enums representing used RPC methods to either retrieve data from the server
 * or to update data on the server.
 */
public enum RPCMethodName {
	CONFIG_GET_IDENTS("config_getIdents"), CONFIG_GET_OBJECTS("config_getObjects"),
	CONFIG_CREATE_OBJECTS("config_createObjects"), CONFIG_UPDATE_OBJECTS("config_updateObjects"),
	CONFIG_DELETE_OBJECTS("config_deleteObjects"),

	CONFIG_STATE_GET_VALUES("configState_getValues"), CONFIG_STATE_UPDATE_OBJECTS("configState_updateObjects"),
	CONFIG_STATE_DELETE_OBJECTS("configState_deleteObjects"),

	ACCESS_CONTROL_AUTHENTICATED("accessControl_authenticated"),
	ACCESS_CONTROL_USER_IS_ADMIN("accessControl_userIsAdmin"),
	ACCESS_CONTROL_USER_IS_READ_ONLY_USER("accessControl_userIsReadOnlyUser"),

	USER_GET_OBJECTS("user_getObjects"),

	HOST_GET_OBJECTS("host_getObjects"), HOST_CREATE_OBJECTS("host_createObjects"),
	HOST_UPDATE_OBJECTS("host_updateObjects"), HOST_DELETE("host_delete"),
	HOST_RENAME_OPSI_CLIENT("host_renameOpsiClient"),
	HOST_GET_MESSAGEBUS_CONNECTED_IDS("host_getMessagebusConnectedIds"),

	HOST_CONTROL_START("hostControl_start"), HOST_CONTROL_FIRE_EVENT("hostControl_fireEvent"),
	HOST_CONTROL_SHOW_POPUP("hostControl_showPopup"), HOST_CONTROL_SHUTDOWN("hostControl_shutdown"),
	HOST_CONTROL_REBOOT("hostControl_reboot"), HOST_CONTROL_REACHABLE("hostControl_reachable"),
	HOST_CONTROL_GET_ACTIVE_SESSIONS("hostControl_getActiveSessions"),
	HOST_CONTROL_PROCESS_ACTION_REQUESTS("hostControl_processActionRequests"),

	HOST_CONTROL_SAFE_REACHABLE("hostControlSafe_reachable"),
	HOST_CONTROL_SAFE_OPSICLIENTD_RPC("hostControlSafe_opsiclientdRpc"),

	PRODUCT_GET_OBJECTS("product_getObjects"),

	PRODUCT_ON_DEPOT_GET_IDENTS("productOnDepot_getIdents"), PRODUCT_ON_DEPOT_GET_OBJECTS("productOnDepot_getObjects"),

	PRODUCT_ON_CLIENT_GET_OBJECTS("productOnClient_getObjects"),
	PRODUCT_ON_CLIENT_GET_OBJECTS_WITH_SEQUENCE("productOnClient_getObjectsWithSequence"),
	PRODUCT_ON_CLIENT_CREATE_OBJECTS("productOnClient_createObjects"),
	PRODUCT_ON_CLIENT_UPDATE_OBJECTS("productOnClient_updateObjects"),
	PRODUCT_ON_CLIENT_DELETE_OBJECTS("productOnClient_deleteObjects"),

	PRODUCT_DEPENDENCY_GET_OBJECTS("productDependency_getObjects"),

	PRODUCT_PROPERTY_GET_OBJECTS("productProperty_getObjects"),

	PRODUCT_PROPERTY_STATE_GET_OBJECTS("productPropertyState_getObjects"),
	PRODUCT_PROPERTY_STATE_UPDATE_OBJECTS("productPropertyState_updateObjects"),
	PRODUCT_PROPERTY_STATE_DELETE_OBJECTS("productPropertyState_deleteObjects"),
	PRODUCT_PROPERTY_STATE_DELETE("productPropertyState_delete"),

	OBJECT_TO_GROUP_GET_OBJECTS("objectToGroup_getObjects"),
	OBJECT_TO_GROUP_CREATE_OBJECTS("objectToGroup_createObjects"),
	OBJECT_TO_GROUP_DELETE_OBJECTS("objectToGroup_deleteObjects"), OBJECT_TO_GROUP_CREATE("objectToGroup_create"),
	OBJECT_TO_GROUP_DELETE("objectToGroup_delete"),

	GROUP_GET_OBJECTS("group_getObjects"), GROUP_CREATE_OBJECTS("group_createObjects"),
	GROUP_UPDATE_OBJECT("group_updateObject"), GROUP_DELETE("group_delete"),

	AUDIT_HARDWARE_GET_CONFIG("auditHardware_getConfig"),

	AUDIT_HARDWARE_ON_HOST_GET_OBJECTS("auditHardwareOnHost_getObjects"),

	AUDIT_SOFTWARE_GET_OBJECTS("auditSoftware_getObjects"),

	AUDIT_SOFTWARE_ON_CLIENT_GET_OBJECTS("auditSoftwareOnClient_getObjects"),

	AUDIT_SOFTWARE_TO_LICENSE_POOL_GET_OBJECTS("auditSoftwareToLicensePool_getObjects"),
	AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS("auditSoftwareToLicensePool_createObjects"),
	AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS("auditSoftwareToLicensePool_deleteObjects"),

	BACKEND_GET_INTERFACE("backend_getInterface"), GET_BACKEND_INFOS_LIST_OF_HASHES("getBackendInfos_listOfHashes"),
	BACKEND_GET_LICENSING_INFO("backend_getLicensingInfo"),

	LICENSE_CONTRACT_CREATE("licenseContract_create"), LICENSE_CONTRACT_DELETE("licenseContract_delete"),
	LICENSE_CONTRACT_GET_OBJECTS("licenseContract_getObjects"),

	LICENSE_POOL_CREATE("licensePool_create"), LICENSE_POOL_DELETE("licensePool_delete"),
	LICENSE_POOL_GET_OBJECTS("licensePool_getObjects"), LICENSE_POOL_UPDATE_OBJECT("licensePool_updateObject"),

	SOFTWARE_LICENSE_DELETE("softwareLicense_delete"), SOFTWARE_LICENSE_GET_OBJECTS("softwareLicense_getObjects"),
	SOFTWARE_LICENSE_CREATE_OEM("softwareLicense_createOEM"),
	SOFTWARE_LICENSE_CREATE_RETAIL("softwareLicense_createRetail"),
	SOFTWARE_LICENSE_CREATE_VOLUME("softwareLicense_createVolume"),
	SOFTWARE_LICENSE_CREATE_CONCURRENT("softwareLicense_createConcurrent"),

	SOFTWARE_LICENSE_TO_LICENSE_POOL_CREATE("softwareLicenseToLicensePool_create"),
	SOFTWARE_LICENSE_TO_LICENSE_POOL_DELETE("softwareLicenseToLicensePool_delete"),
	SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS("softwareLicenseToLicensePool_getObjects"),

	LICENSE_ON_CLIENT_CREATE("licenseOnClient_create"), LICENSE_ON_CLIENT_DELETE("licenseOnClient_delete"),
	LICENSE_ON_CLIENT_GET_OBJECTS("licenseOnClient_getObjects"),
	LICENSE_ON_CLIENT_GET_OR_CREATE_OBJECT("licenseOnClient_getOrCreateObject"),
	LICENSE_ON_CLIENT_DELETE_OBJECTS("licenseOnClient_deleteObjects"),

	SSH_COMMAND_GET_OBJECTS("SSHCommand_getObjects"), SSH_COMMAND_CREATE_OBJECTS("SSHCommand_createObjects"),
	SSH_COMMAND_UPDATE_OBJECTS("SSHCommand_updateObjects"), SSH_COMMAND_DELETE_OBJECTS("SSHCommand_deleteObjects"),

	LOG_READ("log_read"),

	GET_OPSI_CA_CERT("getOpsiCACert"),

	GET_PRODUCT_ORDERING("getProductOrdering"),

	GET_DOMAIN("getDomain"),

	DEPOT_INSTALL_PACKAGE("depot_installPackage"),

	SET_RIGHTS("setRights"),

	SERVICE_HEALTH_CHECK("service_healthCheck"), SERVICE_GET_DIAGNOSTIC_DATA("service_getDiagnosticData"),

	/**
	 * This enum only exists for testing purposes.
	 */
	NON_EXISTING_METHOD("non_existing_method");

	private final String displayName;

	RPCMethodName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
