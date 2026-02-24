/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ClientInfoPanel;
import de.uib.configed.gui.ClientTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.share.logging.Logging;

public class HostInfo {
	public static final String DEPOT_OF_CLIENT_KEY = "depotId";
	public static final String CLIENT_DESCRIPTION_KEY = "description";
	public static final String CLIENT_INVENTORY_NUMBER_KEY = "inventoryNumber";
	public static final String CLIENT_ONE_TIME_PASSWORD_KEY = "oneTimePassword";
	public static final String CLIENT_NOTES_KEY = "notes";
	public static final String CLIENT_SYSTEM_UUID_KEY = "systemUUID";
	public static final String CLIENT_MAC_ADDRESS_KEY = "hardwareAddress";
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
	public static final String CSV_DOMAIN_KEY = "domain";
	public static final String CSV_GROUPS_KEY = "groups";
	public static final String CSV_NETBOOT_PRODUCT_KEY = "netbootProduct";

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

	private static final Set<String> keys = Set.of(DEPOT_OF_CLIENT_KEY, CLIENT_DESCRIPTION_KEY,
			CLIENT_INVENTORY_NUMBER_KEY, CLIENT_ONE_TIME_PASSWORD_KEY, CLIENT_NOTES_KEY, CLIENT_SYSTEM_UUID_KEY,
			CLIENT_MAC_ADDRESS_KEY, LAST_SEEN_KEY, CREATED_KEY, HOSTNAME_KEY, HOST_KEY_KEY, HOST_TYPE_KEY,
			CLIENT_IP_ADDRESS_KEY, CLIENT_WAN_CONFIG_KEY, CLIENT_SHUTDOWN_INSTALL_KEY, CLIENT_OS_KEY,
			CLIENT_OS_TYPE_KEY, CLIENT_OS_ARCHITECTURE_KEY, CLIENT_DEVICE_TYPE_KEY, CLIENT_DEVICE_VENDOR_KEY,
			CLIENT_DEVICE_MODEL_KEY, CLIENT_MONITORING_KEY, UEFI_BOOT_KEY);

	private static final Set<String> booleanKeys = Set.of(CLIENT_WAN_CONFIG_KEY, CLIENT_SHUTDOWN_INSTALL_KEY,
			CLIENT_MONITORING_KEY, UEFI_BOOT_KEY);

	private static final Set<String> stringKeys = keys.stream().filter(k -> !booleanKeys.contains(k))
			.collect(Collectors.toSet());

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

	private final Map<String, Object> data = new HashMap<>();

	public Map<String, Object> getDisplayRowMap() {
		Map<String, Object> displayRowMap = new HashMap<>();

		displayRowMap.put(CLIENT_OS_TYPE_DISPLAY_FIELD_LABEL, data.get(CLIENT_OS_TYPE_KEY));
		displayRowMap.put(HOST_NAME_DISPLAY_FIELD_LABEL, data.get(HOSTNAME_KEY));
		displayRowMap.put(CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL, data.get(CLIENT_DESCRIPTION_KEY));
		displayRowMap.put(CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL, data.get(CLIENT_INVENTORY_NUMBER_KEY));
		displayRowMap.put(LAST_SEEN_DISPLAY_FIELD_LABEL, data.get(LAST_SEEN_KEY));

		displayRowMap.put(CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, data.get(CLIENT_WAN_CONFIG_KEY));
		displayRowMap.put(CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, data.get(CLIENT_IP_ADDRESS_KEY));
		displayRowMap.put(CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL, data.get(CLIENT_SYSTEM_UUID_KEY));
		displayRowMap.put(CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, data.get(CLIENT_MAC_ADDRESS_KEY));
		displayRowMap.put(CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, data.get(CLIENT_SHUTDOWN_INSTALL_KEY));

		displayRowMap.put(CREATED_DISPLAY_FIELD_LABEL, data.get(CREATED_KEY));
		displayRowMap.put(DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, data.get(DEPOT_OF_CLIENT_KEY));

		displayRowMap.put(CLIENT_OS_DISPLAY_FIELD_LABEL, data.get(CLIENT_OS_KEY));
		displayRowMap.put(CLIENT_OS_ARCHITECTURE_DISPLAY_FIELD_LABEL, data.get(CLIENT_OS_ARCHITECTURE_KEY));
		displayRowMap.put(CLIENT_DEVICE_TYPE_DISPLAY_FIELD_LABEL, data.get(CLIENT_DEVICE_TYPE_KEY));
		displayRowMap.put(CLIENT_DEVICE_VENDOR_DISPLAY_FIELD_LABEL, data.get(CLIENT_DEVICE_VENDOR_KEY));
		displayRowMap.put(CLIENT_DEVICE_MODEL_DISPLAY_FIELD_LABEL, data.get(CLIENT_DEVICE_MODEL_KEY));
		displayRowMap.put(CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL, data.get(CLIENT_MONITORING_KEY));
		Logging.debug(this, "getMap clientName ", data.get(HOSTNAME_KEY), " : ", displayRowMap);

		return displayRowMap;
	}

	public Map<String, Object> getMap() {
		Map<String, Object> unordered = new HashMap<>();

		unordered.put(DEPOT_OF_CLIENT_KEY, data.get(DEPOT_OF_CLIENT_KEY));
		unordered.put(CLIENT_DESCRIPTION_KEY, data.get(CLIENT_DESCRIPTION_KEY));
		unordered.put(CLIENT_INVENTORY_NUMBER_KEY, data.get(CLIENT_INVENTORY_NUMBER_KEY));
		unordered.put(CLIENT_ONE_TIME_PASSWORD_KEY, data.get(CLIENT_ONE_TIME_PASSWORD_KEY));
		unordered.put(CLIENT_NOTES_KEY, data.get(CLIENT_NOTES_KEY));
		unordered.put(CLIENT_SYSTEM_UUID_KEY, data.get(CLIENT_SYSTEM_UUID_KEY));
		unordered.put(CLIENT_MAC_ADDRESS_KEY, data.get(CLIENT_MAC_ADDRESS_KEY));
		unordered.put(LAST_SEEN_KEY, data.get(LAST_SEEN_KEY));
		unordered.put(CREATED_KEY, data.get(CREATED_KEY));
		unordered.put(HOSTNAME_KEY, data.get(HOSTNAME_KEY));
		unordered.put(HOST_KEY_KEY, data.get(HOST_KEY_KEY));

		unordered.put(HOST_TYPE_KEY, data.get(HOST_TYPE_KEY));
		unordered.put(CLIENT_IP_ADDRESS_KEY, data.get(CLIENT_IP_ADDRESS_KEY));
		unordered.put(CLIENT_WAN_CONFIG_KEY, data.get(CLIENT_WAN_CONFIG_KEY));
		unordered.put(CLIENT_SHUTDOWN_INSTALL_KEY, data.get(CLIENT_SHUTDOWN_INSTALL_KEY));

		unordered.put(CLIENT_OS_KEY, data.get(CLIENT_OS_KEY));
		unordered.put(CLIENT_OS_TYPE_KEY, data.get(CLIENT_OS_TYPE_KEY));
		unordered.put(CLIENT_OS_ARCHITECTURE_KEY, data.get(CLIENT_OS_ARCHITECTURE_KEY));
		unordered.put(CLIENT_DEVICE_TYPE_KEY, data.get(CLIENT_DEVICE_TYPE_KEY));
		unordered.put(CLIENT_DEVICE_VENDOR_KEY, data.get(CLIENT_DEVICE_VENDOR_KEY));
		unordered.put(CLIENT_DEVICE_MODEL_KEY, data.get(CLIENT_DEVICE_MODEL_KEY));
		unordered.put(CLIENT_MONITORING_KEY, data.get(CLIENT_MONITORING_KEY));
		unordered.put(UEFI_BOOT_KEY, data.get(UEFI_BOOT_KEY));
		Logging.debug(this, "getMap clientName ", data.get(HOSTNAME_KEY));

		return unordered;
	}

	public static Set<String> getKeysForCSV() {
		Set<String> keys = new LinkedHashSet<>();
		keys.add(HOSTNAME_KEY);
		keys.add(CSV_DOMAIN_KEY);
		keys.add(DEPOT_OF_CLIENT_KEY);
		keys.add(CLIENT_MAC_ADDRESS_KEY);
		keys.add(CLIENT_DESCRIPTION_KEY);
		keys.add(CLIENT_INVENTORY_NUMBER_KEY);
		keys.add(CLIENT_NOTES_KEY);
		keys.add(CLIENT_SYSTEM_UUID_KEY);
		keys.add(CLIENT_IP_ADDRESS_KEY);
		keys.add(CSV_GROUPS_KEY);
		keys.add(CLIENT_WAN_CONFIG_KEY);
		keys.add(CLIENT_SHUTDOWN_INSTALL_KEY);
		keys.add(HOST_KEY_KEY);
		keys.add(CSV_NETBOOT_PRODUCT_KEY);
		return Collections.unmodifiableSet(keys);
	}

	public static List<String> getGroupsFromObject(Object groups) {
		if (!((String) groups).contains(",")) {
			List<String> result = new ArrayList<>();
			result.add((String) groups);
			return result;
		}
		return Arrays.asList(((String) groups).split(","));
	}

	public void put(String key, Object value) {
		if (keys.contains(key)) {
			data.put(key, value);
		} else {
			Logging.warning(this, "key ", key, " not expected");
		}
	}

	public String getString(String key) {
		if (stringKeys.contains(key)) {
			return (String) data.get(key);
		} else {
			Logging.warning(this, "key ", key, " not expected");
			return null;
		}
	}

	public Boolean getWanConfig() {
		return (Boolean) data.get(CLIENT_WAN_CONFIG_KEY);
	}

	public Boolean getMonitoring() {
		return (Boolean) data.get(CLIENT_MONITORING_KEY);
	}

	public Boolean getUefiBoot() {
		return (Boolean) data.get(UEFI_BOOT_KEY);
	}

	public Boolean getShutdownInstall() {
		return (Boolean) data.get(CLIENT_SHUTDOWN_INSTALL_KEY);
	}

	public void setType(String type) {
		data.put(HOST_TYPE_KEY, type);
	}

	private static Object showValue(String key, Object value) {
		if (booleanKeys.contains(key)) {
			return Boolean.TRUE.equals(value);
		} else {
			return value == null || "null".equals(value) ? "" : value;
		}
	}

	public void setValues(Map<String, Object> pcInfo) {
		// shows pckey

		if (pcInfo == null) {
			resetValues();
			return;
		}

		keys.forEach(key -> put(key, showValue(key, pcInfo.get(key))));
	}

	public HostInfo combineWith(HostInfo secondInfo) {
		if (secondInfo == null) {
			return this;
		}

		// save values which could be mixed
		Boolean clientWanConfigSave = (Boolean) data.get(CLIENT_WAN_CONFIG_KEY);
		Boolean clientMonitoringSave = (Boolean) data.get(CLIENT_MONITORING_KEY);
		Boolean clientUefiBootSave = (Boolean) data.get(UEFI_BOOT_KEY);
		Boolean clientShutdownInstallSave = (Boolean) data.get(CLIENT_SHUTDOWN_INSTALL_KEY);

		// empty everything
		resetValues();

		if (!secondInfo.getWanConfig().equals(clientWanConfigSave)) {
			data.put(CLIENT_WAN_CONFIG_KEY, null);
		} else {
			data.put(CLIENT_WAN_CONFIG_KEY, clientWanConfigSave);
		}

		if (!secondInfo.getMonitoring().equals(clientMonitoringSave)) {
			data.put(CLIENT_MONITORING_KEY, null);
		} else {
			data.put(CLIENT_MONITORING_KEY, clientMonitoringSave);
		}

		if (!secondInfo.getUefiBoot().equals(clientUefiBootSave)) {
			data.put(UEFI_BOOT_KEY, null);
		} else {
			data.put(UEFI_BOOT_KEY, clientUefiBootSave);
		}

		if (!secondInfo.getShutdownInstall().equals(clientShutdownInstallSave)) {
			data.put(CLIENT_SHUTDOWN_INSTALL_KEY, null);
		} else {
			data.put(CLIENT_SHUTDOWN_INSTALL_KEY, clientShutdownInstallSave);
		}

		return this;
	}

	public void resetGui() {
		Logging.info(this, "resetGui for ", this);

		ClientInfoPanel clientInfoPanel = ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration()
				.getClientInfoPanel();

		clientInfoPanel.setClientDescriptionText((String) data.get(CLIENT_DESCRIPTION_KEY));
		clientInfoPanel.setClientInventoryNumberText((String) data.get(CLIENT_INVENTORY_NUMBER_KEY));
		clientInfoPanel.setClientNotesText((String) data.get(CLIENT_NOTES_KEY));
		clientInfoPanel.setClientSystemUUID((String) data.get(CLIENT_SYSTEM_UUID_KEY));
		clientInfoPanel.setClientMacAddress((String) data.get(CLIENT_MAC_ADDRESS_KEY));
		clientInfoPanel.setClientIpAddress((String) data.get(CLIENT_IP_ADDRESS_KEY));
		clientInfoPanel.setClientOS((String) data.get(CLIENT_OS_KEY));
		clientInfoPanel.setClientDeviceVendorAndModel((String) data.get(CLIENT_DEVICE_VENDOR_KEY),
				(String) data.get(CLIENT_DEVICE_MODEL_KEY), (String) data.get(CLIENT_DEVICE_TYPE_KEY));
		clientInfoPanel.setClientOneTimePasswordText((String) data.get(CLIENT_ONE_TIME_PASSWORD_KEY));
		clientInfoPanel.setClientMonitoring((Boolean) data.get(CLIENT_MONITORING_KEY));
		clientInfoPanel.setClientPlatform((String) data.get(CLIENT_OS_TYPE_KEY));
		clientInfoPanel.setUefiBoot((Boolean) data.get(UEFI_BOOT_KEY));
		clientInfoPanel.setWANConfig((Boolean) data.get(CLIENT_WAN_CONFIG_KEY));
		clientInfoPanel.setShutdownInstall((Boolean) data.get(CLIENT_SHUTDOWN_INSTALL_KEY));
		clientInfoPanel.setOpsiHostKey((String) data.get(HOST_KEY_KEY));
	}

	private void setClientValue(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges, String key,
			String displayFieldLabel, Consumer<String> setTextMethod) {
		if (sourceOfChanges.get(key) != null) {
			data.put(key, sourceOfChanges.get(key));

			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue(displayFieldLabel));
			if (col > -1) {
				int row = clientTablePanel.findModelRowFromClientName(client);
				clientTablePanel.getClientTable().setValueAt(data.get(key), row, col);
			}

			// restoring old value
			setTextMethod.accept((String) data.get(key));

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getDataServices().host.updateHost(client, key, data.get(key));
			persistenceController.getDataServices().hostInfoCollections.updateLocalHostInfo(client, key, data.get(key));
		}
	}

	private void setOneTimePassword(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY) != null) {
			data.put(CLIENT_ONE_TIME_PASSWORD_KEY, sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY));

			// restoring old value
			ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getClientInfoPanel()
					.setClientOneTimePasswordText((String) data.get(CLIENT_ONE_TIME_PASSWORD_KEY));
			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getDataServices().host.updateHost(client, CLIENT_ONE_TIME_PASSWORD_KEY,
					data.get(CLIENT_ONE_TIME_PASSWORD_KEY));
			persistenceController.getDataServices().hostInfoCollections.updateLocalHostInfo(client,
					CLIENT_ONE_TIME_PASSWORD_KEY, data.get(CLIENT_ONE_TIME_PASSWORD_KEY));
		}
	}

	private void setClientNotes(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_NOTES_KEY) != null) {
			data.put(CLIENT_NOTES_KEY, sourceOfChanges.get(CLIENT_NOTES_KEY));

			// restoring old value
			ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getClientInfoPanel()
					.setClientNotesText((String) data.get(CLIENT_NOTES_KEY));

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getDataServices().host.updateHost(client, CLIENT_NOTES_KEY,
					data.get(CLIENT_NOTES_KEY));
			persistenceController.getDataServices().hostInfoCollections.updateLocalHostInfo(client, CLIENT_NOTES_KEY,
					data.get(CLIENT_NOTES_KEY));
		}
	}

	private static void setClientBoolean(ClientTablePanel clientTablePanel, String client, Map<?, ?> sourceOfChanges,
			String key, String displayFieldLabel) {
		if (sourceOfChanges.get(key) != null) {
			boolean value = "true".equals(sourceOfChanges.get(key));

			int col = clientTablePanel.getTableModel().findColumn(Configed.getResourceValue(displayFieldLabel));

			if (col > -1) {
				int row = clientTablePanel.findModelRowFromClientName(client);
				// write it into the visible table
				clientTablePanel.getClientTable().setValueAt(value, row, col);
			}

			OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
					.getPersistenceController();
			persistenceController.getDataServices().host.updateHost(client, key, value);
			persistenceController.getDataServices().hostInfoCollections.updateLocalHostInfo(client, key, value);
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

		ClientInfoPanel clientInfoPanel = ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration()
				.getClientInfoPanel();

		setClientValue(clientTablePanel, client, sourceOfChanges, CLIENT_DESCRIPTION_KEY, "description",
				clientInfoPanel::setClientDescriptionText);

		setClientValue(clientTablePanel, client, sourceOfChanges, CLIENT_INVENTORY_NUMBER_KEY,
				"ConfigedMain.pclistTableModel.clientInventoryNumber", clientInfoPanel::setClientInventoryNumberText);

		setOneTimePassword(client, sourceOfChanges);

		setClientNotes(client, sourceOfChanges);

		setClientValue(clientTablePanel, client, sourceOfChanges, CLIENT_SYSTEM_UUID_KEY,
				"ConfigedMain.pclistTableModel.clientSystemUUID", clientInfoPanel::setClientSystemUUID);

		setClientValue(clientTablePanel, client, sourceOfChanges, CLIENT_MAC_ADDRESS_KEY,
				"ConfigedMain.pclistTableModel.clientHardwareAddress", clientInfoPanel::setClientMacAddress);

		setClientValue(clientTablePanel, client, sourceOfChanges, CLIENT_IP_ADDRESS_KEY, "ipAddress",
				clientInfoPanel::setClientIpAddress);

		setClientBoolean(clientTablePanel, client, sourceOfChanges, CLIENT_SHUTDOWN_INSTALL_KEY,
				"ConfigedMain.pclistTableModel." + CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);

		setClientBoolean(clientTablePanel, client, sourceOfChanges, CLIENT_WAN_CONFIG_KEY,
				"ConfigedMain.pclistTableModel." + CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
	}

	@Override
	public String toString() {
		return data.toString();
	}

	public void resetValues() {
		stringKeys.forEach(stringKey -> data.put(stringKey, ""));
		booleanKeys.forEach(booleanKey -> data.put(booleanKey, false));

		data.put(CLIENT_OS_TYPE_KEY, "<<intern:empty>>");
		data.put(CLIENT_DEVICE_TYPE_KEY, "<<intern:empty>>");
	}
}
