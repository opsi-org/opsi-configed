package de.uib.configed.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uib.configed.Configed;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;

public class HostInfo {
	static int callCounter = 0;
	private static int numberOfInstances = 0; // an AtomicInteger would be threadsafe
	private final int instanceNumber;
	private static Map<String, Integer> id2InstanceNumber;

	protected String depotOfClient;
	protected String clientDescription;
	protected String clientInventoryNumber;
	protected String clientOneTimePassword;
	protected String clientNotes;

	protected String clientMacAddress;
	protected String lastSeen;
	protected String created;
	protected String clientName;
	protected String hostKey;

	protected String hostType;
	protected String clientIpAddress;
	protected Boolean clientUefiBoot;
	protected Boolean clientWanConfig;
	protected String clientSessionInfo;

	protected Boolean clientConnected;
	protected Boolean clientShutdownInstall;

	// ---
	public static final String DEPOT_OF_CLIENT_KEY = "depotId";
	public static final String CLIENT_DESCRIPTION_KEY = "description";
	public static final String CLIENT_INVENTORY_NUMBER_KEY = "inventoryNumber";
	public static final String CLIENT_ONE_TIME_PASSWORD_KEY = "oneTimePassword";
	public static final String CLIENT_NOTES_KEY = "notes";
	public static final String CLIENT_MAC_ADRESS_KEY = "hardwareAddress";
	public static final String LAST_SEEN_KEY = "lastSeen";
	public static final String CREATED_KEY = "created";
	public static final String HOSTNAME_KEY = "id";
	public static final String HOST_KEY_KEY = "opsiHostKey";
	public static final String HOST_TYPE_KEY = "type";
	public static final String CLIENT_IP_ADDRESS_KEY = "ipAddress";
	public static final String CLIENT_UEFI_BOOT_KEY = "uefiBoot";
	public static final String CLIENT_WAN_CONFIG_KEY = "wanConfig";
	public static final String CLIENT_SESSION_INFO_KEY = "sessionInfo";
	public static final String CLIENT_CONNECTED_KEY = "clientConnected";
	public static final String CLIENT_SHUTDOWN_INSTALL_KEY = "clientShutdownInstall";
	public static final String DEPOT_WORKBENCH_KEY = "workbenchLocalUrl";

	// ---
	public static final String DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL = "depotId";
	public static final String CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL = "clientDescription";
	public static final String CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL = "clientInventoryNumber";
	public static final String CLIENT_ONE_TIME_PASSWORD_DISPLAY_FIELD_LABEL = "clientOneTimePassword";
	public static final String CLIENT_NOTES_DISPLAY_FIELD_LABEL = "notes";

	public static final String CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL = "clientHardwareAddress";
	public static final String LAST_SEEN_DISPLAY_FIELD_LABEL = "clientLastSeen";
	public static final String CREATED_DISPLAY_FIELD_LABEL = "clientCreated";
	public static final String HOST_NAME_DISPLAY_FIELD_LABEL = "clientName";
	public static final String HOST_KEY_DISPLAY_FIELD_LABEL = "opsiHostKey";

	public static final String HOST_TYPE_DISPLAY_FIELD_LABEL = "hostType";
	public static final String CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL = "clientIPAddress";
	public static final String CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL = "UEFIboot";
	public static final String CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL = "WANmode";
	public static final String CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL = "clientSessionInfo";

	public static final String CLIENT_CONNECTED_DISPLAY_FIELD_LABEL = "clientConnected";
	public static final String CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL = "installByShutdown";
	// ---

	public static final List<String> ORDERING_DISPLAY_FIELDS;
	static {
		ORDERING_DISPLAY_FIELDS = new LinkedList<>();

		ORDERING_DISPLAY_FIELDS.add(HOST_NAME_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(LAST_SEEN_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CREATED_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);

	}

	// --

	public static final String IS_MASTER_DEPOT_KEY = "isMasterDepot";

	public static final String HOST_TYPE_VALUE_OPSI_CONFIG_SERVER = "OpsiConfigserver";
	public static final String HOST_TYPE_VALUE_OPSI_DEPOT_SERVER = "OpsiDepotserver";
	public static final String HOST_TYPE_VALUE_OPSI_CLIENT = "OpsiClient";

	public static final String HOST_SUB_CLASS_TAG_OPSI_CLIENT_PROTOTYPE = "OpsiPrototype";

	static final String NOT_LEGAL_CHARS_0 = ",:!@#$%^&',(){} ";
	static final Set<Character> notLegalChars = new HashSet<>();
	static {
		for (int i = 0; i < NOT_LEGAL_CHARS_0.length(); i++) {
			notLegalChars.add(NOT_LEGAL_CHARS_0.charAt(i));
		}
	}

	public static String checkADNamingConvention(String proposal)
	// https://support.microsoft.com/en-us/kb/909264
	{

		boolean result = true;
		String hintMessage = null;

		if (proposal == null || proposal.trim().equals("")) {
			hintMessage = "name must not be empty";
			result = false;
		}

		if (result && proposal.length() > 15) {
			hintMessage = "netbios names are restricted to 15 characters";
			result = false;
		}

		final Pattern onlyDigitsP = Pattern.compile("\\d*");
		Matcher m = onlyDigitsP.matcher(proposal);

		if (result && m.matches()) {
			hintMessage = "only digits are not allowed";
			result = false;
		}

		if (result) {
			StringBuilder found = new StringBuilder();
			for (int i = 0; i < proposal.length(); i++) {
				if (notLegalChars.contains(proposal.charAt(i))) {
					found.append(proposal.charAt(i));
				}
			}
			if (found.length() > 0) {
				hintMessage = "not allowed character(s)>>>  '" + found + "'";
			}
		}

		return hintMessage;

	}

	public static void resetInstancesCount() {
		numberOfInstances = 0;
		id2InstanceNumber = new HashMap<>();
	}

	public static int getInstancesCount() {
		return numberOfInstances;
	}

	public static int incAndGetInstancesCount() {
		numberOfInstances++;
		return numberOfInstances;
	}

	public Integer getInstanceNumber(String key) {
		return id2InstanceNumber.get(key);
	}

	public HostInfo() {
		initialize();
		instanceNumber = incAndGetInstancesCount();
	}

	public HostInfo(Map<String, Object> pcInfo) {
		instanceNumber = incAndGetInstancesCount();
		setBy(pcInfo);
	}

	public boolean isInstanceNumber(int compareNumber) {
		return instanceNumber == compareNumber;
	}

	public Map<String, Object> getDisplayRowMap0() {
		HashMap<String, Object> unordered = new HashMap<>();

		unordered.put(HOST_NAME_DISPLAY_FIELD_LABEL, clientName);
		unordered.put(CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL, clientDescription);
		unordered.put(CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL, clientInventoryNumber);
		unordered.put(CLIENT_CONNECTED_DISPLAY_FIELD_LABEL, clientConnected);
		unordered.put(LAST_SEEN_DISPLAY_FIELD_LABEL, lastSeen);

		unordered.put(CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL, clientWanConfig);
		unordered.put(CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL, clientIpAddress);
		unordered.put(CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL, clientMacAddress);
		unordered.put(CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL, clientUefiBoot);
		unordered.put(CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL, clientShutdownInstall);

		unordered.put(CREATED_DISPLAY_FIELD_LABEL, created);
		unordered.put(CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL, clientSessionInfo);
		unordered.put(DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL, depotOfClient);
		unordered.put(CLIENT_CONNECTED_KEY, clientConnected);

		Logging.debug(this, "getMap clientName " + clientName + " : " + unordered);

		return unordered;
	}

	public Map<String, Object> getDisplayRowMap() {
		HashMap<String, Object> unordered = new HashMap<>(getDisplayRowMap0());
		unordered.put(HOST_NAME_DISPLAY_FIELD_LABEL, clientName);
		return unordered;
	}

	public Map<String, Object> getMap() {
		HashMap<String, Object> unordered = new HashMap<>();

		unordered.put(DEPOT_OF_CLIENT_KEY, depotOfClient);
		unordered.put(CLIENT_DESCRIPTION_KEY, clientDescription);
		unordered.put(CLIENT_INVENTORY_NUMBER_KEY, clientInventoryNumber);
		unordered.put(CLIENT_ONE_TIME_PASSWORD_KEY, clientOneTimePassword);
		unordered.put(CLIENT_NOTES_KEY, clientNotes);

		unordered.put(CLIENT_MAC_ADRESS_KEY, clientMacAddress);
		unordered.put(LAST_SEEN_KEY, lastSeen);
		unordered.put(CREATED_KEY, created);
		unordered.put(HOSTNAME_KEY, clientName);
		unordered.put(HOST_KEY_KEY, hostKey);

		unordered.put(HOST_TYPE_KEY, hostType);
		unordered.put(CLIENT_IP_ADDRESS_KEY, clientIpAddress);
		unordered.put(CLIENT_UEFI_BOOT_KEY, clientUefiBoot);
		unordered.put(CLIENT_WAN_CONFIG_KEY, clientWanConfig);
		unordered.put(CLIENT_SESSION_INFO_KEY, clientSessionInfo);

		unordered.put(CLIENT_CONNECTED_KEY, clientConnected);
		unordered.put(CLIENT_SHUTDOWN_INSTALL_KEY, clientShutdownInstall);

		Logging.debug(this, "getMap clientName " + clientName);

		return unordered;
	}

	public void put(String key, Object value) {
		switch (key) {
		case DEPOT_OF_CLIENT_KEY: {
			depotOfClient = "" + value;
			break;
		}
		case CLIENT_DESCRIPTION_KEY: {
			clientDescription = "" + value;
			break;
		}
		case CLIENT_INVENTORY_NUMBER_KEY: {
			clientInventoryNumber = "" + value;
			break;
		}
		case CLIENT_NOTES_KEY: {
			clientNotes = "" + value;
			break;
		}
		case CLIENT_ONE_TIME_PASSWORD_KEY: {
			clientOneTimePassword = "" + value;
			break;
		}
		case CLIENT_MAC_ADRESS_KEY: {
			clientMacAddress = "" + value;
			break;
		}
		case HOST_KEY_KEY: {
			hostKey = "" + value;
			break;
		}
		case CREATED_KEY: {
			created = "" + value;
			break;
		}
		case LAST_SEEN_KEY: {
			lastSeen = "" + value;
			break;
		}
		case CLIENT_UEFI_BOOT_KEY: {
			clientUefiBoot = (Boolean) value;
			break;
		}
		case CLIENT_WAN_CONFIG_KEY: {
			clientWanConfig = (Boolean) value;
			break;
		}
		case CLIENT_SHUTDOWN_INSTALL_KEY: {
			clientShutdownInstall = (Boolean) value;
			break;
		}
		default: {
			Logging.warning(this, "key " + key + " not expected");
		}
		}
	}

	public String getInDepot() {
		return depotOfClient;
	}

	public void setInDepot(String depot) {
		depotOfClient = depot;
	}

	public boolean isClientInDepot(String depot) {
		return depotOfClient.equalsIgnoreCase(depot);
	}

	public String getHostType() {
		return hostType;
	}

	public String getDescription() {
		return clientDescription;
	}

	public String getInventoryNumber() {
		return clientInventoryNumber;
	}

	public String getOneTimePassword() {
		return clientOneTimePassword;
	}

	public String getNotes() {
		return clientNotes;
	}

	public String getMacAddress() {
		return clientMacAddress;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public String getCreated() {
		return created;
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

	private String showValue(String value) {
		if (value == null || value.equals("null"))
			return "";
		else
			return value;
	}

	private boolean showValue(Boolean value) {
		if (value == null)
			return false;

		else
			return value;
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

		if (secondInfo == null)
			return this;

		// save values which could be mixed
		Boolean clientWanConfigSave = clientWanConfig;
		Boolean clientUefiBootSave = clientUefiBoot;
		Boolean clientShutdownInstallSave = clientShutdownInstall;

		initialize(); // empty everything

		if (!secondInfo.clientWanConfig.equals(clientWanConfigSave))
			clientWanConfig = null;
		else
			clientWanConfig = clientWanConfigSave;

		if (!secondInfo.clientUefiBoot.equals(clientUefiBootSave))
			clientUefiBoot = null;
		else
			clientUefiBoot = clientUefiBootSave;

		if (!secondInfo.clientShutdownInstall.equals(clientShutdownInstallSave))
			clientShutdownInstall = null;
		else
			clientShutdownInstall = clientShutdownInstallSave;

		return this;

	}

	public void setMap(Map<String, Object> infoMap) {
		Logging.debug(this, "setMap " + infoMap);
		initialize();

		if (infoMap == null) {
			return;
		}

		clientUefiBoot = showValue(((Boolean) infoMap.get(CLIENT_UEFI_BOOT_KEY)));
		clientWanConfig = showValue(((Boolean) infoMap.get(CLIENT_WAN_CONFIG_KEY)));
		clientShutdownInstall = showValue((Boolean) infoMap.get(CLIENT_SHUTDOWN_INSTALL_KEY));

		clientDescription = showValue("" + infoMap.get(CLIENT_DESCRIPTION_KEY));
		clientInventoryNumber = showValue("" + infoMap.get(CLIENT_INVENTORY_NUMBER_KEY));
		clientNotes = showValue("" + infoMap.get(CLIENT_NOTES_KEY));
		clientOneTimePassword = showValue("" + infoMap.get(CLIENT_ONE_TIME_PASSWORD_KEY));
		clientMacAddress = showValue("" + infoMap.get(CLIENT_MAC_ADRESS_KEY));
		clientIpAddress = showValue("" + infoMap.get(CLIENT_IP_ADDRESS_KEY));
		hostKey = showValue("" + infoMap.get(HOST_KEY_KEY));
		clientName = showValue("" + infoMap.get(HOSTNAME_KEY));
		created = showValue("" + infoMap.get(CREATED_KEY));
		lastSeen = showValue("" + infoMap.get(LAST_SEEN_KEY));
	}

	private int findCol(JTableSelectionPanel selectionPanel, String colName) {
		return selectionPanel.getTableModel().findColumn(colName

		);
	}

	private int findRow(JTableSelectionPanel selectionPanel, String client) {
		return selectionPanel.findModelRowFromValue(client, 0);
	}

	public void resetGui(MainFrame mainFrame) {
		Logging.info(this, "resetGui for " + toString());
		mainFrame.setClientDescriptionText(clientDescription);
		mainFrame.setClientInventoryNumberText(clientInventoryNumber);
		mainFrame.setClientNotesText(clientNotes);
		mainFrame.setClientMacAddress(clientMacAddress);
		mainFrame.setClientIpAddress(clientIpAddress);
		mainFrame.setClientOneTimePasswordText(clientOneTimePassword);
		mainFrame.setUefiBoot(clientUefiBoot);
		mainFrame.setWANConfig(clientWanConfig);
		mainFrame.setShutdownInstall(clientShutdownInstall);

		mainFrame.setOpsiHostKey(hostKey);
	}

	public void showAndSaveInternally(JTableSelectionPanel selectionPanel, MainFrame mainFrame,
			PersistenceController persist, String client, Map<String, String> sourceOfChanges) {
		if (client == null || client.equals("")) {
			Logging.warning(this, "show and save: no hostId given: " + sourceOfChanges);
			return;
		}

		Logging.info(this, "showAndSave client, source " + client + ", " + sourceOfChanges);
		int row = findRow(selectionPanel, client);

		if (sourceOfChanges == null)
			return;

		if (sourceOfChanges.get(CLIENT_DESCRIPTION_KEY) != null) {
			clientDescription = sourceOfChanges.get(CLIENT_DESCRIPTION_KEY);
			int col = findCol(selectionPanel,
					Configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientDescription, row, col);

			mainFrame.setClientDescriptionText(clientDescription); // restoring old value

			persist.setHostDescription(client, clientDescription);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_DESCRIPTION_KEY, clientDescription);
		}

		if (sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY) != null) {
			clientInventoryNumber = sourceOfChanges.get(CLIENT_INVENTORY_NUMBER_KEY);

			int col = findCol(selectionPanel,
					Configed.getResourceValue("ConfigedMain.pclistTableModel.clientInventoryNumber"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientInventoryNumber, row, col);

			mainFrame.setClientInventoryNumberText(clientInventoryNumber); // restoring old value

			persist.setClientInventoryNumber(client, clientInventoryNumber);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_INVENTORY_NUMBER_KEY,
					clientInventoryNumber);
		}

		if (sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY) != null) {
			clientOneTimePassword = sourceOfChanges.get(CLIENT_ONE_TIME_PASSWORD_KEY);
			mainFrame.setClientOneTimePasswordText(clientOneTimePassword); // restoring old value

			persist.setClientOneTimePassword(client, clientOneTimePassword);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_ONE_TIME_PASSWORD_KEY,
					clientOneTimePassword);
		}

		if (sourceOfChanges.get(CLIENT_NOTES_KEY) != null) {
			clientNotes = sourceOfChanges.get(CLIENT_NOTES_KEY);

			mainFrame.setClientNotesText(clientNotes); // restoring old value

			persist.setHostNotes(client, clientNotes);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_NOTES_KEY, clientNotes);
		}

		if ((sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY) != null)
				&& !(sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY).trim()).equals("")) {
			clientMacAddress = (sourceOfChanges.get(CLIENT_MAC_ADRESS_KEY)).trim();

			int col = findCol(selectionPanel,
					Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientMacAddress, row, col);

			mainFrame.setClientMacAddress(clientMacAddress); // restoring old value

			persist.setMacAddress(client, clientMacAddress);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_MAC_ADRESS_KEY, clientMacAddress);
		}

		if ((sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY) != null)
				&& !(sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY).trim()).equals("")) {
			clientIpAddress = (sourceOfChanges.get(CLIENT_IP_ADDRESS_KEY)).trim();

			int col = findCol(selectionPanel,
					Configed.getResourceValue("ConfigedMain.pclistTableModel.clientIPAddress"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientIpAddress, row, col);

			mainFrame.setClientIpAddress(clientIpAddress); // restoring old value

			persist.setIpAddress(client, clientIpAddress);

			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_IP_ADDRESS_KEY, clientIpAddress);
		}

		if (sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY) != null) {
			boolean shutdownInstall = false;

			if (sourceOfChanges.get(CLIENT_SHUTDOWN_INSTALL_KEY).equals("true")) {
				shutdownInstall = true;
			}

			int col = findCol(selectionPanel, Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL));

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(shutdownInstall, row, col);

			persist.configureInstallByShutdown(client, shutdownInstall);
			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_SHUTDOWN_INSTALL_KEY, shutdownInstall);
		}

		if (sourceOfChanges.get(CLIENT_UEFI_BOOT_KEY) != null) {
			boolean uefiboot = false;

			if (sourceOfChanges.get(CLIENT_UEFI_BOOT_KEY).equals("true")) {
				uefiboot = true;
			}

			int col = findCol(selectionPanel, Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL));

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(uefiboot, row, col);

			persist.configureUefiBoot(client, uefiboot);
			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_UEFI_BOOT_KEY, uefiboot);
		}

		if (sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY) != null) {
			boolean wanStandard = false;

			if (sourceOfChanges.get(CLIENT_WAN_CONFIG_KEY).equals("true")) {
				wanStandard = true;
			}

			int col = findCol(selectionPanel, Configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL));

			Logging.info(this, "showAndSave found col " + col);

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(wanStandard, row, col);

			if (!(persist.setWANConfigs(client, wanStandard)))
				Logging.error(this, "wan settings could not be set");
			persist.getHostInfoCollections().updateLocalHostInfo(client, CLIENT_WAN_CONFIG_KEY, wanStandard);
		}

	}

	@Override
	public String toString() {
		return "(" + clientName + ";" + depotOfClient + ";" + clientDescription + ";" + clientInventoryNumber + ";"
				+ clientOneTimePassword + ";" + clientNotes + ";" + clientMacAddress + ";" + clientIpAddress + ";"
				+ lastSeen + ";" + created + ";" + clientUefiBoot + ";" + clientWanConfig + ";" + clientShutdownInstall
				+ ")";
	}

	public void initialize() {
		depotOfClient = "";
		clientDescription = "";
		clientInventoryNumber = "";
		clientOneTimePassword = "";
		clientNotes = "";

		clientMacAddress = "";
		lastSeen = "";
		created = "";
		clientName = "";
		hostKey = "";

		hostType = "";
		clientIpAddress = "";
		clientUefiBoot = false;
		clientWanConfig = false;
		clientSessionInfo = "";

		clientConnected = false;

		clientShutdownInstall = false;
	}

}
