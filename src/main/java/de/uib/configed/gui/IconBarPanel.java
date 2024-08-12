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

import com.formdev.flatlaf.FlatLaf;

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

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(
				layout.createParallelGroup().addComponent(iconsLeft).addComponent(opsiLogo).addComponent(targetIcons));

		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(iconsLeft).addGap(0, 0, Short.MAX_VALUE)
				.addComponent(opsiLogo).addGap(0, 0, Short.MAX_VALUE).addComponent(targetIcons));
	}

	private JToolBar initIconPaneTargets() {
		JToggleButton jButtonClientsConfiguration = new JToggleButton(Utils.getThemeIcon("desktop", 32));
		jButtonClientsConfiguration.setSelectedIcon(Utils.getSelectedThemeIntelljIcon("desktop", 32));
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setSelected(true);
		jButtonClientsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.CLIENTS));

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Utils.getIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));
		jButtonDepotsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.DEPOTS));

		JToggleButton jButtonServerConfiguration = new JToggleButton(Utils.getIntellijIcon("editorConfig", 32));
		jButtonServerConfiguration.setSelectedIcon(Utils.getSelectedIntellijIcon("editorConfig", 32));
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.SERVER));

		JToggleButton jButtonDashboard = new JToggleButton(Utils.getIntellijIcon("dataSchema", 32));
		jButtonDashboard.setSelectedIcon(Utils.getSelectedIntellijIcon("dataSchema", 32));
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.DASHBOARD));

		JToggleButton jButtonOpsiLicenses = new JToggleButton(Utils.getOpsiModulesIcon());
		jButtonOpsiLicenses.setSelectedIcon(
				Utils.getOpsiIcon(32, FlatLaf.isLafDark() ? Globals.ICON_ACTIVE_DARK : Globals.ICON_ACTIVE_LIGHT));
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.OPSI_MODULES));

		JToggleButton jButtonHealthCheck = new JToggleButton(Utils.getIntellijIcon("springBootHealth", 32));
		jButtonHealthCheck.setSelectedIcon(Utils.getSelectedIntellijIcon("springBootHealth", 32));
		jButtonHealthCheck.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jButtonHealthCheck.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.HEALTH_CHECK));

		JToggleButton jButtonLicenses = new JToggleButton(Utils.getIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setSelectedIcon(Utils.getSelectedIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.LICENSE_MANAGEMENT));

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jButtonClientsConfiguration);
		buttonGroup.add(jButtonDepotsConfiguration);
		buttonGroup.add(jButtonServerConfiguration);
		buttonGroup.add(jButtonDashboard);
		buttonGroup.add(jButtonOpsiLicenses);
		buttonGroup.add(jButtonHealthCheck);
		buttonGroup.add(jButtonLicenses);

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(jButtonClientsConfiguration);
		jToolBar.add(jButtonDepotsConfiguration);
		jToolBar.add(jButtonServerConfiguration);
		jToolBar.add(jButtonDashboard);
		jToolBar.add(jButtonOpsiLicenses);
		jToolBar.add(jButtonHealthCheck);
		jToolBar.add(jButtonLicenses);

		return jToolBar;
	}

	private JToolBar initIconsLeft() {
		JButton jButtonReload = new JButton(Utils.getIntellijIcon("refresh", 32));
		jButtonReload.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jButtonReload.addActionListener((ActionEvent e) -> configedMain.reload());

		jButtonReloadLicenses = new JButton(Utils.getReloadLicensingIcon());
		jButtonReloadLicenses.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		jButtonReloadLicenses.setVisible(false);
		jButtonReloadLicenses.addActionListener((ActionEvent e) -> configedMain.reloadLicensesAction());

		JButton jButtonNewClient = new JButton(Utils.getIntellijIcon("add", 32));
		jButtonNewClient.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonNewClient"));
		jButtonNewClient
				.setVisible(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
		jButtonNewClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		JButton jButtonSetGroup = new JButton(Utils.getIntellijIcon("search", 32));
		jButtonSetGroup.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jButtonSetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		jButtonSaveConfiguration = new JButton(Utils.getIntellijIcon("save", 32));
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
