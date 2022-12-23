package de.uib.configed.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uib.configed.configed;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;

public class HostInfo {
	static int callCounter = 0;
	private static int numberOfInstances = 0; // an AtomicInteger would be threadsafe
	private final int instanceNumber;
	public static HashMap<String, Integer> ID2InstanceNumber;

	protected String depotOfClient;
	protected String clientDescription;;
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
	public static final String depotOfClientKEY = "depotId";
	public static final String clientDescriptionKEY = "description";
	public static final String clientInventoryNumberKEY = "inventoryNumber";
	public static final String clientOneTimePasswordKEY = "oneTimePassword";
	public static final String clientNotesKEY = "notes";
	public static final String clientMacAddressKEY = "hardwareAddress";
	public static final String lastSeenKEY = "lastSeen";
	public static final String createdKEY = "created";
	public static final String hostnameKEY = "id";
	public static final String hostKeyKEY = "opsiHostKey";
	public static final String hostTypeKEY = "type";
	public static final String clientIpAddressKEY = "ipAddress";
	public static final String clientUefiBootKEY = "uefiBoot";
	public static final String clientWanConfigKEY = "wanConfig";
	public static final String clientSessionInfoKEY = "sessionInfo";
	public static final String clientConnectedKEY = "clientConnected";
	public static final String clientShutdownInstallKEY = "clientShutdownInstall";
	public static final String depotWorkbenchKEY = "workbenchLocalUrl";
	// public static final String hostIdKEY = "hostId";

	// ---
	public static final String depotOfClient_DISPLAY_FIELD_LABEL = "depotId";
	public static final String clientDescription_DISPLAY_FIELD_LABEL = "clientDescription";
	public static final String clientInventoryNumber_DISPLAY_FIELD_LABEL = "clientInventoryNumber";
	public static final String clientOneTimePassword_DISPLAY_FIELD_LABEL = "clientOneTimePassword";
	public static final String clientNotes_DISPLAY_FIELD_LABEL = "notes";

	public static final String clientMacAddress_DISPLAY_FIELD_LABEL = "clientHardwareAddress";
	public static final String lastSeen_DISPLAY_FIELD_LABEL = "clientLastSeen";
	public static final String created_DISPLAY_FIELD_LABEL = "clientCreated";
	public static final String hostname_DISPLAY_FIELD_LABEL = "clientName";
	public static final String hostKey_DISPLAY_FIELD_LABEL = "opsiHostKey";

	public static final String hostType_DISPLAY_FIELD_LABEL = "hostType";
	public static final String clientIpAddress_DISPLAY_FIELD_LABEL = "clientIPAddress";
	public static final String clientUefiBoot_DISPLAY_FIELD_LABEL = "UEFIboot";
	public static final String clientWanConfig_DISPLAY_FIELD_LABEL = "WANmode";
	public static final String clientSessionInfo_DISPLAY_FIELD_LABEL = "clientSessionInfo";

	public static final String clientConnected_DISPLAY_FIELD_LABEL = "clientConnected";
	public static final String clientInstallByShutdown_DISPLAY_FIELD_LABEL = "installByShutdown";
	// ---

	public static final LinkedList<String> ORDERING_DISPLAY_FIELDS;
	static {
		ORDERING_DISPLAY_FIELDS = new LinkedList<>();

		ORDERING_DISPLAY_FIELDS.add(hostname_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientDescription_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientInventoryNumber_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientConnected_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(lastSeen_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientWanConfig_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientIpAddress_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientMacAddress_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientUefiBoot_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientInstallByShutdown_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(created_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(clientSessionInfo_DISPLAY_FIELD_LABEL);
		ORDERING_DISPLAY_FIELDS.add(depotOfClient_DISPLAY_FIELD_LABEL);

	}

	// --

	public static final String isMasterDepotKEY = "isMasterDepot";

	public static final String hostTypeVALUE_OpsiConfigserver = "OpsiConfigserver";
	public static final String hostTypeVALUE_OpsiDepotserver = "OpsiDepotserver";
	public static final String hostTypeVALUE_OpsiClient = "OpsiClient";

	public static final String hostSubClassTag_OpsiClientPrototype = "OpsiPrototype";

	static final String notLegalChars0 = ",:!@#$%^&',(){} ";
	static final Set<Character> notLegalChars = new HashSet<>();
	static {
		for (int i = 0; i < notLegalChars0.length(); i++) {
			notLegalChars.add(notLegalChars0.charAt(i));
		}
	}

	public static String checkADNamingConvention(String proposal)
	// https://support.microsoft.com/en-us/kb/909264
	{

		// logging.debug("checkADNamingConvention");
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
			String found = "";
			for (int i = 0; i < proposal.length(); i++) {
				if (notLegalChars.contains(proposal.charAt(i))) {
					found = found + proposal.charAt(i);
				}
			}
			if (found.length() > 0) {
				hintMessage = "not allowed character(s)>>>  '" + found + "'";
				result = false;
			}
		}

		// logging.debug( hintMessage );

		return hintMessage;

	}

	public static void resetInstancesCount() {
		numberOfInstances = 0;
		ID2InstanceNumber = new HashMap<>();
	}

	public static int getInstancesCount() {
		return numberOfInstances;
	}

	public static int incAndGetInstancesCount() {
		numberOfInstances++;
		return numberOfInstances;
	}

	public Integer getInstanceNumber(String key) {
		return ID2InstanceNumber.get(key);
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

	public HashMap<String, Object> getDisplayRowMap0() {
		HashMap<String, Object> unordered = new HashMap<>();

		unordered.put(hostname_DISPLAY_FIELD_LABEL, clientName);
		unordered.put(clientDescription_DISPLAY_FIELD_LABEL, clientDescription);
		unordered.put(clientInventoryNumber_DISPLAY_FIELD_LABEL, clientInventoryNumber);
		unordered.put(clientConnected_DISPLAY_FIELD_LABEL, clientConnected);
		unordered.put(lastSeen_DISPLAY_FIELD_LABEL, lastSeen);

		unordered.put(clientWanConfig_DISPLAY_FIELD_LABEL, clientWanConfig);
		unordered.put(clientIpAddress_DISPLAY_FIELD_LABEL, clientIpAddress);
		unordered.put(clientMacAddress_DISPLAY_FIELD_LABEL, clientMacAddress);
		unordered.put(clientUefiBoot_DISPLAY_FIELD_LABEL, clientUefiBoot);
		unordered.put(clientInstallByShutdown_DISPLAY_FIELD_LABEL, clientShutdownInstall);

		unordered.put(created_DISPLAY_FIELD_LABEL, created);
		unordered.put(clientSessionInfo_DISPLAY_FIELD_LABEL, clientSessionInfo);
		unordered.put(depotOfClient_DISPLAY_FIELD_LABEL, depotOfClient);
		unordered.put(clientConnectedKEY, clientConnected);

		// unordered.put( clientShutdownInstallKEY, clientShutdownInstall );

		logging.debug(this, "getMap clientName " + clientName + " : " + unordered);

		return unordered;
	}

	public HashMap<String, Object> getDisplayRowMap() {
		HashMap<String, Object> unordered = new HashMap<>(getDisplayRowMap0());
		unordered.put(hostname_DISPLAY_FIELD_LABEL, clientName);
		return unordered;
	}

	public HashMap<String, Object> getMap() {
		HashMap<String, Object> unordered = new HashMap<>();

		unordered.put(depotOfClientKEY, depotOfClient);
		unordered.put(clientDescriptionKEY, clientDescription);
		unordered.put(clientInventoryNumberKEY, clientInventoryNumber);
		unordered.put(clientOneTimePasswordKEY, clientOneTimePassword);
		unordered.put(clientNotesKEY, clientNotes);

		unordered.put(clientMacAddressKEY, clientMacAddress);
		unordered.put(lastSeenKEY, lastSeen);
		unordered.put(createdKEY, created);
		unordered.put(hostnameKEY, clientName);
		unordered.put(hostKeyKEY, hostKey);

		unordered.put(hostTypeKEY, hostType);
		unordered.put(clientIpAddressKEY, clientIpAddress);
		unordered.put(clientUefiBootKEY, clientUefiBoot);
		unordered.put(clientWanConfigKEY, clientWanConfig);
		unordered.put(clientSessionInfoKEY, clientSessionInfo);

		unordered.put(clientConnectedKEY, clientConnected);
		unordered.put(clientShutdownInstallKEY, clientShutdownInstall);

		// unordered.put( clientShutdownInstallKEY, clientShutdownInstall );

		logging.debug(this, "getMap clientName " + clientName);

		return unordered;
	}

	public void put(String key, Object value) {
		switch (key) {
		case depotOfClientKEY: {
			depotOfClient = "" + value;
			break;
		}
		case clientDescriptionKEY: {
			clientDescription = "" + value;
			break;
		}
		case clientInventoryNumberKEY: {
			clientInventoryNumber = "" + value;
			break;
		}
		case clientNotesKEY: {
			clientNotes = "" + value;
			break;
		}
		case clientOneTimePasswordKEY: {
			clientOneTimePassword = "" + value;
			break;
		}
		case clientMacAddressKEY: {
			clientMacAddress = "" + value;
			break;
		}
		case hostKeyKEY: {
			hostKey = "" + value;
			break;
		}
		case createdKEY: {
			created = "" + value;
			break;
		}
		case lastSeenKEY: {
			lastSeen = "" + value;
			break;
		}
		case clientUefiBootKEY: {
			clientUefiBoot = (Boolean) value;
			break;
		}
		case clientWanConfigKEY: {
			clientWanConfig = (Boolean) value;
			break;
		}
		case clientShutdownInstallKEY: {
			clientShutdownInstall = (Boolean) value;
			break;
		}
		default: {
			logging.warning(this, "key " + key + " not expected");
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
		// logging.info(this, "produceFrom " + pcInfo);
		// shows pckey

		// Map pcInfos = persist.getMapOfPCInfoMaps();
		// Map pcInfo = (Map) pcInfos.get(selectedClients[0]);

		if (pcInfo == null) {
			initialize();
			return;
		}

		// encodeStringFromService is just returning the given value but was used for
		// switching an encoding
		clientDescription = showValue(configed.encodeStringFromService((String) pcInfo.get(clientDescriptionKEY)));
		clientInventoryNumber = showValue(
				configed.encodeStringFromService((String) pcInfo.get(clientInventoryNumberKEY)));
		clientNotes = showValue(configed.encodeStringFromService((String) pcInfo.get(clientNotesKEY)));
		clientOneTimePassword = showValue(
				configed.encodeStringFromService((String) pcInfo.get(clientOneTimePasswordKEY)));
		clientMacAddress = showValue((String) pcInfo.get(clientMacAddressKEY));
		clientIpAddress = showValue((String) pcInfo.get(clientIpAddressKEY));
		hostKey = showValue((String) pcInfo.get(hostKeyKEY));
		clientName = showValue((String) pcInfo.get(hostnameKEY));
		hostType = showValue((String) pcInfo.get(hostTypeKEY));
		created = showValue((String) pcInfo.get(createdKEY));
		lastSeen = showValue((String) pcInfo.get(lastSeenKEY));

		depotOfClient = showValue((String) pcInfo.get(depotOfClientKEY));

		clientUefiBoot = showValue((Boolean) pcInfo.get(clientUefiBootKEY));
		clientWanConfig = showValue((Boolean) pcInfo.get(clientWanConfigKEY));

		clientShutdownInstall = showValue((Boolean) pcInfo.get(clientShutdownInstallKEY));
	}

	public HostInfo combineWith(HostInfo secondInfo) {

		if (secondInfo == null)
			return this;

		// save values which could be mixed
		Boolean clientWanConfig_save = clientWanConfig;
		Boolean clientUefiBoot_save = clientUefiBoot;
		Boolean clientShutdownInstall_save = clientShutdownInstall;

		initialize(); // empty everything

		if (clientWanConfig_save != secondInfo.clientWanConfig)
			clientWanConfig = null;
		else
			clientWanConfig = clientWanConfig_save;

		if (clientUefiBoot_save != secondInfo.clientUefiBoot)
			clientUefiBoot = null;
		else
			clientUefiBoot = clientUefiBoot_save;

		if (clientShutdownInstall_save != secondInfo.clientShutdownInstall)
			clientShutdownInstall = null;
		else
			clientShutdownInstall = clientShutdownInstall_save;

		return this;

	}

	public void setMap(Map<String, Object> infoMap) {
		logging.debug(this, "setMap " + infoMap);
		initialize();

		if (infoMap == null) {
			return;
		}

		clientUefiBoot = showValue(((Boolean) infoMap.get(clientUefiBootKEY)));
		clientWanConfig = showValue(((Boolean) infoMap.get(clientWanConfigKEY)));
		clientShutdownInstall = showValue((Boolean) infoMap.get(clientShutdownInstallKEY));

		clientDescription = showValue("" + infoMap.get(clientDescriptionKEY));
		clientInventoryNumber = showValue("" + infoMap.get(clientInventoryNumberKEY));
		clientNotes = showValue("" + infoMap.get(clientNotesKEY));
		clientOneTimePassword = showValue("" + infoMap.get(clientOneTimePasswordKEY));
		clientMacAddress = showValue("" + infoMap.get(clientMacAddressKEY));
		clientIpAddress = showValue("" + infoMap.get(clientIpAddressKEY));
		hostKey = showValue("" + infoMap.get(hostKeyKEY));
		clientName = showValue("" + infoMap.get(hostnameKEY));
		created = showValue("" + infoMap.get(createdKEY));
		lastSeen = showValue("" + infoMap.get(lastSeenKEY));
	}

	/*
	 * public void getInfo(Map<String, Object> pcInfo)
	 * {
	 * 
	 * logging.info(this, "getInfo " + pcInfo);
	 * 
	 * //Map pcInfos = persist.getMapOfPCInfoMaps();
	 * //Map pcInfo = (Map) pcInfos.get(selectedClients[0]);
	 * 
	 * if (pcInfo == null)
	 * {
	 * initialize();
	 * return;
	 * }
	 * 
	 * clientDescription = configed.encodeStringFromService ((String)
	 * pcInfo.get(clientDescriptionKEY));
	 * if (clientDescription == null)
	 * clientDescription = "";
	 * 
	 * clientInventoryNumber = configed.encodeStringFromService ((String)
	 * pcInfo.get(clientInventoryNumberKEY));
	 * if (clientInventoryNumber == null)
	 * clientInventoryNumber = "";
	 * 
	 * clientNotes = configed.encodeStringFromService ( (String)
	 * pcInfo.get(clientNotesKEY) );
	 * if (clientNotes == null)
	 * clientNotes = "";
	 * 
	 * clientOneTimePassword = configed.encodeStringFromService ( (String)
	 * pcInfo.get(clientOneTimePasswordKEY) );
	 * if (clientOneTimePassword == null)
	 * clientOneTimePassword = "";
	 * 
	 * clientMacAddress = (String) pcInfo.get(clientMacAddressKEY) ;
	 * if (clientMacAddress == null)
	 * clientMacAddress = "";
	 * 
	 * clientIpAddress = (String) pcInfo.get(clientIpAddressKEY) ;
	 * if (clientIpAddress == null)
	 * clientIpAddress = "";
	 * 
	 * 
	 * hostKey = (String) pcInfo.get(hostKeyKEY);
	 * 
	 * clientName = (String) pcInfo.get(hostnameKEY);
	 * 
	 * created = (String) pcInfo.get(createdKEY);
	 * 
	 * lastSeen = (String) pcInfo.get(lastSeenKEY);
	 * if (lastSeen != null)
	 * {
	 * String[] ls = lastSeen.split("");
	 * if (ls.length >= 15)
	 * lastSeen = ls[1]+ls[2]+ls[3]+ls[4]+'-'+ls[5]+ls[6]+'-'+ls[7]+ls[8]+'
	 * '+ls[9]+ls[10]+':'+ls[11]+ls[12]+':'+ls[13]+ls[14];
	 * }
	 * else
	 * {
	 * lastSeen = "";
	 * }
	 * 
	 * clientUefiBoot = (Boolean)
	 * pcInfo.get(PersistenceController.HOST_KEY_UEFI_BOOT);
	 * if (clientUefiBoot == null)
	 * clientUefiBoot = false;
	 * }
	 */

	private int findCol(JTableSelectionPanel selectionPanel, String colName) {
		return selectionPanel.getTableModel().findColumn(colName
		// configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription")
		);
	}

	private int findRow(JTableSelectionPanel selectionPanel, String client) {
		return selectionPanel.findModelRowFromValue(client, 0);
	}

	public void resetGui(MainFrame mainFrame) {
		logging.info(this, "resetGui for " + toString());
		mainFrame.setClientDescriptionText(clientDescription);
		mainFrame.setClientInventoryNumberText(clientInventoryNumber);
		mainFrame.setClientNotesText(clientNotes);
		mainFrame.setClientMacAddress(clientMacAddress);
		mainFrame.setClientIpAddress(clientIpAddress);
		mainFrame.setClientOneTimePasswordText(clientOneTimePassword);
		mainFrame.setUefiBoot(clientUefiBoot);
		mainFrame.setWANConfig(clientWanConfig);
		mainFrame.setShutdownInstall(clientShutdownInstall);

		/*
		 * //testcode
		 * callCounter++;
		 * String partOfHostKey = "";
		 * if (hostKey != null && hostKey.length()>0)
		 * {
		 * partOfHostKey = hostKey.substring(0,1);
		 * logging.info(this, "++++++ " + clientName + " host key " + hostKey);
		 * }
		 * else
		 * logging.info(this, "++++++ " + clientName + " no host key " + hostKey);
		 * mainFrame.setOpsiHostKey(callCounter + ":" + partOfHostKey);
		 */

		mainFrame.setOpsiHostKey(hostKey);
	}

	public void showAndSaveInternally(JTableSelectionPanel selectionPanel, MainFrame mainFrame,
			PersistenceController persist, String client, Map<String, String> sourceOfChanges) {
		if (client == null || client.equals("")) {
			logging.warning(this, "show and save: no hostId given: " + sourceOfChanges);
			return;
		}

		logging.info(this, "showAndSave client, source " + client + ", " + sourceOfChanges);
		int row = findRow(selectionPanel, client);

		if (sourceOfChanges == null)
			return;

		if (sourceOfChanges.get(clientDescriptionKEY) != null) {
			clientDescription = sourceOfChanges.get(clientDescriptionKEY);
			int col = findCol(selectionPanel,
					configed.getResourceValue("ConfigedMain.pclistTableModel.clientDescription"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientDescription, row, col);

			mainFrame.setClientDescriptionText(clientDescription); // restoring old value

			persist.setHostDescription(client, configed.encodeStringForService(clientDescription));

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientDescriptionKEY, clientDescription);
		}

		if (sourceOfChanges.get(clientInventoryNumberKEY) != null) {
			clientInventoryNumber = sourceOfChanges.get(clientInventoryNumberKEY);

			int col = findCol(selectionPanel,
					configed.getResourceValue("ConfigedMain.pclistTableModel.clientInventoryNumber"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientInventoryNumber, row, col);

			mainFrame.setClientInventoryNumberText(clientInventoryNumber); // restoring old value

			persist.setClientInventoryNumber(client, configed.encodeStringForService(clientInventoryNumber));

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientInventoryNumberKEY,
					clientInventoryNumber);
		}

		if (sourceOfChanges.get(clientOneTimePasswordKEY) != null) {
			clientOneTimePassword = sourceOfChanges.get(clientOneTimePasswordKEY);
			mainFrame.setClientOneTimePasswordText(clientOneTimePassword); // restoring old value

			persist.setClientOneTimePassword(client, configed.encodeStringForService(clientOneTimePassword));

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientOneTimePasswordKEY,
					clientOneTimePassword);
		}

		if (sourceOfChanges.get(clientNotesKEY) != null) {
			clientNotes = sourceOfChanges.get(clientNotesKEY);

			mainFrame.setClientNotesText(clientNotes); // restoring old value

			persist.setHostNotes(client, configed.encodeStringForService(clientNotes));

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientNotesKEY, clientNotes);
		}

		if ((sourceOfChanges.get(clientMacAddressKEY) != null)
				&& !(sourceOfChanges.get(clientMacAddressKEY).trim()).equals("")) {
			clientMacAddress = (sourceOfChanges.get(clientMacAddressKEY)).trim();

			int col = findCol(selectionPanel,
					configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientMacAddress, row, col);

			mainFrame.setClientMacAddress(clientMacAddress);; // restoring old value

			persist.setMacAddress(client, clientMacAddress);

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientMacAddressKEY, clientMacAddress);
		}

		if ((sourceOfChanges.get(clientIpAddressKEY) != null)
				&& !(sourceOfChanges.get(clientIpAddressKEY).trim()).equals("")) {
			clientIpAddress = (sourceOfChanges.get(clientIpAddressKEY)).trim();

			int col = findCol(selectionPanel,
					configed.getResourceValue("ConfigedMain.pclistTableModel.clientIPAddress"));
			if (col > -1)
				selectionPanel.getTableModel().setValueAt(clientIpAddress, row, col);

			mainFrame.setClientIpAddress(clientIpAddress);; // restoring old value

			persist.setIpAddress(client, clientIpAddress);

			persist.getHostInfoCollections().updateLocalHostInfo(client, clientIpAddressKEY, clientIpAddress);
		}

		if (sourceOfChanges.get(clientShutdownInstallKEY) != null) {
			boolean shutdownInstall = false;

			if (sourceOfChanges.get(clientShutdownInstallKEY).equals("true")) {
				shutdownInstall = true;
			}

			int col = findCol(selectionPanel, configed.getResourceValue(
					"ConfigedMain.pclistTableModel." + HostInfo.clientInstallByShutdown_DISPLAY_FIELD_LABEL));

			/*
			 * Vector<String> columns = new Vector<>();
			 * for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++)
			 * {
			 * columns.add( selectionPanel.getTableModel().getColumnName( i ) );
			 * }
			 * logging.info(this, "showAndSave columns are " + columns);
			 * 
			 * logging.info(this, "showAndSave found col " + col);
			 */

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(shutdownInstall, row, col);

			persist.configureInstallByShutdown(client, shutdownInstall);
			persist.getHostInfoCollections().updateLocalHostInfo(client, clientShutdownInstallKEY, shutdownInstall);
		}

		if (sourceOfChanges.get(clientUefiBootKEY) != null) {
			boolean uefiboot = false;

			if (sourceOfChanges.get(clientUefiBootKEY).equals("true")) {
				uefiboot = true;
			}

			int col = findCol(selectionPanel, configed
					.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL));

			/*
			 * Vector<String> columns = new Vector<>();
			 * for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++)
			 * {
			 * columns.add( selectionPanel.getTableModel().getColumnName( i ) );
			 * }
			 * logging.info(this, "showAndSave columns are " + columns);
			 * 
			 * logging.info(this, "showAndSave found col " + col);
			 */

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(uefiboot, row, col);

			persist.configureUefiBoot(client, uefiboot);
			persist.getHostInfoCollections().updateLocalHostInfo(client, clientUefiBootKEY, uefiboot);
		}

		if (sourceOfChanges.get(clientWanConfigKEY) != null) {
			boolean wanStandard = false;

			if (sourceOfChanges.get(clientWanConfigKEY).equals("true")) {
				wanStandard = true;
			}

			int col = findCol(selectionPanel, configed
					.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL));

			/*
			 * Vector<String> columns = new Vector<>();
			 * for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++)
			 * {
			 * columns.add( selectionPanel.getTableModel().getColumnName( i ) );
			 * }
			 * logging.info(this, "showAndSave columns are " + columns);
			 */
			logging.info(this, "showAndSave found col " + col);

			if (col > -1)
				// write it into the visible table
				selectionPanel.getTableModel().setValueAt(wanStandard, row, col);

			if (!(persist.setWANConfigs(client, wanStandard)))
				logging.error(this, "wan settings could not be set");
			persist.getHostInfoCollections().updateLocalHostInfo(client, clientWanConfigKEY, wanStandard);
		}

		// if ( sourceOfChanges.get(clientShutdownInstallKEY) != null )
		// {
		// String shutdownInstall = "off";
		// boolean boolShutdownInstall = false;

		// if ( sourceOfChanges.get(clientShutdownInstallKEY).equals("true") )
		// {
		// shutdownInstall = "on";
		// boolShutdownInstall = true;
		// }
		// // System.out.print("");logging.debug("");logging.debug("");
		// // logging.debug("HostInfo showAndSave shutdownInstall " +
		
		// // logging.debug("");logging.debug("");logging.debug("");

		// String product = "opsi-client-agent";
		// persist.setCommonProductPropertyValue( new HashSet<>(Arrays.asList(client)),
		// product, "on_shutdown_install" , Arrays.asList(shutdownInstall) );
		// // Set<String> clientNames, String productName, String propertyName,
		// List<String> values
		// Map<String, String> productValues = new HashMap<>();
		// productValues.put("actionRequest", "setup");

		// persist.updateProductOnClient(
		// client,
		// product,
		// OpsiPackage.TYPE_LOCALBOOT,
		// productValues
		

		// persist.updateProductOnClients();
		// persist.getHostInfoCollections().updateLocalHostInfo(client,
		// clientShutdownInstallKEY, boolShutdownInstall);
		// }
	}

	@Override
	public String toString() {
		String result = "(" + clientName + ";" + depotOfClient + ";" + clientDescription + ";" + clientInventoryNumber
				+ ";" + clientOneTimePassword + ";" + clientNotes + ";" + clientMacAddress + ";" + clientIpAddress + ";"
				+ lastSeen + ";" + created + ";" + clientUefiBoot + ";" + clientWanConfig + ";" + clientShutdownInstall
				+ ")";

		return result;
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
