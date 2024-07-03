/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

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
		JToolBar iconsLeft = initIconsLeft();
		JLabel opsiLogo = new JLabel(Utils.getOpsiLogoWide());
		JToolBar targetIcons = initIconPaneTargets();
		JToolBar extraFrames = initIconPaneExtraFrames();

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup().addComponent(iconsLeft).addComponent(opsiLogo)
				.addComponent(targetIcons).addComponent(extraFrames));

		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(iconsLeft).addGap(0, 0, Short.MAX_VALUE)
				.addComponent(opsiLogo).addGap(0, 0, Short.MAX_VALUE).addComponent(targetIcons)
				.addGap(0, 3 * Globals.GAP_SIZE, 3 * Globals.GAP_SIZE).addComponent(extraFrames));
	}

	private JToolBar initIconPaneTargets() {
		JToggleButton jButtonServerConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("editorConfig"));
		jButtonServerConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("editorConfig", 32));
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("dbms"));
		jButtonDepotsConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));

		JToggleButton jButtonClientsConfiguration = new JToggleButton(Utils.getThemeIcon("desktop", 32));
		jButtonClientsConfiguration.setSelectedIcon(Utils.getSelectedThemeIntelljIcon("desktop").derive(32, 32));
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
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
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.addActionListener(event -> configedMain.initDashInfo());

		JButton jButtonHealthCheck = new JButton(Utils.getLargeIntellijIcon("springBootHealth"));
		jButtonHealthCheck.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jButtonHealthCheck.addActionListener(event -> mainFrame.showHealthDataAction());

		JButton jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(32));
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(e -> mainFrame.showOpsiModules());

		JButton jButtonLicenses = new JButton(Utils.getLargeIntellijIcon("scriptingScript"));
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
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
		jButtonReload.addActionListener((ActionEvent e) -> configedMain.reload());

		jButtonReloadLicenses = new JButton(Utils.getReloadLicensingIcon());
		jButtonReloadLicenses.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		jButtonReloadLicenses.setVisible(false);
		jButtonReloadLicenses.addActionListener((ActionEvent e) -> mainFrame.reloadLicensesAction());

		JButton jButtonNewClient = new JButton(Utils.getLargeIntellijIcon("add"));
		jButtonNewClient.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonNewClient"));
		jButtonNewClient
				.setVisible(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
		jButtonNewClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		JButton jButtonSetGroup = new JButton(Utils.getLargeIntellijIcon("search"));
		jButtonSetGroup.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jButtonSetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		jButtonSaveConfiguration = new JButton(Utils.getLargeIntellijIcon("save"));
		jButtonSaveConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"));
		jButtonSaveConfiguration.setEnabled(false);
		jButtonSaveConfiguration.addActionListener((ActionEvent e) -> configedMain.checkSaveAll(false));

		jButtonSessionInfo = new JButton(Utils.getThemeIcon("user", 32));
		jButtonSessionInfo.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"));
		jButtonSessionInfo.addActionListener(event -> configedMain.getSessionInfo());

		JToolBar jToolBar = new JToolBar();
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
