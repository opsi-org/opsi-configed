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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsComponent;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.SeparatedDocument;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ClientInfoPanel extends JPanel {
	private JLabel labelDeviceTypeIcon;
	private JTextArea jTextAreaVendorModel;

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

		jTextFieldDescription = new JTextField();
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.getDocument().addDocumentListener(
				SwingUtils.onDocumentChange(() -> dataChange(jTextFieldDescription, HostInfo.CLIENT_DESCRIPTION_KEY)));

		jTextFieldInventoryNumber = new JTextField();
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.getDocument().addDocumentListener(SwingUtils
				.onDocumentChange(() -> dataChange(jTextFieldInventoryNumber, HostInfo.CLIENT_INVENTORY_NUMBER_KEY)));

		jTextAreaNotes = new JTextArea();

		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);

		jTextAreaNotes.getDocument().addDocumentListener(
				SwingUtils.onDocumentChange(() -> dataChange(jTextAreaNotes, HostInfo.CLIENT_NOTES_KEY)));

		systemUUIDField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36,
				Character.MIN_VALUE, 36, true), "", 36);
		systemUUIDField.getDocument().addDocumentListener(
				SwingUtils.onDocumentChange(() -> dataChange(systemUUIDField, HostInfo.CLIENT_SYSTEM_UUID_KEY)));

		macAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':',
				2, true), "", 17);

		macAddressField.getDocument().addDocumentListener(
				SwingUtils.onDocumentChange(() -> dataChange(macAddressField, HostInfo.CLIENT_MAC_ADDRESS_KEY)));

		ipAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' },
				28, Character.MIN_VALUE, 4, false), "", 24);
		ipAddressField.getDocument().addDocumentListener(
				SwingUtils.onDocumentChange(() -> dataChange(ipAddressField, HostInfo.CLIENT_IP_ADDRESS_KEY)));

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
		checkBoxHealthCheckActive.setVisible(Boolean.TRUE.equals(persistenceController.getDataServices().host
				.getHostDisplayFields().get(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)));

		openHealthCheckSettingsDialogButton = new JButton(Icons.getIntellijIcon("settings"));
		openHealthCheckSettingsDialogButton
				.setToolTipText(Configed.getResourceValue("HealthCheckSettingsDialog.title"));
		openHealthCheckSettingsDialogButton.setVisible(Boolean.TRUE.equals(persistenceController.getDataServices().host
				.getHostDisplayFields().get(HostInfo.CLIENT_HEALTH_CHECK_ACTIVE_DISPLAY_FIELD_LABEL)));
		openHealthCheckSettingsDialogButton
				.addActionListener(e -> new HealthCheckSettingsComponent().showHealthCheckSettings(configedMain,
						configedMain.getSelectedClients(), checkBoxHealthCheckActive.getState()));
		openHealthCheckSettingsDialogButton
				.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		checkBoxInstallByShutdown = new FlatTriStateCheckBox(
				Configed.getResourceValue("NewClientDialog.installByShutdown"));
		checkBoxInstallByShutdown.setAllowIndeterminate(false);
		checkBoxInstallByShutdown.addActionListener(event -> installByShutdownAction());
		checkBoxInstallByShutdown.setFocusable(false);

		updateClientCheckboxText();

		jTextFieldOneTimePassword = new JTextField();
		jTextFieldOneTimePassword.getDocument().addDocumentListener(SwingUtils
				.onDocumentChange(() -> dataChange(jTextFieldOneTimePassword, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY)));

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
		setLayout(new MigLayout("insets 0, wrap 1", "[grow, fill, 0:0]", "[]0"));

		final int MIN = Globals.MIN_GAP_SIZE;
		final int GAP = Globals.GAP_SIZE;

		add(labelClientOSIcon, "split 2, gapright " + MIN);
		add(jTextFieldClientID, "growx, gapbottom " + MIN);

		add(jTextFieldClientOS, "growx, gapbottom " + MIN);

		add(labelDeviceTypeIcon, "split 2, gapright " + MIN);
		add(jTextFieldDeviceType, "growx");

		add(scrollpaneVendorModel, "growx, hmin 50, gapbottom " + MIN);

		add(SwingUtils.createBoldLabel("description"), "gaptop " + GAP);
		add(jTextFieldDescription, "growx");

		add(SwingUtils.createBoldLabel("ConfigedMain.pclistTableModel.clientInventoryNumber"), "gaptop " + GAP);
		add(jTextFieldInventoryNumber, "growx");

		add(SwingUtils.createBoldLabel("ConfigedMain.pclistTableModel.systemUUID"), "gaptop " + GAP);
		add(systemUUIDField, "growx");

		add(SwingUtils.createBoldLabel("ConfigedMain.pclistTableModel.clientHardwareAddress"), "gaptop " + GAP);
		add(macAddressField, "growx");

		add(SwingUtils.createBoldLabel("ipAddress"), "gaptop " + GAP);
		add(ipAddressField, "growx");

		add(checkBoxInstallByShutdown, "gaptop " + GAP);

		add(checkBoxUEFIBoot);

		add(checkBoxWANConfig);

		add(checkBoxHealthCheckActive, "split 2, gapright " + GAP);
		add(openHealthCheckSettingsDialogButton);

		add(SwingUtils.createBoldLabel("ConfigedMain.pclistTableModel.oneTimePassword"), "gaptop " + GAP);
		add(jTextFieldOneTimePassword, "growx");

		add(SwingUtils.createBoldLabel("opsi-host-key"), "gaptop " + GAP);
		add(hostKeyField, "growx");

		add(SwingUtils.createBoldLabel("ConfigedMain.pclistTableModel.notes"), "gaptop " + GAP);
		add(new JScrollPane(jTextAreaNotes, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), "grow, pushy");

		setMinimumSize(new Dimension());
	}

	public void setClientDescriptionText(String s) {
		changeData(() -> jTextFieldDescription.setText(s));
	}

	public void setClientInventoryNumberText(String s) {
		changeData(() -> jTextFieldInventoryNumber.setText(s));
	}

	public void setClientOneTimePasswordText(String s) {
		changeData(() -> jTextFieldOneTimePassword.setText(s));
	}

	public void setClientNotesText(String s) {
		changeData(() -> jTextAreaNotes.setText(s));
	}

	public void setClientMacAddress(String s) {
		changeData(() -> macAddressField.setText(s));
	}

	public void setClientSystemUUID(String s) {
		changeData(() -> systemUUIDField.setText(s));
	}

	public void setClientIpAddress(String s) {
		changeData(() -> ipAddressField.setText(s));
	}

	public void setClientOS(String s) {
		changeData(() -> jTextFieldClientOS.setText(s));
	}

	private void changeData(Runnable changeDataAction) {
		dataAreChangedProgramatically = true;
		changeDataAction.run();
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
		changeData(() -> checkBoxHealthCheckActive.setChecked(value));
	}

	public void setClientPlatform(String value) {
		changeData(() -> labelClientOSIcon.setIcon(Utils.determineIconBasedOnPlatform(value, 24)));
	}

	public void setUefiBoot(Boolean uefiBoot) {
		Logging.info(this, "setUefiBoot ", uefiBoot);
		changeData(() -> checkBoxUEFIBoot.setChecked(uefiBoot));
	}

	public void setWANConfig(Boolean value) {
		Logging.info(this, "setWANConfig ", value);
		changeData(() -> checkBoxWANConfig.setChecked(value));
	}

	public void setShutdownInstall(Boolean value) {
		Logging.info(this, "setShutdownInstall ", value);
		changeData(() -> checkBoxInstallByShutdown.setChecked(value));
	}

	public void setOpsiHostKey(String s) {
		Logging.info(this, "setOpsiHostKey ", s);
		hostKeyField.setText(s);
	}

	public void setClientID(String s) {
		jTextFieldClientID.setText(s);
	}

	public void updateClientCheckboxText() {
		if (persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.VPN)) {
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

	private void dataChange(JTextComponent editorField, String key) {
		Logging.debug(this, "reactToClientDataChange, dataAreChangedProgramatically: ", dataAreChangedProgramatically);

		if (!dataAreChangedProgramatically && configedMain.getSelectedClients().size() == 1) {
			applyChanges(editorField, key);
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
		boolean writingAllowed = !persistenceController.getDataServices().userRoles.isGlobalReadOnly();

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
				&& persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.VPN));
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
}
