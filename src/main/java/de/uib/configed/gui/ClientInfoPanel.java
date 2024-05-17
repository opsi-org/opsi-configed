/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.RevertibleTextField;
import de.uib.utils.swing.SeparatedDocument;

public class ClientInfoPanel extends JPanel implements KeyListener {
	private JLabel labelClientDescription;
	private JLabel labelClientInventoryNumber;
	private JLabel labelClientNotes;
	private JLabel labelClientSystemUUID;
	private JLabel labelClientMacAddress;
	private JLabel labelClientIPAddress;
	private JLabel labelOneTimePassword;
	private JLabel labelOpsiHostKey;

	private JScrollPane scrollpaneNotes;

	private JLabel labelClientID;
	private JCheckBox jCheckBoxInstallByShutdown;
	private JCheckBox jCheckBoxUEFIBoot;
	private JCheckBox jCheckBoxWANConfig;

	private RevertibleTextField jTextFieldDescription;
	private RevertibleTextField jTextFieldInventoryNumber;
	private JTextArea jTextAreaNotes;
	private RevertibleTextField systemUUIDField;
	private RevertibleTextField macAddressField;
	private RevertibleTextField ipAddressField;
	private RevertibleTextField jTextFieldOneTimePassword;
	private JPasswordField jTextFieldHostKey;

	private Map<String, Map<String, String>> changedClientInfos;
	private String oldNotes;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientInfoPanel(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initComponents();
		setupLayout();
	}

	private void initComponents() {
		labelClientID = new JLabel();

		labelClientID.setFont(labelClientID.getFont().deriveFont(Font.BOLD));

		labelClientDescription = new JLabel(Configed.getResourceValue("MainFrame.jLabelDescription"));
		labelClientDescription.setPreferredSize(Globals.BUTTON_DIMENSION);
		labelClientInventoryNumber = new JLabel(Configed.getResourceValue("MainFrame.jLabelInventoryNumber"));
		labelClientInventoryNumber.setPreferredSize(Globals.BUTTON_DIMENSION);
		labelClientNotes = new JLabel(Configed.getResourceValue("MainFrame.jLabelNotes"));
		labelClientSystemUUID = new JLabel(Configed.getResourceValue("MainFrame.jLabelSystemUUID"));
		labelClientMacAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelMacAddress"));
		labelClientIPAddress = new JLabel(Configed.getResourceValue("MainFrame.jLabelIPAddress"));
		labelOneTimePassword = new JLabel(Configed.getResourceValue("MainFrame.jLabelOneTimePassword"));
		labelOpsiHostKey = new JLabel("opsiHostKey");

		jTextFieldDescription = new RevertibleTextField("");
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		jTextFieldDescription.addKeyListener(this);

		jTextFieldInventoryNumber = new RevertibleTextField("");
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		jTextFieldInventoryNumber.addKeyListener(this);

		jTextAreaNotes = new JTextArea();

		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);

		jTextAreaNotes.addKeyListener(this);

		scrollpaneNotes = new JScrollPane(jTextAreaNotes);
		scrollpaneNotes.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);
		scrollpaneNotes.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpaneNotes.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		systemUUIDField = new RevertibleTextField(
				new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36, Character.MIN_VALUE, 36, true),
				"", 36);

		systemUUIDField.addKeyListener(this);

		macAddressField = new RevertibleTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2',
				'3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);

		macAddressField.addKeyListener(this);

		ipAddressField = new RevertibleTextField(
				new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'.', 'a', 'b', 'c', 'd', 'e', 'f', ':' }, 28, Character.MIN_VALUE, 4, false),
				"", 24);
		ipAddressField.addKeyListener(this);

		jCheckBoxUEFIBoot = new JCheckBox(Configed.getResourceValue("NewClientDialog.boottype"));
		jCheckBoxUEFIBoot.setEnabled(false);

		jCheckBoxWANConfig = new JCheckBox(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
		jCheckBoxWANConfig.addActionListener(event -> wanConfigAction());
		jCheckBoxWANConfig.setFocusable(false);

		jCheckBoxInstallByShutdown = new JCheckBox(Configed.getResourceValue("NewClientDialog.installByShutdown"));
		jCheckBoxInstallByShutdown.addActionListener(event -> installByShutdownAction());
		jCheckBoxInstallByShutdown.setFocusable(false);

		updateClientCheckboxText();

		jTextFieldOneTimePassword = new RevertibleTextField("");
		jTextFieldOneTimePassword.addKeyListener(this);

		jTextFieldHostKey = new JPasswordField();
		jTextFieldHostKey.setEditable(false);
	}

	private void setupLayout() {
		GroupLayout layoutClientPane = new GroupLayout(this);
		setLayout(layoutClientPane);
		layoutClientPane.setHorizontalGroup(layoutClientPane.createParallelGroup()
				/////// HOST
				.addGroup(layoutClientPane.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE)
						.addComponent(labelClientID, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE))

				/////// DESCRIPTION
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldDescription, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// INVENTORY NUMBER
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldInventoryNumber, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// SYSTEM UUID
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientSystemUUID, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(systemUUIDField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// MAC ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientMacAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(macAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// IP ADDRESS
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientIPAddress, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(ipAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
						Globals.FIRST_LABEL_WIDTH)

				/////// INSTALL BY SHUTDOWN
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
						jCheckBoxInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// UEFI BOOT
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jCheckBoxUEFIBoot, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// WAN CONFIG
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jCheckBoxWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// ONE TIME PASSWORD
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldOneTimePassword, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				////// opsiHostKey
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelOpsiHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(jTextFieldHostKey, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// NOTES
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(labelClientNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(scrollpaneNotes, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
				/////// HOST
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientID, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// DESCRIPTION
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// INVENTORY NUMBER
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldInventoryNumber, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// SYSTEM UUID
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(systemUUIDField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// MAC ADDRESS
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(macAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// IP ADDRESS
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientIPAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(ipAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// INSTALL BY SHUTDOWN
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jCheckBoxInstallByShutdown, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// UEFI BOOT & WAN Config
				.addComponent(jCheckBoxUEFIBoot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(jCheckBoxWANConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// ONE TIME PASSWORD
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldOneTimePassword, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				////// opsiHostKey
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelOpsiHostKey, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldHostKey, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// NOTES
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelClientNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpaneNotes, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void setClientDescriptionText(String s) {
		jTextFieldDescription.setText(s);
		jTextFieldDescription.setCaretPosition(0);
	}

	public void setClientInventoryNumberText(String s) {
		jTextFieldInventoryNumber.setText(s);
		jTextFieldInventoryNumber.setCaretPosition(0);
	}

	public void setClientOneTimePasswordText(String s) {
		jTextFieldOneTimePassword.setText(s);
		jTextFieldOneTimePassword.setCaretPosition(0);
	}

	public void setClientNotesText(String s) {
		jTextAreaNotes.setText(s);
		jTextAreaNotes.setCaretPosition(0);
		oldNotes = s;
	}

	public void setClientMacAddress(String s) {
		macAddressField.setText(s);
	}

	public void setClientSystemUUID(String s) {
		systemUUIDField.setText(s);
	}

	public void setClientIpAddress(String s) {
		ipAddressField.setText(s);
	}

	public void setUefiBoot() {
		Boolean value = persistenceController.getConfigDataService().isUEFI43(configedMain.getSelectedClients());

		Logging.info(this, "setUefiBoot " + value);
		Utils.setSelectedValueForButton(jCheckBoxUEFIBoot, value);
	}

	public void setWANConfig(Boolean value) {
		Logging.info(this, "setWANConfig " + value);
		Utils.setSelectedValueForButton(jCheckBoxWANConfig, value);
	}

	public void setShutdownInstall(Boolean value) {
		Logging.info(this, "setShutdownInstall " + value);
		Utils.setSelectedValueForButton(jCheckBoxInstallByShutdown, value);
	}

	public void setOpsiHostKey(String s) {
		Logging.info(this, "setOpsiHostKey " + s);
		jTextFieldHostKey.setText(s);
	}

	public void setClientID(String s) {
		labelClientID.setText(s);
	}

	public void updateClientCheckboxText() {
		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.UEFI)) {
			jCheckBoxUEFIBoot.setText(Configed.getResourceValue("NewClientDialog.boottype"));
		} else {
			jCheckBoxUEFIBoot.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			jCheckBoxUEFIBoot.setEnabled(false);
		}

		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)) {
			jCheckBoxWANConfig.setText(Configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			jCheckBoxWANConfig.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			jCheckBoxWANConfig.setEnabled(false);
		}
	}

	private void wanConfigAction() {
		Logging.info(this, "actionPerformed on cbWANConfig");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, Boolean.toString(jCheckBoxWANConfig.isSelected()));
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}

		// When we change the state, we need to remove this property, otherwise state might remain indeterminate
		jCheckBoxWANConfig.putClientProperty("JButton.selectedState", null);
		jCheckBoxWANConfig.repaint();
	}

	private void installByShutdownAction() {
		Logging.info(this, "actionPerformed on cbInstallByShutdown");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY,
					Boolean.toString(jCheckBoxInstallByShutdown.isSelected()));
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}

		// When we change the state, we need to remove this property, otherwise state might remain indeterminate
		jCheckBoxInstallByShutdown.putClientProperty("JButton.selectedState", null);
		jCheckBoxInstallByShutdown.repaint();
	}

	private void reactToClientDataChange(InputEvent e) {
		if (configedMain.getSelectedClients().size() != 1) {
			return;
		}

		if (e.getSource() == jTextFieldDescription) {
			applyChanges(jTextFieldDescription, HostInfo.CLIENT_DESCRIPTION_KEY);
		} else if (e.getSource() == jTextFieldInventoryNumber) {
			applyChanges(jTextFieldInventoryNumber, HostInfo.CLIENT_INVENTORY_NUMBER_KEY);
		} else if (e.getSource() == jTextFieldOneTimePassword) {
			applyChanges(jTextFieldOneTimePassword, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY);
		} else if (e.getSource() == jTextAreaNotes) {
			String client = configedMain.getSelectedClients().get(0);
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			if (!jTextAreaNotes.getText().equals(oldNotes)) {
				changedClientInfo.put(HostInfo.CLIENT_NOTES_KEY, jTextAreaNotes.getText());
				configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
			} else {
				changedClientInfo.remove(HostInfo.CLIENT_NOTES_KEY);
			}
		} else if (e.getSource() == systemUUIDField) {
			applyChanges(systemUUIDField, HostInfo.CLIENT_SYSTEM_UUID_KEY);
		} else if (e.getSource() == macAddressField) {
			applyChanges(macAddressField, HostInfo.CLIENT_MAC_ADRESS_KEY);
		} else if (e.getSource() == ipAddressField) {
			applyChanges(ipAddressField, HostInfo.CLIENT_IP_ADDRESS_KEY);
		} else {
			Logging.warning(this, "unexpected source in reactToHostDataChange: " + e.getSource());
		}
	}

	private void applyChanges(RevertibleTextField editorField, String key) {
		String client = configedMain.getSelectedClients().get(0);
		Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
		if (editorField.isTextChanged()) {
			changedClientInfo.put(key, editorField.getText());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		} else {
			changedClientInfo.remove(key);
		}
	}

	private Map<String, String> getChangedClientInfoFor(String client) {
		if (changedClientInfos == null) {
			changedClientInfos = new HashMap<>();
		}

		return changedClientInfos.computeIfAbsent(client, arg -> new HashMap<>());
	}

	public void setClientInfoEditing(boolean singleClient) {
		// singleClient is primarily conceived as toggle: true for single host, false
		// for multi hosts editing

		// mix with global read only flag
		boolean gb = !PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly();

		// resulting toggle for multi hosts editing
		boolean b1 = false;
		if (singleClient && gb) {
			b1 = true;
		}

		jTextFieldDescription.setEnabled(singleClient);
		jTextFieldDescription.setEditable(b1);
		jTextFieldInventoryNumber.setEnabled(singleClient);
		jTextFieldInventoryNumber.setEditable(b1);
		jTextFieldOneTimePassword.setEnabled(singleClient);
		jTextFieldOneTimePassword.setEditable(b1);
		jTextAreaNotes.setEnabled(singleClient);
		jTextAreaNotes.setEditable(b1);
		systemUUIDField.setEnabled(singleClient);
		systemUUIDField.setEditable(b1);
		macAddressField.setEnabled(singleClient);
		macAddressField.setEditable(b1);
		ipAddressField.setEnabled(singleClient);
		ipAddressField.setEditable(b1);

		jCheckBoxWANConfig
				.setEnabled(gb && persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN));
		jCheckBoxInstallByShutdown.setEnabled(gb);

		jTextFieldHostKey.setEnabled(singleClient);

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

	@Override
	public void keyPressed(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		Logging.debug(this, "key released " + configedMain.getSelectedClients());

		reactToClientDataChange(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
