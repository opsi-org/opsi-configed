/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.Globals;
import de.uib.configed.type.HostInfo;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.modulelicense.LicensingInfoDialog;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class IconBarPanel extends JPanel {
	private JButton jButtonServerConfiguration;
	private JButton jButtonDepotsConfiguration;
	private JButton jButtonClientsConfiguration;

	private JButton jButtonLicenses;
	private JButton jButtonOpsiLicenses;

	private IconButton iconButtonReload;
	private IconButton iconButtonReloadLicenses;
	private IconButton iconButtonNewClient;
	private IconButton iconButtonSetGroup;
	private IconButton iconButtonSaveConfiguration;
	private IconButton iconButtonToggleClientFilter;

	private IconButton iconButtonReachableInfo;
	private IconButton iconButtonSessionInfo;

	private LicensingInfoMap licensingInfoMap;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ConfigedMain configedMain;
	private MainFrame mainFrame;

	public IconBarPanel(ConfigedMain configedMain, MainFrame mainFrame) {
		this.configedMain = configedMain;
		this.mainFrame = mainFrame;
		init();
	}

	public IconButton getIconButtonReloadLicenses() {
		return iconButtonReloadLicenses;
	}

	public IconButton getIconButtonSaveConfiguration() {
		return iconButtonSaveConfiguration;
	}

	public IconButton getIconButtonToggleClientFilter() {
		return iconButtonToggleClientFilter;
	}

	public IconButton getIconButtonReachableInfo() {
		return iconButtonReachableInfo;
	}

	public IconButton getIconButtonSessionInfo() {
		return iconButtonSessionInfo;
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

		String logoPath;

		if (FlatLaf.isLafDark()) {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_ohne_Text_quer_neg.png";
		} else {
			logoPath = "opsilogos/UIB_1704_2023_OPSI_Logo_Bildmarke_kurz_quer.png";
		}

		add(new JLabel(Utils.createImageIcon(logoPath, null, 150, 50)), c);

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

		JPanel iconPaneTargets = initIconPaneTargets();
		JPanel iconPaneExtraFrames = initIconPaneExtraFrames();

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

	private JPanel initIconPaneTargets() {
		jButtonServerConfiguration = new JButton(Utils.createImageIcon("images/opsiconsole_deselected.png", ""));
		jButtonServerConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsiconsole.png", ""));
		jButtonServerConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.setFocusable(false);

		jButtonDepotsConfiguration = new JButton(Utils.createImageIcon("images/opsidepots_deselected.png", ""));
		jButtonDepotsConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsidepots.png", ""));
		jButtonDepotsConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));
		jButtonDepotsConfiguration.setFocusable(false);

		jButtonClientsConfiguration = new JButton(Utils.createImageIcon("images/opsiclients_deselected.png", ""));
		jButtonClientsConfiguration.setSelectedIcon(Utils.createImageIcon("images/opsiclients.png", ""));
		jButtonClientsConfiguration.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setFocusable(false);

		jButtonServerConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.SERVER));
		jButtonDepotsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.DEPOTS));
		jButtonClientsConfiguration.addActionListener(event -> configedMain.setEditingTarget(EditingTarget.CLIENTS));

		JPanel iconPaneTargets = new JPanel();

		GroupLayout layoutIconPaneTargets = new GroupLayout(iconPaneTargets);
		iconPaneTargets.setLayout(layoutIconPaneTargets);

		layoutIconPaneTargets.setHorizontalGroup(layoutIconPaneTargets.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutIconPaneTargets.setVerticalGroup(layoutIconPaneTargets.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		return iconPaneTargets;
	}

	private JPanel initIconPaneExtraFrames() {
		jButtonLicenses = new JButton(Utils.createImageIcon("images/licenses_deselected.png", ""));
		jButtonLicenses.setEnabled(false);
		jButtonLicenses.setSelectedIcon(Utils.createImageIcon("images/licenses.png", ""));
		jButtonLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.setFocusable(false);
		jButtonLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		JButton jButtonWorkOnGroups = new JButton(Utils.createImageIcon("images/group_all_unselected_40.png", ""));
		jButtonWorkOnGroups.setSelectedIcon(Utils.createImageIcon("images/group_all_selected_40.png", ""));
		jButtonWorkOnGroups.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnGroups.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jButtonWorkOnGroups.setFocusable(false);

		jButtonWorkOnGroups
				.setEnabled(persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LOCAL_IMAGING));
		jButtonWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JButton jButtonWorkOnProducts = new JButton(Utils.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setSelectedIcon(Utils.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnProducts.setToolTipText(Configed.getResourceValue("MainFrame.labelWorkOnProducts"));
		jButtonWorkOnProducts.setFocusable(false);

		jButtonWorkOnProducts.addActionListener(event -> configedMain.startProductActionFrame());

		JButton jButtonDashboard = new JButton(Utils.createImageIcon("images/dash_unselected.png", ""));
		jButtonDashboard.setSelectedIcon(Utils.createImageIcon("images/dash_selected.png", ""));
		jButtonDashboard.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.setFocusable(false);
		jButtonDashboard.setEnabled(ServerFacade.isOpsi43());
		jButtonDashboard.setVisible(ServerFacade.isOpsi43());
		jButtonDashboard.addActionListener(event -> configedMain.initDashInfo());

		initOpsiLicenseButtonBasedOnWarningLevel();
		jButtonOpsiLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(e -> mainFrame.showOpsiModules());
		jButtonOpsiLicenses.setFocusable(false);

		JPanel iconPaneExtraFrames = new JPanel();

		GroupLayout layoutIconPaneExtraFrames = new GroupLayout(iconPaneExtraFrames);
		iconPaneExtraFrames.setLayout(layoutIconPaneExtraFrames);

		layoutIconPaneExtraFrames.setHorizontalGroup(layoutIconPaneExtraFrames.createSequentialGroup()
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0, ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0,
						ServerFacade.isOpsi43() ? Globals.GAP_SIZE : 0)
				.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jButtonLicenses, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutIconPaneExtraFrames.setVerticalGroup(layoutIconPaneExtraFrames.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonOpsiLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		return iconPaneExtraFrames;
	}

	private void initOpsiLicenseButtonBasedOnWarningLevel() {
		if (persistenceController.getModuleDataService().isOpsiLicensingAvailablePD()
				&& persistenceController.getModuleDataService().isOpsiUserAdminPD() && licensingInfoMap == null) {
			licensingInfoMap = LicensingInfoMap.getInstance(
					persistenceController.getModuleDataService().getOpsiLicensingInfoOpsiAdminPD(),
					persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
					!LicensingInfoDialog.isExtendedView());

			switch (licensingInfoMap.getWarningLevel()) {
			case LicensingInfoMap.STATE_OVER_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses-error-small.png", ""));
				break;
			case LicensingInfoMap.STATE_CLOSE_TO_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses-warning-small.png", ""));
				break;

			case LicensingInfoMap.STATE_OKAY:
				jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses.png", ""));
				break;

			default:
				Logging.warning(this, "unexpected warninglevel: " + licensingInfoMap.getWarningLevel());
				break;
			}
		} else {
			jButtonOpsiLicenses = new JButton(Utils.createImageIcon("images/opsi-licenses.png", ""));
		}
	}

	private JPanel initIconsLeft() {
		setupIcons();
		JPanel iconsTopLeft = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconsTopLeft);
		iconsTopLeft.setLayout(layoutIconPane1);

		layoutIconPane1
				.setHorizontalGroup(
						layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE).addComponent(iconButtonSessionInfo,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		return iconsTopLeft;
	}

	private void setupIcons() {
		iconButtonReload = new IconButton(Configed.getResourceValue("MainFrame.jMenuFileReload"), "images/reload.gif",
				"images/reload_over.gif", "");
		iconButtonReload.setFocusable(false);
		iconButtonReload.addActionListener((ActionEvent e) -> configedMain.reload());

		iconButtonReloadLicenses = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
				"images/reload_licenses.png", "images/reload_licenses_over.png", "", true);
		iconButtonReloadLicenses.setFocusable(false);
		iconButtonReloadLicenses.setVisible(false);
		iconButtonReloadLicenses.addActionListener((ActionEvent e) -> mainFrame.reloadLicensesAction());

		iconButtonNewClient = new IconButton(Configed.getResourceValue("MainFrame.iconButtonNewClient"),
				"images/newClient.gif", "images/newClient_over.gif", "");
		iconButtonNewClient.setFocusable(false);
		iconButtonNewClient
				.setVisible(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
		iconButtonNewClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		iconButtonSetGroup = new IconButton(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"),
				"images/setGroup.gif", "images/setGroup_over.gif", "");
		iconButtonSetGroup.setFocusable(false);
		iconButtonSetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		iconButtonSaveConfiguration = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply.png", "", "images/apply_disabled.png", false);
		iconButtonSaveConfiguration.setFocusable(false);
		iconButtonSaveConfiguration.addActionListener((ActionEvent e) -> configedMain.checkSaveAll(false));

		iconButtonToggleClientFilter = new IconButton(
				Configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
				"images/view-filter_disabled-32.png", "images/view-filter_over-32.png", "images/view-filter-32.png",
				true);
		iconButtonToggleClientFilter.setFocusable(false);
		iconButtonToggleClientFilter.addActionListener((ActionEvent e) -> mainFrame.toggleClientFilterAction());

		iconButtonReachableInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
				"images/new_networkconnection.png", "images/new_networkconnection.png",
				"images/new_networkconnection.png",
				persistenceController.getHostDataService().getHostDisplayFields().get("clientConnected"));
		iconButtonReachableInfo.setFocusable(false);
		iconButtonReachableInfo.addActionListener((ActionEvent e) -> {
			iconButtonReachableInfo.setEnabled(false);
			SwingUtilities.invokeLater(configedMain::getReachableInfo);
		});

		iconButtonSessionInfo = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
				"images/system-users-query.png", "images/system-users-query_over.png",
				"images/system-users-query_over.png", persistenceController.getHostDataService().getHostDisplayFields()
						.get(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL));
		iconButtonSessionInfo.setFocusable(false);
		iconButtonSessionInfo.setEnabled(true);
		iconButtonSessionInfo.addActionListener((ActionEvent e) -> {
			configedMain.setColumnSessionInfo(true);
			configedMain.getSessionInfo();
		});
	}

	public void visualizeEditingTarget(EditingTarget t) {
		switch (t) {
		case CLIENTS:
			jButtonClientsConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonServerConfiguration.setSelected(false);
			break;

		case DEPOTS:
			jButtonDepotsConfiguration.setSelected(true);
			jButtonServerConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			break;

		case SERVER:
			jButtonServerConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			break;

		default:
			break;
		}
	}

	public void visualizeLicensesFramesActive(boolean b) {
		jButtonLicenses.setSelected(b);
		iconButtonReloadLicenses.setVisible(true);
		iconButtonReloadLicenses.setEnabled(true);
	}

	public void enableAfterLoading() {
		jButtonLicenses.setEnabled(true);
	}
}
