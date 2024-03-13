/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.ClientTable;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

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
	public static final String CLIENT_UEFI_BOOT_KEY = "uefiBoot";
	public static final String CLIENT_WAN_CONFIG_KEY = "wanConfig";
	public static final String CLIENT_SHUTDOWN_INSTALL_KEY = "clientShutdownInstall";
	public static final String DEPOT_WORKBENCH_KEY = "workbenchLocalUrl";

	public static final String DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL = "depotId";
	public static final String CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL = "clientDescription";
	public static final String CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL = "clientInventoryNumber";

	public static final String CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL = "clientSystemUUID";
	public static final String CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL = "clientHardwareAddress";
	public static final String LAST_SEEN_DISPLAY_FIELD_LABEL = "clientLastSeen";
	public static final String CREATED_DISPLAY_FIELD_LABEL = "clientCreated";
	public static final String HOST_NAME_DISPLAY_FIELD_LABEL = "clientName";

	public static final String CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL = "clientIPAddress";
	public static final String CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL = "UEFIboot";
	public static final String CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL = "WANmode";
	public static final String CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL = "clientSessionInfo";

	public static final String CLIENT_CONNECTED_DISPLAY_FIELD_LABEL = "clientConnected";
	public static final String CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL = "installByShutdown";

	public static final List<String> ORDERING_DISPLAY_FIELDS = List.of(HOST_NAME_DISPLAY_FIELD_LABEL,
			CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL, CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL,
			CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, LAST_SEEN_DISPLAY_FIELD_LABEL, CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL,
			CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL,
			CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL,
			CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, CREATED_DISPLAY_FIELD_LABEL,
			CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);

	public static final String IS_MASTER_DEPOT_KEY = "isMasterDepot";

	public static final String HOST_TYPE_VALUE_OPSI_CONFIG_SERVER = "OpsiConfigserver";
	public static final String HOST_TYPE_VALUE_OPSI_DEPOT_SERVER = "OpsiDepotserver";
	public static final String HOST_TYPE_VALUE_OPSI_CLIENT = "OpsiClient";

	// an AtomicInteger would be threadsafe
	private static int numberOfInstances;

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
	private Boolean clientUefiBoot;
	private Boolean clientWanConfig;

	private Boolean clientShutdownInstall;

	OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory.getPersistenceController();

	public HostInfo() {
		initialize();
		increaseInstancesCount();
	}

	public HostInfo(Map<String, Object> pcInfo) {
		increaseInstancesCount();
		setBy(pcInfo);
	}

	public static void resetInstancesCount() {
		numberOfInstances = 0;
	}

	public static int getInstancesCount() {
		return numberOfInstances;
	}

	private static void increaseInstancesCount() {
		numberOfInstances++;
	}

	public Map<String, Object> getDisplayRowMap0() {
		Map<String, Object> unordered = new HashMap<>();

		unordered.put(HOST_NAME_DISPLAY_FIELD_LABEL, clientName);
		unordered.put(CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL, clientDescription);
		unordered.put(CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL, clientInventoryNumber);
		unordered.put(LAST_SEEN_DISPLAY_FIELD_LABEL, lastSeen);

		unordered.put(CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, clientWanConfig);
		unordered.put(CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, clientIpAddress);
		unordered.put(CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL, clientSystemUUID);
		unordered.put(CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, clientMacAddress);
		unordered.put(CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL, clientUefiBoot);
		unordered.put(CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, clientShutdownInstall);

		unordered.put(CREATED_DISPLAY_FIELD_LABEL, created);
		unordered.put(DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, depotOfClient);

		Logging.debug(this, "getMap clientName " + clientName + " : " + unordered);

		return unordered;
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
		unordered.put(CLIENT_UEFI_BOOT_KEY, clientUefiBoot);
		unordered.put(CLIENT_WAN_CONFIG_KEY, clientWanConfig);

		unordered.put(CLIENT_SHUTDOWN_INSTALL_KEY, clientShutdownInstall);

		Logging.debug(this, "getMap clientName " + clientName);

		return unordered;
	}

	public static List<String> getKeysForCSV() {
		List<String> keys = new ArrayList<>();
		keys.add(HOSTNAME_KEY);
		keys.add("domain");
		keys.add(DEPOT_OF_CLIENT_KEY);
		keys.add(CLIENT_DESCRIPTION_KEY);
		keys.add(CLIENT_INVENTORY_NUMBER_KEY);
		keys.add(CLIENT_NOTES_KEY);
		keys.add(CLIENT_SYSTEM_UUID_KEY);
		keys.add(CLIENT_MAC_ADRESS_KEY);
		keys.add(CLIENT_IP_ADDRESS_KEY);
		keys.add("groups");
		keys.add(CLIENT_WAN_CONFIG_KEY);
		keys.add(CLIENT_UEFI_BOOT_KEY);
		keys.add(CLIENT_SHUTDOWN_INSTALL_KEY);
		keys.add(HOST_KEY_KEY);
		return Collections.unmodifiableList(keys);
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

		case CLIENT_UEFI_BOOT_KEY:
			clientUefiBoot = (Boolean) value;
			break;

		case CLIENT_WAN_CONFIG_KEY:
			clientWanConfig = (Boolean) value;
			break;

		case CLIENT_SHUTDOWN_INSTALL_KEY:
			clientShutdownInstall = (Boolean) value;
			break;

		default:
			Logging.warning(this, "key " + key + " not expected");
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

	public Boolean getUefiBoot() {
		return clientUefiBoot;
	}

	public Boolean getWanConfig() {
		return clientWanConfig;
	}

	public Boolean getShutdownInstall() {
		return clientShutdownInstall;
	}

	public void setUefiBoot(boolean b) {
		clientUefiBoot = b;
	}

	public void setShutdownInstall(boolean b) {
		clientShutdownInstall = b;
	}

	public void setWanConfig(boolean b) {
		clientWanConfig = b;
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

	public void setBy(Map<String, Object> pcInfo) {
		// shows pckey

		if (pcInfo == null) {
			initialize();
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

		depotOfClient = showValue((String) pcInfo.get(DEPOT_OF_CLIENT_KEY));

		clientUefiBoot = showValue((Boolean) pcInfo.get(CLIENT_UEFI_BOOT_KEY));
		clientWanConfig = showValue((Boolean) pcInfo.get(CLIENT_WAN_CONFIG_KEY));

		clientShutdownInstall = showValue((Boolean) pcInfo.get(CLIENT_SHUTDOWN_INSTALL_KEY));
	}

	public HostInfo combineWith(HostInfo secondInfo) {
		if (secondInfo == null) {
			return this;
		}

		// save values which could be mixed
		Boolean clientWanConfigSave = clientWanConfig;
		Boolean clientUefiBootSave = clientUefiBoot;
		Boolean clientShutdownInstallSave = clientShutdownInstall;

		// empty everything
		initialize();

		if (!secondInfo.clientWanConfig.equals(clientWanConfigSave)) {
			clientWanConfig = null;
		} else {
			clientWanConfig = clientWanConfigSave;
		}

		if (!secondInfo.clientUefiBoot.equals(clientUefiBootSave)) {
			clientUefiBoot = null;
		} else {
			clientUefiBoot = clientUefiBootSave;
		}

		if (!secondInfo.clientShutdownInstall.equals(clientShutdownInstallSave)) {
			clientShutdownInstall = null;
		} else {
			clientShutdownInstall = clientShutdownInstallSave;
		}

		return this;
	}

	public void resetGui() {
		Logging.info(this, "resetGui for " + toString());

		MainFrame mainFrame = ConfigedMain.getMainFrame();

		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientDescriptionText(clientDescription);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientInventoryNumberText(clientInventoryNumber);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientNotesText(clientNotes);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientSystemUUID(clientSystemUUID);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientMacAddress(clientMacAddress);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientIpAddress(clientIpAddress);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setClientOneTimePasswordText(clientOneTimePassword);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setUefiBoot(clientUefiBoot);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setWANConfig(clientWanConfig);
		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setShutdownInstall(clientShutdownInstall);

		mainFrame.getTabbedConfigPanes().getClientInfoPanel().setOpsiHostKey(hostKey);
	}

	private void setClientDescription(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_DESCRIPTION_KEY) != null) {
			clientDescription = (String) sourceOfChanges.get(CLIENT_DESCRIPTION_KEY);
			int col = selectionPanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
			if (col > -1) {
				selectionPanel.getTableModel().setValueAt(clientDescription, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel()
					.setClientDescriptionText(clientDescription);

			persistenceController.getHostDataService().setHostDescription(client, clientDescription);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_DESCRIPTION_KEY,
					clientDescription);
		}
	}

	private void setClientInventoryNumber(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY) != null) {
			clientInventoryNumber = (String) sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY);

			int col = selectionPanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientInventoryNumber"));
			if (col > -1) {
				selectionPanel.getTableModel().setValueAt(clientInventoryNumber, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel()
					.setClientInventoryNumberText(clientInventoryNumber);

			persistenceController.getHostDataService().setClientInventoryNumber(client, clientInventoryNumber);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_INVENTORY_NUMBER_KEY,
					clientInventoryNumber);
		}
	}

	private void setOneTimePassword(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY) != null) {
			clientOneTimePassword = (String) sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY);

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel()
					.setClientOneTimePasswordText(clientOneTimePassword);

			persistenceController.getHostDataService().setClientOneTimePassword(client, clientOneTimePassword);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_ONE_TIME_PASSWORD_KEY,
					clientOneTimePassword);
		}
	}

	private void setClientNotes(String client, Map<?, ?> sourceOfChanges) {
		if (sourceOfChanges.get(CLIENT_NOTES_KEY) != null) {
			clientNotes = (String) sourceOfChanges.get(CLIENT_NOTES_KEY);

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel().setClientNotesText(clientNotes);

			persistenceController.getHostDataService().setHostNotes(client, clientNotes);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_NOTES_KEY, clientNotes);
		}
	}

	private void setClientSystemUUID(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_SYSTEM_UUID_KEY) != null) {
			clientSystemUUID = ((String) sourceOfChanges.get(CLIENT_SYSTEM_UUID_KEY)).trim();

			int col = selectionPanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientSystemUUID"));
			if (col > -1) {
				selectionPanel.getTableModel().setValueAt(clientMacAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel()
					.setClientSystemUUID(clientSystemUUID);

			persistenceController.getHostDataService().setSystemUUID(client, clientSystemUUID);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_SYSTEM_UUID_KEY,
					clientSystemUUID);
		}
	}

	private void setClientMACAddress(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY) != null) {
			clientMacAddress = ((String) sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY)).trim();

			int col = selectionPanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
			if (col > -1) {
				selectionPanel.getTableModel().setValueAt(clientMacAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel()
					.setClientMacAddress(clientMacAddress);

			persistenceController.getHostDataService().setMacAddress(client, clientMacAddress);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_MAC_ADRESS_KEY,
					clientMacAddress);
		}
	}

	private void setClientIPAddress(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY) != null) {
			clientIpAddress = ((String) sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY)).trim();

			int col = selectionPanel.getTableModel()
					.findColumn(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientIPAddress"));
			if (col > -1) {
				selectionPanel.getTableModel().setValueAt(clientIpAddress, row, col);
			}

			// restoring old value
			ConfigedMain.getMainFrame().getTabbedConfigPanes().getClientInfoPanel().setClientIpAddress(clientIpAddress);

			persistenceController.getHostDataService().setIpAddress(client, clientIpAddress);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_IP_ADDRESS_KEY,
					clientIpAddress);
		}
	}

	private void setClientShutdownInstall(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges,
			int row) {
		if (sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY) != null) {
			boolean shutdownInstall = false;

			if ("true".equals(sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY))) {
				shutdownInstall = true;
			}

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			if (col > -1) {
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(shutdownInstall, row, col);
			}

			persistenceController.getConfigDataService().configureInstallByShutdown(client, shutdownInstall);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_SHUTDOWN_INSTALL_KEY,
					shutdownInstall);
		}
	}

	private void setClientUEFIBoot(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_UEFI_BOOT_KEY) != null) {
			boolean uefiboot = false;

			if ("true".equals(sourceOfChanges.get(CLIENT_UEFI_BOOT_KEY))) {
				uefiboot = true;
			}

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

			if (col > -1) {
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(uefiboot, row, col);
			}

			persistenceController.getConfigDataService().configureUefiBoot(client, uefiboot);
			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_UEFI_BOOT_KEY, uefiboot);
		}
	}

	private void setClientWANConfig(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges, int row) {
		if (sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY) != null) {
			boolean wanStandard = false;

			if ("true".equals(sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY))) {
				wanStandard = true;
			}

			int col = selectionPanel.getTableModel().findColumn(Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "showAndSave found col " + col);

			if (col > -1) {
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(wanStandard, row, col);
			}

			if (!(persistenceController.getConfigDataService().setWANConfigs(client, wanStandard))) {
				Logging.error(this, "wan settings could not be set");
			}

			persistenceController.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_WAN_CONFIG_KEY,
					wanStandard);
		}
	}

	public void showAndSaveInternally(ClientTable selectionPanel, String client, Map<?, ?> sourceOfChanges) {
		if (client == null || client.isEmpty()) {
			Logging.warning(this, "show and save: no hostId given: " + sourceOfChanges);
			return;
		}

		Logging.info(this, "showAndSave client, source " + client + ", " + sourceOfChanges);

		if (sourceOfChanges == null) {
			return;
		}

		int row = selectionPanel.findModelRowFromValue(client);

		setClientDescription(selectionPanel, client, sourceOfChanges, row);

		setClientInventoryNumber(selectionPanel, client, sourceOfChanges, row);

		setOneTimePassword(client, sourceOfChanges);

		setClientNotes(client, sourceOfChanges);

		setClientSystemUUID(selectionPanel, client, sourceOfChanges, row);

		setClientMACAddress(selectionPanel, client, sourceOfChanges, row);

		setClientIPAddress(selectionPanel, client, sourceOfChanges, row);

		setClientShutdownInstall(selectionPanel, client, sourceOfChanges, row);

		setClientUEFIBoot(selectionPanel, client, sourceOfChanges, row);

		setClientWANConfig(selectionPanel, client, sourceOfChanges, row);
	}

	@Override
	public String toString() {
		return "(" + clientName + ";" + depotOfClient + ";" + clientDescription + ";" + clientInventoryNumber + ";"
				+ clientOneTimePassword + ";" + clientNotes + ";" + clientSystemUUID + ";" + clientMacAddress + ";"
				+ clientIpAddress + ";" + lastSeen + ";" + created + ";" + clientUefiBoot + ";" + clientWanConfig + ";"
				+ clientShutdownInstall + ")";
	}

	public void initialize() {
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
		clientUefiBoot = false;
		clientWanConfig = false;
		clientShutdownInstall = false;
	}
}
