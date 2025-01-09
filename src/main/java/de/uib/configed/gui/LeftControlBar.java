/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.EditingTarget;
import de.uib.configed.Globals;
import de.uib.utils.Icons;

public class LeftControlBar extends JToolBar {
	public LeftControlBar() {
		super(SwingConstants.VERTICAL);

		initControlIcons();
	}

	private void initControlIcons() {
		JToggleButton jButtonClientsConfiguration = new JToggleButton(Icons.getThemeIcon("desktop", 32));
		jButtonClientsConfiguration.setSelectedIcon(Icons.getSelectedThemeIntelljIcon("desktop", 32));
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setSelected(true);
		jButtonClientsConfiguration.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.CLIENTS));

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Icons.getIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setSelectedIcon(Icons.getSelectedIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("depotConfiguration"));
		jButtonDepotsConfiguration.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.DEPOTS));

		JToggleButton jButtonServerConfiguration = new JToggleButton(Icons.getIntellijIcon("settings", 32));
		jButtonServerConfiguration.setSelectedIcon(Icons.getSelectedIntellijIcon("settings", 32));
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.SERVER));

		JToggleButton jButtonDashboard = new JToggleButton(Icons.getIntellijIcon("dataSchema", 32));
		jButtonDashboard.setSelectedIcon(Icons.getSelectedIntellijIcon("dataSchema", 32));
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.DASHBOARD));

		JToggleButton jButtonOpsiLicenses = new JToggleButton(Icons.getOpsiModulesIcon());
		jButtonOpsiLicenses.setSelectedIcon(
				Icons.getOpsiIcon(32, FlatLaf.isLafDark() ? Globals.ICON_ACTIVE_DARK : Globals.ICON_ACTIVE_LIGHT));
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.OPSI_MODULES));

		JToggleButton jButtonHealthCheck = new JToggleButton(Icons.getIntellijIcon("springBootHealth", 32));
		jButtonHealthCheck.setSelectedIcon(Icons.getSelectedIntellijIcon("springBootHealth", 32));
		jButtonHealthCheck.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jButtonHealthCheck.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.HEALTH_CHECK));

		JToggleButton jButtonLicenses = new JToggleButton(Icons.getIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setSelectedIcon(Icons.getSelectedIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.addActionListener(event -> ConfigedMain.setEditingTarget(EditingTarget.LICENSE_MANAGEMENT));

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jButtonClientsConfiguration);
		buttonGroup.add(jButtonDepotsConfiguration);
		buttonGroup.add(jButtonServerConfiguration);
		buttonGroup.add(jButtonDashboard);
		buttonGroup.add(jButtonOpsiLicenses);
		buttonGroup.add(jButtonHealthCheck);
		buttonGroup.add(jButtonLicenses);

		add(jButtonClientsConfiguration);
		add(jButtonDepotsConfiguration);
		add(jButtonServerConfiguration);
		add(jButtonDashboard);
		add(jButtonOpsiLicenses);
		add(jButtonHealthCheck);
		add(jButtonLicenses);
	}
}
