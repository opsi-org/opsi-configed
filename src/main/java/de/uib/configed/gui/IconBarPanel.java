/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;

public class IconBarPanel extends JPanel {
	private JButton jButtonReloadLicenses;
	private JButton jButtonSaveConfiguration;

	private JButton jButtonSessionInfo;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;
	private MainFrame mainFrame;

	public IconBarPanel(ConfigedMain configedMain, MainFrame mainFrame) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		init();
	}

	public JButton getjButtonReloadLicenses() {
		return jButtonReloadLicenses;
	}

	public JButton getjButtonSaveConfiguration() {
		return jButtonSaveConfiguration;
	}

	public JButton getjButtonSessionInfo() {
		return jButtonSessionInfo;
	}

	private void init() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 0;
		c.gridy = 0;

		add(initIconsLeft(), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;

		add(new JLabel(Utils.getOpsiLogoWide()), c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 2;
		c.gridy = 0;
		add(initIconsTopRight(), c);
	}

	private JPanel initIconsTopRight() {
		JPanel iconsTopRight = new JPanel();
		GroupLayout layoutIconPane0 = new GroupLayout(iconsTopRight);
		iconsTopRight.setLayout(layoutIconPane0);

		JToolBar iconPaneTargets = initIconPaneTargets();
		JToolBar iconPaneExtraFrames = initIconPaneExtraFrames();

		layoutIconPane0.setHorizontalGroup(
				layoutIconPane0.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE));

		layoutIconPane0.setVerticalGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		return iconsTopRight;
	}

	private JToolBar initIconPaneTargets() {
		JToggleButton jButtonServerConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("editorConfig"));
		jButtonServerConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("editorConfig", 32));
		jButtonServerConfiguration.setSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.setFocusable(false);

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("dbms"));
		jButtonDepotsConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));
		jButtonDepotsConfiguration.setFocusable(false);

		JToggleButton jButtonClientsConfiguration = new JToggleButton(Utils.getThemeIcon("desktop", 32));
		jButtonClientsConfiguration.setSelectedIcon(Utils.getSelectedThemeIntelljIcon("desktop").derive(32, 32));
		jButtonClientsConfiguration.setSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setFocusable(false);
		jButtonClientsConfiguration.setSelected(true);

		jButtonServerConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.SERVER));
		jButtonDepotsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.DEPOTS));
		jButtonClientsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.CLIENTS));

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jButtonClientsConfiguration);
		buttonGroup.add(jButtonDepotsConfiguration);
		buttonGroup.add(jButtonServerConfiguration);

		JToolBar jToolBarSwitch = new JToolBar();
		jToolBarSwitch.add(jButtonClientsConfiguration);
		jToolBarSwitch.add(jButtonDepotsConfiguration);
		jToolBarSwitch.add(jButtonServerConfiguration);

		return jToolBarSwitch;
	}

	private JToolBar initIconPaneExtraFrames() {
		JButton jButtonDashboard = new JButton(Utils.getLargeIntellijIcon("dataSchema"));
		jButtonDashboard.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.setFocusable(false);
		jButtonDashboard.addActionListener(event -> configedMain.initDashInfo());

		JButton jButtonHealthCheck = new JButton(Utils.getLargeIntellijIcon("springBootHealth"));
		jButtonHealthCheck.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonHealthCheck.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jButtonHealthCheck.setFocusable(false);
		jButtonHealthCheck.addActionListener(event -> mainFrame.showHealthDataAction());

		JButton jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(32));
		jButtonOpsiLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(e -> mainFrame.showOpsiModules());
		jButtonOpsiLicenses.setFocusable(false);

		JButton jButtonLicenses = new JButton(Utils.getLargeIntellijIcon("scriptingScript"));
		jButtonLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.setFocusable(false);
		jButtonLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(jButtonDashboard);
		jToolBar.add(jButtonHealthCheck);
		jToolBar.add(jButtonOpsiLicenses);
		jToolBar.add(jButtonLicenses);

		return jToolBar;
	}

	private JToolBar initIconsLeft() {
		JButton jButtonReload = new JButton(Utils.getLargeIntellijIcon("refresh"));
		jButtonReload.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jButtonReload.setFocusable(false);
		jButtonReload.addActionListener((ActionEvent e) -> configedMain.reload());

		jButtonReloadLicenses = new JButton(Utils.getReloadLicensingIcon());
		jButtonReloadLicenses.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		jButtonReloadLicenses.setFocusable(false);
		jButtonReloadLicenses.setVisible(false);
		jButtonReloadLicenses.addActionListener((ActionEvent e) -> mainFrame.reloadLicensesAction());

		JButton jButtonNewClient = new JButton(Utils.getLargeIntellijIcon("add"));
		jButtonNewClient.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonNewClient"));
		jButtonNewClient.setFocusable(false);
		jButtonNewClient
				.setVisible(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
		jButtonNewClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		JButton jButtonSetGroup = new JButton(Utils.getLargeIntellijIcon("search"));
		jButtonSetGroup.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jButtonSetGroup.setFocusable(false);
		jButtonSetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		jButtonSaveConfiguration = new JButton(Utils.getLargeIntellijIcon("save"));
		jButtonSaveConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"));
		jButtonSaveConfiguration.setFocusable(false);
		jButtonSaveConfiguration.setEnabled(false);
		jButtonSaveConfiguration.addActionListener((ActionEvent e) -> configedMain.checkSaveAll(false));

		jButtonSessionInfo = new JButton(Utils.getThemeIcon("user", 32));
		jButtonSessionInfo.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"));
		jButtonSessionInfo.setFocusable(false);
		jButtonSessionInfo.addActionListener(event -> configedMain.getSessionInfo());

		JToolBar jToolBar = new JToolBar("has a name");
		jToolBar.add(jButtonReload);
		jToolBar.add(jButtonReloadLicenses);
		jToolBar.add(jButtonNewClient);
		jToolBar.add(jButtonSetGroup);
		jToolBar.add(jButtonSaveConfiguration);
		jToolBar.add(jButtonSessionInfo);

		return jToolBar;
	}

	public void showReloadLicensingButton() {
		jButtonReloadLicenses.setVisible(true);
	}
}
