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
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.modulelicense.LicensingInfoDialog;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class IconBarPanel extends JPanel {
	private JButton jButtonOpsiLicenses;

	private JButton jButtonReload;
	private JButton jButtonReloadLicenses;
	private JButton jButtonNewClient;
	private JButton jButtonSetGroup;
	private JButton jButtonSaveConfiguration;
	private JButton jButtonToggleClientFilter;

	private JButton jButtonReachableInfo;
	private JButton jButtonSessionInfo;

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

	public JButton getjButtonReloadLicenses() {
		return jButtonReloadLicenses;
	}

	public JButton getjButtonSaveConfiguration() {
		return jButtonSaveConfiguration;
	}

	public JButton getjButtonToggleClientFilter() {
		return jButtonToggleClientFilter;
	}

	public JButton getjButtonReachableInfo() {
		return jButtonReachableInfo;
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

		JToolBar iconPaneTargets = initIconPaneTargets();
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

	private JToolBar initIconPaneTargets() {
		JToggleButton jButtonServerConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("editorConfig"));
		jButtonServerConfiguration.setSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.setFocusable(false);

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("dbms"));
		jButtonDepotsConfiguration.setSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelDepotsConfiguration"));
		jButtonDepotsConfiguration.setFocusable(false);

		JToggleButton jButtonClientsConfiguration = new JToggleButton(Utils.getLargeIntellijIcon("desktop"));
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

	private JPanel initIconPaneExtraFrames() {
		JButton jButtonLicenses = new JButton(Utils.getLargeIntellijIcon("scriptingScript"));
		jButtonLicenses.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.setFocusable(false);
		jButtonLicenses.addActionListener(event -> configedMain.handleLicensesManagementRequest());

		JButton jButtonWorkOnGroups = new JButton(Utils.createImageIcon("images/group_all_selected_40.png", ""));
		jButtonWorkOnGroups.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnGroups.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups"));
		jButtonWorkOnGroups.setFocusable(false);

		jButtonWorkOnGroups
				.setEnabled(persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.LOCAL_IMAGING));
		jButtonWorkOnGroups.addActionListener(event -> configedMain.handleGroupActionRequest());

		JButton jButtonWorkOnProducts = new JButton(Utils.createImageIcon("images/packagebutton.png", ""));
		jButtonWorkOnProducts.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonWorkOnProducts.setToolTipText(Configed.getResourceValue("MainFrame.labelWorkOnProducts"));
		jButtonWorkOnProducts.setFocusable(false);

		jButtonWorkOnProducts.addActionListener(event -> configedMain.startProductActionFrame());

		JButton jButtonDashboard = new JButton(Utils.getLargeIntellijIcon("dataSchema"));
		jButtonDashboard.setPreferredSize(Globals.MODE_SWITCH_DIMENSION);
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.setFocusable(false);
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
				.addGap(Globals.GAP_SIZE)
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
		if (persistenceController.getModuleDataService().isOpsiUserAdminPD() && licensingInfoMap == null) {
			licensingInfoMap = LicensingInfoMap.getInstance(
					persistenceController.getModuleDataService().getOpsiLicensingInfoOpsiAdminPD(),
					persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
					!LicensingInfoDialog.isExtendedView());

			switch (licensingInfoMap.getWarningLevel()) {
			case LicensingInfoMap.STATE_OVER_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(Globals.OPSI_ERROR));
				break;
			case LicensingInfoMap.STATE_CLOSE_TO_LIMIT:
				jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(Globals.OPSI_WARNING));
				break;

			case LicensingInfoMap.STATE_OKAY:
				jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(Globals.OPSI_OK));
				break;

			default:
				Logging.warning(this, "unexpected warninglevel: " + licensingInfoMap.getWarningLevel());
				break;
			}
		} else {
			jButtonOpsiLicenses = new JButton(Utils.getOpsiModulesIcon(Globals.OPSI_OK));
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
								.addComponent(jButtonReload, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonReloadLicenses, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonNewClient, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonSetGroup, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonSaveConfiguration, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonToggleClientFilter, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jButtonReachableInfo, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
								.addGap(Globals.MIN_GAP_SIZE).addComponent(jButtonSessionInfo,
										Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
										Globals.GRAPHIC_BUTTON_SIZE));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonReload, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonReloadLicenses, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonNewClient, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonSetGroup, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonSaveConfiguration, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonToggleClientFilter, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonReachableInfo, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(jButtonSessionInfo, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)));

		return iconsTopLeft;
	}

	private void setupIcons() {
		jButtonReload = new JButton(Utils.getLargeIntellijIcon("refresh"));
		jButtonReload.setToolTipText(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jButtonReload.setFocusable(false);
		jButtonReload.addActionListener((ActionEvent e) -> configedMain.reload());

		jButtonReloadLicenses = new JButton(Utils.getReloadLicensingIcon());
		jButtonReloadLicenses.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"));
		jButtonReloadLicenses.setFocusable(false);
		jButtonReloadLicenses.setVisible(false);
		jButtonReloadLicenses.addActionListener((ActionEvent e) -> mainFrame.reloadLicensesAction());

		jButtonNewClient = new JButton(Utils.getLargeIntellijIcon("add"));
		jButtonNewClient.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonNewClient"));
		jButtonNewClient.setFocusable(false);
		jButtonNewClient
				.setVisible(persistenceController.getUserRolesConfigDataService().hasCreateClientPermissionPD());
		jButtonNewClient.addActionListener((ActionEvent e) -> configedMain.callNewClientDialog());

		jButtonSetGroup = new JButton(Utils.getLargeIntellijIcon("search"));
		jButtonSetGroup.setToolTipText(Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		jButtonSetGroup.setFocusable(false);
		jButtonSetGroup.addActionListener((ActionEvent e) -> configedMain.callClientSelectionDialog());

		jButtonSaveConfiguration = new JButton(Utils.getLargeIntellijIcon("save"));
		jButtonSaveConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"));
		jButtonSaveConfiguration.setFocusable(false);
		jButtonSaveConfiguration.setEnabled(false);
		jButtonSaveConfiguration.addActionListener((ActionEvent e) -> configedMain.checkSaveAll(false));

		jButtonToggleClientFilter = new JButton(Utils.getIntellijIcon("funnelRegular"));
		jButtonToggleClientFilter.setSelectedIcon(Utils.getSelectedIntellijIcon("funnelRegular"));
		jButtonToggleClientFilter.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"));
		jButtonToggleClientFilter.setFocusable(false);
		jButtonToggleClientFilter.addActionListener((ActionEvent e) -> mainFrame.toggleClientFilterAction());

		jButtonReachableInfo = new JButton(Utils.createImageIcon("images/new_networkconnection.png", ""));
		jButtonReachableInfo.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonReachableInfo"));
		jButtonReachableInfo.setFocusable(false);
		jButtonReachableInfo
				.setEnabled(persistenceController.getHostDataService().getHostDisplayFields().get("clientConnected"));
		jButtonReachableInfo.addActionListener((ActionEvent e) -> {
			jButtonReachableInfo.setEnabled(false);
			SwingUtilities.invokeLater(configedMain::getReachableInfo);
		});

		jButtonSessionInfo = new JButton(Utils.getLargeIntellijIcon("infoOutline"));
		jButtonSessionInfo.setToolTipText(Configed.getResourceValue("MainFrame.iconButtonSessionInfo"));
		jButtonSessionInfo.setFocusable(false);
		jButtonSessionInfo.addActionListener(event -> configedMain.getSessionInfo());
	}

	public void showReloadLicensingButton() {
		jButtonReloadLicenses.setVisible(true);
	}
}
