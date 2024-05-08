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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import de.uib.utils.swing.CheckedLabel;
import de.uib.utils.swing.RevertibleTextField;
import de.uib.utils.swing.SeparatedDocument;
import de.uib.utils.swing.ToggleableTextField;

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
	private CheckedLabel cbInstallByShutdown;
	private CheckedLabel cbUefiBoot;
	private CheckedLabel cbWANConfig;

	private RevertibleTextField jTextFieldDescription;
	private RevertibleTextField jTextFieldInventoryNumber;
	private JTextArea jTextAreaNotes;
	private RevertibleTextField systemUUIDField;
	private RevertibleTextField macAddressField;
	private RevertibleTextField ipAddressField;
	private RevertibleTextField jTextFieldOneTimePassword;
	private ToggleableTextField jTextFieldHostKey;

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

		final Icon unselectedIcon = Utils.getThemeIconPNG("bootstrap/square", "");
		final Icon selectedIcon = Utils.getThemeIconPNG("bootstrap/check-square", "");
		final Icon nullIcon = Utils.getThemeIconPNG("bootstrap/slash-square", "");

		cbUefiBoot = new CheckedLabel(Configed.getResourceValue("NewClientDialog.boottype"), selectedIcon,
				unselectedIcon, nullIcon, false);

		cbWANConfig = new CheckedLabel(Configed.getResourceValue("NewClientDialog.wan_not_activated"), selectedIcon,
				unselectedIcon, nullIcon, false);
		cbWANConfig.setEnabled(true);
		cbWANConfig.addActionListener(event -> wanConfigAction());

		cbInstallByShutdown = new CheckedLabel(Configed.getResourceValue("NewClientDialog.installByShutdown"),
				selectedIcon, unselectedIcon, nullIcon, false);
		cbInstallByShutdown.setEnabled(true);
		cbInstallByShutdown.addActionListener(event -> installByShutdownAction());

		updateClientCheckboxText();

		jTextFieldOneTimePassword = new RevertibleTextField("");
		jTextFieldOneTimePassword.addKeyListener(this);

		jTextFieldHostKey = new ToggleableTextField();
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
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(cbInstallByShutdown, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// UEFI BOOT
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(cbUefiBoot,
						0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				/////// WAN CONFIG
				.addGroup(layoutClientPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(cbWANConfig, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

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
				.addComponent(cbInstallByShutdown, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// UEFI BOOT & WAN Config
				.addComponent(cbUefiBoot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(cbWANConfig, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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
		cbUefiBoot
				.setSelected(persistenceController.getConfigDataService().isUEFI43(configedMain.getSelectedClients()));
	}

	public void setWANConfig(Boolean b) {
		Logging.info(this, "setWANConfig " + b);
		cbWANConfig.setSelected(b);
	}

	public void setOpsiHostKey(String s) {
		Logging.info(this, "setOpsiHostKey " + s);
		jTextFieldHostKey.setText(s);
	}

	public void setShutdownInstall(Boolean b) {
		Logging.info(this, "setShutdownInstall " + b);
		cbInstallByShutdown.setSelected(b);
	}

	public void setClientID(String s) {
		labelClientID.setText(s);
	}

	public void updateClientCheckboxText() {
		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.UEFI)) {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype"));
		} else {
			cbUefiBoot.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			cbUefiBoot.setEnabled(false);
		}

		if (persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)) {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wanConfig"));
		} else {
			cbWANConfig.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			cbWANConfig.setEnabled(false);
		}
	}

	private void wanConfigAction() {
		Logging.info(this, "actionPerformed on cbWANConfig");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_WAN_CONFIG_KEY, cbWANConfig.isSelected().toString());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
	}

	private void installByShutdownAction() {
		Logging.info(this, "actionPerformed on cbInstallByShutdown");

		for (String client : configedMain.getSelectedClients()) {
			Map<String, String> changedClientInfo = getChangedClientInfoFor(client);
			changedClientInfo.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, cbInstallByShutdown.isSelected().toString());
			configedMain.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfos);
		}
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

		// multi host editing allowed
		cbUefiBoot.setEnabled(gb && persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.UEFI));
		cbUefiBoot.disableSelection();

		cbWANConfig.setEnabled(gb && persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN));
		cbInstallByShutdown.setEnabled(gb);

		jTextFieldHostKey.setMultiValue(!singleClient);
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
