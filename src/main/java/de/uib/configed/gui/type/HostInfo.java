/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ClientTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.share.logging.Logging;

public class HostInfo {
	public static final String DEPOT_OF_CLIENT_KEY = "depotId";
	public static final String CLIENT_DESCRIPTION_KEY = "description";
	public static final String CLIENT_INVENTORY_NUMBER_KEY = "inventoryNumber";
	public static final String CLIENT_ONE_TIME_PASSWORD_KEY = "oneTimePassword";
	public static final String CLIENT_NOTES_KEY = "notes";
	public static final String CLIENT_SYSTEM_UUID_KEY = "systemUUID";
	public static final String CLIENT_MAC_ADRESS_KEY = "hardwareAddress";
	public static final String LAST_SEEN_KEY = "lastSeen";
	public static final String CREATED_KEY = "created";
	public static final String HOSTNAME_KEY = "id";
	public static final String HOST_KEY_KEY = "opsiHostKey";
	public static final String HOST_TYPE_KEY = "type";
	public static final String CLIENT_IP_ADDRESS_KEY = "ipAddress";
	public static final String CLIENT_WAN_CONFIG_KEY = "wan_vpn";
	public static final String CLIENT_SHUTDOWN_INSTALL_KEY = "install_on_shutdown";
	public static final String DEPOT_WORKBENCH_KEY = "workbenchLocalUrl";
	public static final String DEPOT_WEBDAV_URL = "depotWebdavUrl";
	public static final String CLIENT_OS_KEY = "operating_system";
	public static final String CLIENT_OS_TYPE_KEY = "operating_system_type";
	public static final String CLIENT_OS_ARCHITECTURE_KEY = "operating_system_architecture";
	public static final String CLIENT_DEVICE_TYPE_KEY = "device_type";
	public static final String CLIENT_DEVICE_VENDOR_KEY = "device_vendor";
	public static final String CLIENT_DEVICE_MODEL_KEY = "device_model";
	public static final String UEFI_BOOT_KEY = "uefi_boot";
	public static final String CLIENT_MONITORING_KEY = "monitoring";

	public static final String DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL = "depotId";
	public static final String CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL = "clientDescription";
	public static final String CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL = "clientInventoryNumber";

	public static final String CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL = "clientSystemUUID";
	public static final String CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL = "clientHardwareAddress";
	public static final String LAST_SEEN_DISPLAY_FIELD_LABEL = "clientLastSeen";
	public static final String CREATED_DISPLAY_FIELD_LABEL = "clientCreated";
	public static final String HOST_NAME_DISPLAY_FIELD_LABEL = "clientName";

	public static final String CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL = "clientIPAddress";
	public static final String CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL = "WANmode";
	public static final String CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL = "clientSessionInfo";

	public static final String CLIENT_CONNECTED_DISPLAY_FIELD_LABEL = "clientConnected";
	public static final String CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL = "installByShutdown";
	public static final String CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL = "healthCheckActive";

	public static final String CLIENT_OS_DISPLAY_FIELD_LABEL = "operatingSystem";
	public static final String CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL = "operatingSystemType";
	public static final String CLIENT_OS_ARCHITECTURE_DISPLAY_FIELD_LABEL = "operatingSystemArchitecture";
	public static final String CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL = "deviceType";
	public static final String CLIENT_DEVICE_VENDOR_DISPLAY_FIELD_LABEL = "deviceVendor";
	public static final String CLIENT_DEVICE_MODEL_DISPLAY_FIELD_LABEL = "deviceModel";

	public static class ColumnDisplayInfo {
		public final String label;
		public final String resourceKey;

		public ColumnDisplayInfo(String label, String resourceKey) {
			this.label = label;
			this.resourceKey = resourceKey;
		}

		// When we have no resource key it means, we won't show it in the options
		// because it will always be shown
		public ColumnDisplayInfo(String label) {
			this.label = label;
			this.resourceKey = null;
		}
	}

	public static final List<ColumnDisplayInfo> ORDERED_DISPLAY_COLUMN_INFOS = List.of(
			new ColumnDisplayInfo(HostInfo.CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.operatingSystemType"),
			new ColumnDisplayInfo(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL),
			new ColumnDisplayInfo(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.clientDescription"),
			new ColumnDisplayInfo(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL,
					"MainFrame.jMenuShowInventoryNumberColumn"),
			new ColumnDisplayInfo(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.clientConnected"),
			new ColumnDisplayInfo(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.clientLastSeen"),
			new ColumnDisplayInfo(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, "MainFrame.jMenuShowWanConfig"),
			new ColumnDisplayInfo(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, "ipAddress"),
			new ColumnDisplayInfo(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL,
					"MainFrame.jMenuShowSystemUUIDColumn"),
			new ColumnDisplayInfo(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL,
					"MainFrame.jMenuShowHardwareAddressColumn"),
			new ColumnDisplayInfo(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL,
					"MainFrame.jMenuShowInstallByShutdown"),
			new ColumnDisplayInfo(HostInfo.CREATED_DISPLAY_FIELD_LABEL, "MainFrame.jMenuShowCreatedColumn"),
			new ColumnDisplayInfo(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, "sessionInfo"),
			new ColumnDisplayInfo(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, "depot"),
			new ColumnDisplayInfo(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.healthCheckActive"),
			new ColumnDisplayInfo(HostInfo.CLIENT_OS_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.operatingSystem"),
			new ColumnDisplayInfo(HostInfo.CLIENT_OS_ARCHITECTURE_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.operatingSystemArchitecture"),
			new ColumnDisplayInfo(HostInfo.CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.deviceType"),
			new ColumnDisplayInfo(HostInfo.CLIENT_DEVICE_VENDOR_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.deviceVendor"),
			new ColumnDisplayInfo(HostInfo.CLIENT_DEVICE_MODEL_DISPLAY_FIELD_LABEL,
					"ConfigedMain.pclistTableModel.deviceModel"));

	public static final String IS_MASTER_DEPOT_KEY = "isMasterDepot";

	public static final String HOST_TYPE_VALUE_OPSI_CONFIG_SERVER = "OpsiConfigserver";
	public static final String HOST_TYPE_VALUE_OPSI_DEPOT_SERVER = "OpsiDepotserver";
	public static final String HOST_TYPE_VALUE_OPSI_CLIENT = "OpsiClient";

	private String depotOfClient;
	private String clientDescription;
	private String clientInventoryNumber;
	private String clientOneTimePassword;
	private String clientNotes;

	private String clientSystemUUID;
	private String clientMacAddress;
	private String lastSeen;
	private String created;
	private String clientName;
	private String hostKey;

	private String hostType;
	private String clientIpAddress;
	private Boolean clientWanConfig;

	private Boolean clientShutdownInstall;

	private String clientOS;
	private String clientOSType;
	private String clientOSArchitecture;
	private String clientDeviceType;
	private String clientDeviceVendor;
	private String clientDeviceModel;
	private Boolean clientMonitoring;
	private Boolean uefiBoot;

	public Map<String, Object> getDisplayRowMap() {
		Map<String, Object> displayRowMap = new HashMap<>();

		displayRowMap.put(CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL, clientOSType);
		displayRowMap.put(HOST_NAME_DISPLAY_FIELD_LABEL, clientName);
		displayRowMap.put(CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL, clientDescription);
		displayRowMap.put(CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL, clientInventoryNumber);
		displayRowMap.put(LAST_SEEN_DISPLAY_FIELD_LABEL, lastSeen);

		displayRowMap.put(CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, clientWanConfig);
		displayRowMap.put(CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, clientIpAddress);
		displayRowMap.put(CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL, clientSystemUUID);
		displayRowMap.put(CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, clientMacAddress);
		displayRowMap.put(CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, clientShutdownInstall);

		displayRowMap.put(CREATED_DISPLAY_FIELD_LABEL, created);
		displayRowMap.put(DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, depotOfClient);

		displayRowMap.put(CLIENT_OS_DISPLAY_FIELD_LABEL, clientOS);
		displayRowMap.put(CLIENT_OS_ARCHITECTURE_DISPLAY_FIELD_LABEL, clientOSArchitecture);
		displayRowMap.put(CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL, clientDeviceType);
		displayRowMap.put(CLIENT_DEVICE_VENDOR_DISPLAY_FIELD_LABEL, clientDeviceVendor);
		displayRowMap.put(CLIENT_DEVICE_MODEL_DISPLAY_FIELD_LABEL, clientDeviceModel);
		displayRowMap.put(CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL, clientMonitoring);

		Logging.debug(this, "getMap clientName ", clientName, " : ", displayRowMap);

		return displayRowMap;
	}

	public Map<String, Object> getMap() {
		Map<String, Object> unordered = new HashMap<>();

		unordered.put(DEPOT_OF_CLIENT_KEY, depotOfClient);
		unordered.put(CLIENT_DESCRIPTION_KEY, clientDescription);
		unordered.put(CLIENT_INVENTORY_NUMBER_KEY, clientInventoryNumber);
		unordered.put(CLIENT_ONE_TIME_PASSWORD_KEY, clientOneTimePassword);
		unordered.put(CLIENT_NOTES_KEY, clientNotes);

		unordered.put(CLIENT_SYSTEM_UUID_KEY, clientSystemUUID);
		unordered.put(CLIENT_MAC_ADRESS_KEY, clientMacAddress);
		unordered.put(LAST_SEEN_KEY, lastSeen);
		unordered.put(CREATED_KEY, created);
		unordered.put(HOSTNAME_KEY, clientName);
		unordered.put(HOST_KEY_KEY, hostKey);

		unordered.put(HOST_TYPE_KEY, hostType);
		unordered.put(CLIENT_IP_ADDRESS_KEY, clientIpAddress);
		unordered.put(CLIENT_WAN_CONFIG_KEY, clientWanConfig);

		unordered.put(CLIENT_SHUTDOWN_INSTALL_KEY, clientShutdownInstall);

		unordered.put(CLIENT_OS_KEY, clientOS);
		unordered.put(CLIENT_OS_TYPE_KEY, clientOSType);
		unordered.put(CLIENT_OS_ARCHITECTURE_KEY, clientOSArchitecture);
		unordered.put(CLIENT_DEVICE_TYPE_KEY, clientDeviceType);
		unordered.put(CLIENT_DEVICE_VENDOR_KEY, clientDeviceVendor);
		unordered.put(CLIENT_DEVICE_MODEL_KEY, clientDeviceModel);
		unordered.put(CLIENT_MONITORING_KEY, clientMonitoring);
		unordered.put(UEFI_BOOT_KEY, uefiBoot);

		Logging.debug(this, "getMap clientName ", clientName);

		return unordered;
	}

	public static Set<String> getKeysForCSV() {
		Set<String> keys = new LinkedHashSet<>();
		keys.add(HOSTNAME_KEY);
		keys.add("domain");
		keys.add(DEPOT_OF_CLIENT_KEY);
		keys.add(CLIENT_MAC_ADRESS_KEY);
		keys.add(CLIENT_DESCRIPTION_KEY);
		keys.add(CLIENT_INVENTORY_NUMBER_KEY);
		keys.add(CLIENT_NOTES_KEY);
		keys.add(CLIENT_SYSTEM_UUID_KEY);
		keys.add(CLIENT_IP_ADDRESS_KEY);
		keys.add("groups");
		keys.add(CLIENT_WAN_CONFIG_KEY);
		keys.add(CLIENT_SHUTDOWN_INSTALL_KEY);
		keys.add(HOST_KEY_KEY);
		return Collections.unmodifiableSet(keys);
	}

	public void put(String key, Object value) {
		switch (key) {
		case DEPOT_OF_CLIENT_KEY:
			depotOfClient = "" + value;
			break;

		case CLIENT_DESCRIPTION_KEY:
			clientDescription = "" + value;
			break;

		case CLIENT_INVENTORY_NUMBER_KEY:
			clientInventoryNumber = "" + value;
			break;

		case CLIENT_NOTES_KEY:
			clientNotes = "" + value;
			break;

		case CLIENT_ONE_TIME_PASSWORD_KEY:
			clientOneTimePassword = "" + value;
			break;

		case CLIENT_SYSTEM_UUID_KEY:
			clientSystemUUID = "" + value;
			break;

		case CLIENT_MAC_ADRESS_KEY:
			clientMacAddress = "" + value;
			break;

		case CLIENT_IP_ADDRESS_KEY:
			clientIpAddress = "" + value;
			break;

		case HOST_KEY_KEY:
			hostKey = "" + value;
			break;

		case CREATED_KEY:
			created = "" + value;
			break;

		case LAST_SEEN_KEY:
			lastSeen = "" + value;
			break;

		case CLIENT_WAN_CONFIG_KEY:
			clientWanConfig = (Boolean) value;
			break;

		case CLIENT_SHUTDOWN_INSTALL_KEY:
			clientShutdownInstall = (Boolean) value;
			break;

		case CLIENT_OS_KEY:
			clientOS = "" + value;
			break;

		case CLIENT_OS_TYPE_KEY:
			clientOSType = "" + value;
			break;

		case CLIENT_OS_ARCHITECTURE_KEY:
			clientOSArchitecture = "" + value;
			break;

		case CLIENT_DEVICE_TYPE_KEY:
			clientDeviceType = "" + value;
			break;

		case CLIENT_DEVICE_VENDOR_KEY:
			clientDeviceVendor = "" + value;
			break;

		case CLIENT_DEVICE_MODEL_KEY:
			clientDeviceModel = "" + value;
			break;

		case CLIENT_MONITORING_KEY:
			clientMonitoring = (Boolean) value;
			break;

		case UEFI_BOOT_KEY:
			uefiBoot = (Boolean) value;
			break;

		default:
			Logging.warning(this, "key ", key, " not expected");
			break;
		}
	}

	public String getInDepot() {
		return depotOfClient;
	}

	public void setInDepot(String depot) {
		depotOfClient = depot;
	}

	public String getDescription() {
		return clientDescription;
	}

	public String getInventoryNumber() {
		return clientInventoryNumber;
	}

	public String getMacAddress() {
		return clientMacAddress;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public String getName() {
		return clientName;
	}

	public String getHostKey() {
		return hostKey;
	}

	public String getIpAddress() {
		return clientIpAddress;
	}

	public Boolean getWanConfig() {
		return clientWanConfig;
	}

	public Boolean getShutdownInstall() {
		return clientShutdownInstall;
	}

	public String getClientOS() {
		return clientOS;
	}

	public String getClientOSType() {
		return clientOSType;
	}

	public String getClientDeviceType() {
		return clientDeviceType;
	}

	public String getClientDeviceVendor() {
		return clientDeviceVendor;
	}

	public String getClientDeviceModel() {
		return clientDeviceModel;
	}

	public void setShutdownInstall(boolean b) {
		clientShutdownInstall = b;
	}

	public void setWanConfig(boolean b) {
		clientWanConfig = b;
	}

	public void setType(String type) {
		hostType = type;
	}

	private static String showValue(String value) {
		if (value == null || "null".equals(value)) {
			return "";
		} else {
			return value;
		}
	}

	private static boolean showValue(Boolean value) {
		if (value == null) {
			return false;
		} else {
			return value;
		}
	}

	public void setValues(Map<String, Object> pcInfo) {
		// shows pckey

		if (pcInfo == null) {
			resetValues();
			return;
		}

		// encodeStringFromService is just returning the given value but was used for
		// switching an encoding
		clientDescription = showValue((String) pcInfo.get(CLIENT_DESCRIPTION_KEY));
		clientInventoryNumber = showValue((String) pcInfo.get(CLIENT_INVENTORY_NUMBER_KEY));
		clientNotes = showValue((String) pcInfo.get(CLIENT_NOTES_KEY));
		clientOneTimePassword = showValue((String) pcInfo.get(CLIENT_ONE_TIME_PASSWORD_KEY));
		clientSystemUUID = showValue((String) pcInfo.get(CLIENT_SYSTEM_UUID_KEY));
		clientMacAddress = showValue((String) pcInfo.get(CLIENT_MAC_ADRESS_KEY));
		clientIpAddress = showValue((String) pcInfo.get(CLIENT_IP_ADDRESS_KEY));
		hostKey = showValue((String) pcInfo.get(HOST_KEY_KEY));
		clientName = showValue((String) pcInfo.get(HOSTNAME_KEY));
		hostType = showValue((String) pcInfo.get(HOST_TYPE_KEY));
		created = showValue((String) pcInfo.get(CREATED_KEY));
		lastSeen = showValue((String) pcInfo.get(LAST_SEEN_KEY));
		clientOS = showValue((String) pcInfo.get(CLIENT_OS_KEY));
		clientOSType = showValue((String) pcInfo.get(CLIENT_OS_TYPE_KEY));
		clientOSArchitecture = showValue((String) pcInfo.get(CLIENT_OS_ARCHITECTURE_KEY));
		clientDeviceType = showValue((String) pcInfo.get(CLIENT_DEVICE_TYPE_KEY));
		clientDeviceVendor = showValue((String) pcInfo.get(CLIENT_DEVICE_VENDOR_KEY));
		clientDeviceModel = showValue((String) pcInfo.get(CLIENT_DEVICE_MODEL_KEY));
		clientMonitoring = showValue((Boolean) pcInfo.get(CLIENT_MONITORING_KEY));
		uefiBoot = showValue((Boolean) pcInfo.get(UEFI_BOOT_KEY));

		depotOfClient = showValue((String) pcInfo.get(DEPOT_OF_CLIENT_KEY));

		clientWanConfig = showValue((Boolean) pcInfo.get(CLIENT_WAN_CONFIG_KEY));

		clientShutdownInstall = showValue((Boolean) pcInfo.get(CLIENT_SHUTDOWN_INSTALL_KEY));
	}

	public HostInfo combineWith(HostInfo secondInfo) {
		if (secondInfo == null) {
			return this;
		}

		// save values which could be mixed
		Boolean clientWanConfigSave = clientWanConfig;
		Boolean clientMonitoringSave = clientMonitoring;
		Boolean clientUefiBootSave = uefiBoot;
		Boolean clientShutdownInstallSave = clientShutdownInstall;

		// empty everything
		resetValues();

		if (!secondInfo.clientWanConfig.equals(clientWanConfigSave)) {
			clientWanConfig = null;
		} else {
			clientWanConfig = clientWanConfigSave;
		}

		if (!secondInfo.clientMonitoring.equals(clientMonitoringSave)) {
			clientMonitoring = null;
		} else {
			clientMonitoring = clientMonitoringSave;
		}

		if (!secondInfo.uefiBoot.equals(clientUefiBootSave)) {
			uefiBoot = null;
		} else {
			uefiBoot = clientUefiBootSave;
		}

		if (!secondInfo.clientShutdownInstall.equals(clientShutdownInstallSave)) {
			clientShutdownInstall = null;
		} else {
			clientShutdownInstall = clientShutdownInstallSave;
		}

		return this;
	}

	public void resetGui() {
		Logging.info(this, "resetGui for ", this);

		MainFrame mainFrame = ConfigedMain.getMainFrame();

		mainFrame.getClientConfiguration().getClientInfoPanel().setClientDescriptionText(clientDescription);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientInventoryNumberText(clientInventoryNumber);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientNotesText(clientNotes);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientSystemUUID(clientSystemUUID);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientMacAddress(clientMacAddress);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientIpAddress(clientIpAddress);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientOS(clientOS);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientDeviceVendorAndModel(clientDeviceVendor,
				clientDeviceModel, clientDeviceType);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientOneTimePasswordText(clientOneTimePassword);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientMonitoring(clientMonitoring);
		mainFrame.getClientConfiguration().getClientInfoPanel().setClientPlatform(clientOSType);
		mainFrame.getClientConfiguration().getClientInfoPanel().setUefiBoot(uefiBoot);
		mainFrame.getClientConfiguration().getClientInfoPanel().setWANConfig(clientWanConfig);
		mainFrame.getClientConfiguration().getClientInfoPanel().setShutdownInstall(clientShutdownInstall);
		mainFrame.getClientConfiguration().getClientInfoPanel().setOpsiHostKey(hostKey);
	}

	private void setClientDescription(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_DESCRIPTION_KEY) != null) {
			clientDescription = (String) sourceOfChanges.get(CLIENT_DESCRIPTION_KEY);
			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue("description"));
			if (col > -1) {
				clientTablePanel.getClientTable().setValueAt(clientDescription, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientDescriptionText(clientDescription);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setHostDescription(client, clientDescription);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_DESCRIPTION_KEY,
					clientDescription);
		}
	}

	private void setClientInventoryNumber(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY) != null) {
			clientInventoryNumber = (String) sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY);

			int col = clientTablePanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientInventoryNumber"));
			if (col > -1) {
				clientTablePanel.getClientTable().setValueAt(clientInventoryNumber, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientInventoryNumberText(clientInventoryNumber);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setClientInventoryNumber(client, clientInventoryNumber);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_INVENTORY_NUMBER_KEY,
					clientInventoryNumber);
		}
	}

	private void setOneTimePassword(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY) != null) {
			clientOneTimePassword = (String) sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY);

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientOneTimePasswordText(clientOneTimePassword);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setClientOneTimePassword(client, clientOneTimePassword);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_ONE_TIME_PASSWORD_KEY,
					clientOneTimePassword);
		}
	}

	private void setClientNotes(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_NOTES_KEY) != null) {
			clientNotes = (String) sourceOfChanges.get(CLIENT_NOTES_KEY);

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel().setClientNotesText(clientNotes);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setHostNotes(client, clientNotes);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_NOTES_KEY, clientNotes);
		}
	}

	private void setClientSystemUUID(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_SYSTEM_UUID_KEY) != null) {
			clientSystemUUID = ((String) sourceOfChanges.get(CLIENT_SYSTEM_UUID_KEY)).trim();

			int col = clientTablePanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientSystemUUID"));
			if (col > -1) {
				clientTablePanel.getClientTable().setValueAt(clientMacAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientSystemUUID(clientSystemUUID);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setSystemUUID(client, clientSystemUUID);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_SYSTEM_UUID_KEY,
					clientSystemUUID);
		}
	}

	private void setClientMACAddress(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY) != null) {
			clientMacAddress = ((String) sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY)).trim();

			int col = clientTablePanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
			if (col > -1) {
				clientTablePanel.getClientTable().setValueAt(clientMacAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientMacAddress(clientMacAddress);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setMacAddress(client, clientMacAddress);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_MAC_ADRESS_KEY,
					clientMacAddress);
		}
	}

	private void setClientIPAddress(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY) != null) {
			clientIpAddress = ((String) sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY)).trim();

			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue("ipAddress"));
			if (col > -1) {
				clientTablePanel.getClientTable().setValueAt(clientIpAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getClientConfiguration().getClientInfoPanel()
					.setClientIpAddress(clientIpAddress);

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setIpAddress(client, clientIpAddress);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_IP_ADDRESS_KEY,
					clientIpAddress);
		}
	}

	private static void setClientShutdownInstall(ClientTablePanel clientTablePanel, String client,
			Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY) != null) {
			boolean shutdownInstall = false;

			if ("true".equals(sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY))) {
				shutdownInstall = true;
			}

			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			if (col > -1) {
				// write it into the visible table
				clientTablePanel.getClientTable().setValueAt(shutdownInstall, row, col);
			}

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setInstallOnShutdown(client, shutdownInstall);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_SHUTDOWN_INSTALL_KEY,
					shutdownInstall);
		}
	}

	private void setClientWANConfig(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY) != null) {
			boolean wanStandard = "true".equals(sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY));

			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "showAndSave found col ", col);

			if (col > -1) {
				// write it into the visible table
				clientTablePanel.getClientTable().setValueAt(wanStandard, row, col);
			}

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getHostDataService().setWanConfig(client, wanStandard);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_WAN_CONFIG_KEY,
					wanStandard);
		}
	}

	public void showAndSaveInternally(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges) {
		if (client == null || client.isEmpty()) {
			Logging.warning(this, "show and save: no hostId given: ", sourceOfChanges);
			return;
		}

		Logging.info(this, "showAndSave client, source ", client, ", ", sourceOfChanges);

		if (sourceOfChanges == null) {
			return;
		}

		int row = clientTablePanel.findModelRowFromClientName(client);

		setClientDescription(clientTablePanel, client, sourceOfChanges, row);

		setClientInventoryNumber(clientTablePanel, client, sourceOfChanges, row);

		setOneTimePassword(client, sourceOfChanges);

		setClientNotes(client, sourceOfChanges);

		setClientSystemUUID(clientTablePanel, client, sourceOfChanges, row);

		setClientMACAddress(clientTablePanel, client, sourceOfChanges, row);

		setClientIPAddress(clientTablePanel, client, sourceOfChanges, row);

		setClientShutdownInstall(clientTablePanel, client, sourceOfChanges, row);

		setClientWANConfig(clientTablePanel, client, sourceOfChanges, row);
	}

	@Override
	public String toString() {
		return "(" + clientName + ";" + depotOfClient + ";" + clientDescription + ";" + clientInventoryNumber + ";"
				+ clientOneTimePassword + ";" + clientNotes + ";" + clientSystemUUID + ";" + clientMacAddress + ";"
				+ clientIpAddress + ";" + lastSeen + ";" + created + ";" + clientWanConfig + ";" + clientShutdownInstall
				+ ";" + clientOS + ")";
	}

	public void resetValues() {
		depotOfClient = "";
		clientDescription = "";
		clientInventoryNumber = "";
		clientOneTimePassword = "";
		clientNotes = "";

		clientSystemUUID = "";
		clientMacAddress = "";
		lastSeen = "";
		created = "";
		clientName = "";
		hostKey = "";

		hostType = "";
		clientIpAddress = "";
		clientWanConfig = false;
		clientShutdownInstall = false;

		clientOS = "";
		// unknown icon is used (e.g. multiple selected clients)
		clientOSType = "<<intern:empty>>";
		clientOSArchitecture = "";
		clientDeviceVendor = "";
		clientDeviceModel = "";
		// empty text used (e.g. multiple selected clients)
		clientDeviceType = "<<intern:empty>>";
	}
}
