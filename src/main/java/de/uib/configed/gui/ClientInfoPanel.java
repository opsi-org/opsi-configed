/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsComponent;
import de.uib.configed.gui.share.swing.SeparatedDocument;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class ClientInfoPanel extends JPanel implements DocumentListener {
	private JLabel labelClientDescription;
	private JLabel labelClientInventoryNumber;
	private JLabel labelClientNotes;
	private JLabel labelClientSystemUUID;
	private JLabel labelClientMacAddress;
	private JLabel labelClientIPAddress;
	private JLabel labelDeviceTypeIcon;
	private JTextArea jTextAreaVendorModel;
	private JLabel labelOneTimePassword;
	private JLabel labelOpsiHostKey;

	private JScrollPane scrollpaneNotes;
	private JScrollPane scrollpaneVendorModel;

	private JLabel labelClientOSIcon;
	private JTextField jTextFieldClientID;
	private JTextField jTextFieldClientOS;
	private JTextField jTextFieldDeviceType;
	private FlatTriStateCheckBox checkBoxInstallByShutdown;
	private FlatTriStateCheckBox checkBoxUEFIBoot;
	private FlatTriStateCheckBox checkBoxWANConfig;
	private FlatTriStateCheckBox checkBoxHealthCheckActive;

	private JTextField jTextFieldDescription;
	private JTextField jTextFieldInventoryNumber;
	private JTextArea jTextAreaNotes;
	private JTextField systemUUIDField;
	private JTextField macAddressField;
	private JTextField ipAddressField;
	private JTextField jTextFieldOneTimePassword;
	private FlatPasswordField hostKeyField;

	private JButton openHealthCheckSettingsDialogButton;

	private Map<String, Map<String, String>> changedClientInfos;

	// We need this flag so that the document listener is not active when
	// the data in the components are updated by the program instead of by user
	// input
	private boolean dataAreChangedProgramatically;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientInfoPanel(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initComponents();
		setupLayout();
	}

	private void initComponents() {
		labelClientOSIcon = new JLabel();
		jTextFieldClientID = createUneditableTextField();
		jTextFieldClientID.setFont(jTextFieldClientID.getFont().deriveFont(Font.BOLD).deriveFont(16.0F));

		labelClientDescription = new JLabel(Configed.getResourceValue("description"));
		labelClientDescription.setFont(labelClientDescription.getFont().deriveFont(Font.BOLD));

		labelClientInventoryNumber = new JLabel(
				Configed.getResourceValue("ConfigedMain.pclistTableModel.clientInventoryNumber"));
		labelClientInventoryNumber.setFont(labelClientInventoryNumber.getFont().deriveFont(Font.BOLD));

		labelClientNotes = new JLabel(Configed.getResourceValue("ConfigedMain.pclistTableModel.notes"));
		labelClientNotes.setFont(labelClientNotes.getFont().deriveFont(Font.BOLD));

		labelClientSystemUUID = new JLabel(Configed.getResourceValue("ConfigedMain.pclistTableModel.systemUUID"));
		labelClientSystemUUID.setFont(labelClientSystemUUID.getFont().deriveFont(Font.BOLD));

		labelClientMacAddress = new JLabel(
				Configed.getResourceValue("ConfigedMain.pclistTableModel.clientHardwareAddress"));
		labelClientMacAddress.setFont(labelClientMacAddress.getFont().deriveFont(Font.BOLD));

		labelClientIPAddress = new JLabel(Configed.getResourceValue("ipAddress"));
		labelClientIPAddress.setFont(labelClientIPAddress.getFont().deriveFont(Font.BOLD));

		jTextFieldClientOS = createUneditableTextField();
		jTextFieldClientOS.setFont(jTextFieldClientOS.getFont().deriveFont(Font.BOLD));
		jTextFieldClientOS.setToolTipText(Configed.getResourceValue("ConfigedMain.pclistTableModel.operatingSystem"));

		jTextFieldDeviceType = createUneditableTextField();
		labelDeviceTypeIcon = new JLabel();

		jTextAreaVendorModel = new JTextArea();
		jTextAreaVendorModel.setEditable(false);
		jTextAreaVendorModel.setRows(2);
		jTextAreaVendorModel.setBorder(null);

		scrollpaneVendorModel = new JScrollPane(jTextAreaVendorModel);
		scrollpaneVendorModel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpaneVendorModel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollpaneVendorModel.setBorder(null);

		labelOneTimePassword = new JLabel(Configed.getResourceValue("ConfigedMain.pclistTableModel.oneTimePassword"));
		labelOneTimePassword.setFont(labelOneTimePassword.getFont().deriveFont(Font.BOLD));

		labelOpsiHostKey = new JLabel("opsi-host-key");
		labelOpsiHostKey.setFont(labelOpsiHostKey.getFont().deriveFont(Font.BOLD));

		jTextFieldDescription = new JTextField();
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.getDocument().addDocumentListener(this);

		jTextFieldInventoryNumber = new JTextField();
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.getDocument().addDocumentListener(this);

		jTextAreaNotes = new JTextArea();

		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);

		jTextAreaNotes.getDocument().addDocumentListener(this);

		scrollpaneNotes = new JScrollPane(jTextAreaNotes);
		scrollpaneNotes.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpaneNotes.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		systemUUIDField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36,
				Character.MIN_VALUE, 36, true), "", 36);
		systemUUIDField.getDocument().addDocumentListener(this);

		macAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':',
				2, true), "", 17);

		macAddressField.getDocument().addDocumentListener(this);

		ipAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' },
				28, Character.MIN_VALUE, 4, false), "", 24);
		ipAddressField.getDocument().addDocumentListener(this);

		checkBoxUEFIBoot = new FlatTriStateCheckBox(Configed.getResourceValue("NewClientDialog.boottype"));
		checkBoxUEFIBoot.setAllowIndeterminate(false);
		checkBoxUEFIBoot.setEnabled(false);

		checkBoxWANConfig = new FlatTriStateCheckBox(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
		checkBoxWANConfig.setAllowIndeterminate(false);
		checkBoxWANConfig.addActionListener(event -> wanConfigAction());
		checkBoxWANConfig.setFocusable(false);

		checkBoxHealthCheckActive = new FlatTriStateCheckBox(
				Configed.getResourceValue("NewClientDialog.healthCheckActive"));
		checkBoxHealthCheckActive.setAllowIndeterminate(false);
		checkBoxHealthCheckActive.setFocusable(false);
		checkBoxHealthCheckActive.setEnabled(false);
		checkBoxHealthCheckActive.setVisible(Boolean.TRUE.equals(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)));

		openHealthCheckSettingsDialogButton = new JButton(Icons.getIntellijIcon("settings"));
		openHealthCheckSettingsDialogButton
				.setToolTipText(Configed.getResourceValue("HealthCheckSettingsDialog.title"));
		openHealthCheckSettingsDialogButton.setVisible(Boolean.TRUE.equals(persistenceController.getHostDataService()
				.getHostDisplayFields().get(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)));
		openHealthCheckSettingsDialogButton
				.addActionListener(e -> new HealthCheckSettingsComponent().showHealthCheckSettings(configedMain,
						configedMain.getSelectedClients(), checkBoxHealthCheckActive.getState()));
		openHealthCheckSettingsDialogButton
				.setEnabled(!persistenceController.getUserRolesConfigDataService().isGlobalReadOnly());

		checkBoxInstallByShutdown = new FlatTriStateCheckBox(
				Configed.getResourceValue("NewClientDialog.installByShutdown"));
		checkBoxInstallByShutdown.setAllowIndeterminate(false);
		checkBoxInstallByShutdown.addActionListener(event -> installByShutdownAction());
		checkBoxInstallByShutdown.setFocusable(false);

		updateClientCheckboxText();

		jTextFieldOneTimePassword = new JTextField();
		jTextFieldOneTimePassword.getDocument().addDocumentListener(this);

		hostKeyField = new FlatPasswordField();
		hostKeyField.setEditable(false);

		// This button copies the hostKey into the clipboard
		JButton jButtonCopyHostKey = new JButton(Icons.getIntellijIcon("copy"));
		jButtonCopyHostKey.setToolTipText(Configed.getResourceValue("MainFrame.copyHostKey"));
		jButtonCopyHostKey.addActionListener(event -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(new String(hostKeyField.getPassword())), null));
		hostKeyField.setTrailingComponent(jButtonCopyHostKey);
	}

	private static JTextField createUneditableTextField() {
		JTextField textField = new JTextField();
		textField.setEditable(false);
		textField.setBorder(null);
		textField.setBackground(null);
		return textField;
	}

	private void setupLayout() {
		GroupLayout layoutClientPane = new GroupLayout(this);
		setLayout(layoutClientPane);
		layoutClientPane.setHorizontalGroup(layoutClientPane.createParallelGroup()
				/////// HOST
				.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(labelClientOSIcon, 24, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jTextFieldClientID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

				/////// Operating System (long label)
				.addComponent(jTextFieldClientOS, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// DEVICE INFO (icon, vendor, model)
				.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(labelDeviceTypeIcon, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jTextFieldDeviceType, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(layoutClientPane.createSequentialGroup().addGap(25).addComponent(scrollpaneVendorModel, 0,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

				/////// DESCRIPTION
				.addComponent(labelClientDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jTextFieldDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// INVENTORY NUMBER
				.addComponent(labelClientInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jTextFieldInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// SYSTEM UUID
				.addComponent(labelClientSystemUUID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(systemUUIDField, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// MAC ADDRESS
				.addComponent(labelClientMacAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(macAddressField, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// IP ADDRESS
				.addComponent(labelClientIPAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(ipAddressField, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// INSTALL BY SHUTDOWN
				.addComponent(checkBoxInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				/////// UEFI BOOT
				.addComponent(checkBoxUEFIBoot, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				/////// WAN CONFIG
				.addComponent(checkBoxWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				/////// Health-Check active
				.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(checkBoxHealthCheckActive, 0, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(openHealthCheckSettingsDialogButton,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// ONE TIME PASSWORD
				.addComponent(labelOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(jTextFieldOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				////// opsiHostKey
				.addComponent(labelOpsiHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(hostKeyField, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// NOTES
				.addComponent(labelClientNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
				/////// HOST
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutClientPane.createParallelGroup()
						.addComponent(labelClientOSIcon, 0, Globals.DEFAULT_JLABEL_HEIGHT * 2,
								Globals.DEFAULT_JLABEL_HEIGHT * 2)
						.addComponent(jTextFieldClientID, 0, Globals.DEFAULT_JLABEL_HEIGHT * 2,
								Globals.DEFAULT_JLABEL_HEIGHT * 2))
				/////// Operating System (long label)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jTextFieldClientOS, 0, Globals.DEFAULT_JLABEL_HEIGHT, Globals.DEFAULT_JLABEL_HEIGHT)
				/////// DEVICE INFO (icon, vendor, model)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutClientPane.createParallelGroup()
						.addComponent(labelDeviceTypeIcon, 0, Globals.DEFAULT_JLABEL_HEIGHT,
								Globals.DEFAULT_JLABEL_HEIGHT)
						.addComponent(jTextFieldDeviceType, 0, Globals.DEFAULT_JLABEL_HEIGHT,
								Globals.DEFAULT_JLABEL_HEIGHT))
				.addComponent(scrollpaneVendorModel, 0, Globals.DEFAULT_JLABEL_HEIGHT * 2 + Globals.GAP_SIZE,
						Globals.DEFAULT_JLABEL_HEIGHT * 2 + Globals.GAP_SIZE)

				/////// DESCRIPTION
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// INVENTORY NUMBER
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// SYSTEM UUID
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(systemUUIDField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// MAC ADDRESS
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(macAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// IP ADDRESS
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientIPAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(ipAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				////// INSTALL BY SHUTDOWN
				.addGap(Globals.GAP_SIZE)
				.addComponent(checkBoxInstallByShutdown, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// UEFI BOOT & WAN Config
				.addComponent(checkBoxUEFIBoot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(checkBoxWANConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// Health-Check active
				.addGroup(layoutClientPane.createParallelGroup()
						.addComponent(checkBoxHealthCheckActive, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(openHealthCheckSettingsDialogButton, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// ONE TIME PASSWORD
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				////// opsiHostKey
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelOpsiHostKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(hostKeyField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// NOTES
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelClientNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		setMinimumSize(new Dimension());
	}

	public void setClientDescriptionText(String s) {
		dataAreChangedProgramatically = true;
		jTextFieldDescription.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientInventoryNumberText(String s) {
		dataAreChangedProgramatically = true;
		jTextFieldInventoryNumber.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientOneTimePasswordText(String s) {
		dataAreChangedProgramatically = true;
		jTextFieldOneTimePassword.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientNotesText(String s) {
		dataAreChangedProgramatically = true;
		jTextAreaNotes.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientMacAddress(String s) {
		dataAreChangedProgramatically = true;
		macAddressField.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientSystemUUID(String s) {
		dataAreChangedProgramatically = true;
		systemUUIDField.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientIpAddress(String s) {
		dataAreChangedProgramatically = true;
		ipAddressField.setText(s);
		dataAreChangedProgramatically = false;
	}

	public void setClientOS(String s) {
		dataAreChangedProgramatically = true;
		jTextFieldClientOS.setText(s);
		dataAreChangedProgramatically = false;
	}

	public static ImageIcon getDeviceTypeIcon(String deviceType) {
		if (deviceType == null || "<<intern:empty>>".equals(deviceType)) {
			deviceType = "";
		}

		return Utils.determineIconBasedOnDeviceType(deviceType, 20);
	}

	public static String transformDeviceType(String deviceType) {
		String deviceTypeResourceKey = deviceType == null ? "" : deviceType;
		if ("<<intern:empty>>".equals(deviceTypeResourceKey)) {
			return "";
		} else {
			return Configed.getResourceValue("ConfigedMain.pclistTableModel.deviceType." + deviceTypeResourceKey);
		}
	}

	public void setClientDeviceVendorAndModel(String vendor, String model, String deviceType) {
		dataAreChangedProgramatically = true;

		labelDeviceTypeIcon.setIcon(getDeviceTypeIcon(deviceType));

		jTextFieldDeviceType.setText(transformDeviceType(deviceType));

		jTextAreaVendorModel.setText("");
		if (vendor.isBlank() && model.isBlank()) {
			jTextAreaVendorModel.setToolTipText(null);
		} else if (vendor.isBlank()) {
			jTextAreaVendorModel.setToolTipText(Configed.getResourceValue("ConfigedMain.pclistTableModel.deviceModel"));
			jTextAreaVendorModel.append(model);
		} else if (model.isBlank()) {
			jTextAreaVendorModel
					.setToolTipText(Configed.getResourceValue("ConfigedMain.pclistTableModel.deviceVendor"));
			jTextAreaVendorModel.append(vendor);
		} else {
			jTextAreaVendorModel.setToolTipText(Configed.getResourceValue("ConfigedMain.pclistTableModel.deviceVendor")
					+ "\n" + Configed.getResourceValue("ConfigedMain.pclistTableModel.deviceModel"));
			jTextAreaVendorModel.append(vendor + "\n");
			jTextAreaVendorModel.append(model);
		}

		// Go to the beginning of the text area
		jTextAreaVendorModel.setCaretPosition(0);

		dataAreChangedProgramatically = false;
	}

	public void setClientMonitoring(Boolean value) {
		dataAreChangedProgramatically = true;
		checkBoxHealthCheckActive.setChecked(value);
		dataAreChangedProgramatically = false;
	}

	public void setClientPlatform(String value) {
		dataAreChangedProgramatically = true;
		labelClientOSIcon.setIcon(Utils.determineIconBasedOnPlatform(value, 24));
		dataAreChangedProgramatically = false;
	}

	public void setUefiBoot(Boolean uefiBoot) {
		Logging.info(this, "setUefiBoot ", uefiBoot);
		checkBoxUEFIBoot.setChecked(uefiBoot);
	}

	public void setWANConfig(Boolean value) {
		Logging.info(this, "setWANConfig ", value);
		checkBoxWANConfig.setChecked(value);
	}

	public void setShutdownInstall(Boolean value) {
		Logging.info(this, "setShutdownInstall ", value);
		checkBoxInstallByShutdown.setChecked(value);
	}

	public void setOpsiHostKey(String s) {
		Logging.info(this, "setOpsiHostKey ", s);
		hostKeyField.setText(s);
	}

	public void setClientID(String s) {
		jTextFieldClientID.setText(s);
	}

	public void updateClientCheckboxText() {
		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)) {
			checkBoxWANConfig.setText(Configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			checkBoxWANConfig.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			checkBoxWANConfig.setEnabled(false);
		}
	}

	private void wanConfigAction() {
		Logging.info(this, "actionPerformed on cbWANConfig");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, Boolean.toString(checkBoxWANConfig.isSelected()));
			ChangedDataManager.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	private void installByShutdownAction() {
		Logging.info(this, "actionPerformed on cbInstallByShutdown");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY,
					Boolean.toString(checkBoxInstallByShutdown.isSelected()));
			ChangedDataManager.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	private void reactToClientDataChange(Document document) {
		Logging.debug(this, "reactToClientDataChange, dataAreChangedProgramatically: ", dataAreChangedProgramatically);

		if (dataAreChangedProgramatically || configedMain.getSelectedClients().size() != 1) {
			return;
		}

		if (document == jTextFieldDescription.getDocument()) {
			applyChanges(jTextFieldDescription, HostInfo.CLIENT_DESCRIPTION_KEY);
		} else if (document == jTextFieldInventoryNumber.getDocument()) {
			applyChanges(jTextFieldInventoryNumber, HostInfo.CLIENT_INVENTORY_NUMBER_KEY);
		} else if (document == jTextFieldOneTimePassword.getDocument()) {
			applyChanges(jTextFieldOneTimePassword, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY);
		} else if (document == jTextAreaNotes.getDocument()) {
			applyChanges(jTextAreaNotes, HostInfo.CLIENT_NOTES_KEY);
		} else if (document == systemUUIDField.getDocument()) {
			applyChanges(systemUUIDField, HostInfo.CLIENT_SYSTEM_UUID_KEY);
		} else if (document == macAddressField.getDocument()) {
			applyChanges(macAddressField, HostInfo.CLIENT_MAC_ADRESS_KEY);
		} else if (document == ipAddressField.getDocument()) {
			applyChanges(ipAddressField, HostInfo.CLIENT_IP_ADDRESS_KEY);
		} else {
			Logging.warning(this, "unexpected source in reactToHostDataChange, document: ", document);
		}
	}

	private void applyChanges(JTextComponent editorField, String key) {
		String client = configedMain.getSelectedClients().get(0);
		Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
		changedClientInfo.put(key, editorField.getText());
		ChangedDataManager.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);

	}

	private Map<String, String> getChangedClientInfoFor(String client) {
		if (changedClientInfos == null) {
			changedClientInfos = new HashMap<>();
		}

		return changedClientInfos.computeIfAbsent(client, arg -> new HashMap<>());
	}

	public void setClientInfoEditing(boolean singleClient, boolean clientSelectionEmpty) {
		// singleClient is primarily conceived as toggle: true for single host, false
		// for multi hosts editing

		// mix with global read only flag
		boolean writingAllowed = !PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly();

		jTextFieldDescription.setEnabled(singleClient);
		jTextFieldDescription.setEditable(writingAllowed);
		jTextFieldInventoryNumber.setEnabled(singleClient);
		jTextFieldInventoryNumber.setEditable(writingAllowed);
		jTextFieldOneTimePassword.setEnabled(singleClient);
		jTextFieldOneTimePassword.setEditable(writingAllowed);
		jTextAreaNotes.setEnabled(singleClient);
		jTextAreaNotes.setEditable(writingAllowed);
		systemUUIDField.setEnabled(singleClient);
		systemUUIDField.setEditable(writingAllowed);
		macAddressField.setEnabled(singleClient);
		macAddressField.setEditable(writingAllowed);
		ipAddressField.setEnabled(singleClient);
		ipAddressField.setEditable(writingAllowed);

		checkBoxWANConfig.setEnabled(writingAllowed && !clientSelectionEmpty
				&& persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN));
		checkBoxInstallByShutdown.setEnabled(writingAllowed && !clientSelectionEmpty);

		hostKeyField.setEnabled(singleClient);

		if (singleClient) {
			jTextFieldDescription.setToolTipText(null);
			jTextFieldInventoryNumber.setToolTipText(null);
			jTextFieldOneTimePassword.setToolTipText(null);
			jTextAreaNotes.setToolTipText(null);
		} else {
			jTextFieldDescription
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword
					.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(Configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
		}
	}

	public void hideHealthCheckActiveCheckBox(boolean hide) {
		checkBoxHealthCheckActive.setVisible(hide);
		openHealthCheckSettingsDialogButton.setVisible(hide);
	}

	// DocumentListener
	@Override
	public void changedUpdate(DocumentEvent e) {
		reactToClientDataChange(e.getDocument());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		reactToClientDataChange(e.getDocument());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		reactToClientDataChange(e.getDocument());
	}
}
