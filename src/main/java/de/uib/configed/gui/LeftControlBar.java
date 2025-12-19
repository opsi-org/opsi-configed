/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import de.uib.configed.gui.ConfigedMain.EditingTarget;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class LeftControlBar extends JToolBar {
	private ButtonGroup buttonGroup;

	JToggleButton selectedButton;

	public LeftControlBar() {
		super(SwingConstants.VERTICAL);

		initControlIcons();
	}

	private void initControlIcons() {
		JToggleButton jButtonClientsConfiguration = new JToggleButton(Icons.getThemeIntellijIcon("desktop", 32));
		jButtonClientsConfiguration.setSelectedIcon(Icons.getSelectedThemeIntelljIcon("desktop", 32));
		jButtonClientsConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelClientsConfiguration"));
		jButtonClientsConfiguration.setSelected(true);
		selectedButton = jButtonClientsConfiguration;
		jButtonClientsConfiguration
				.addActionListener(event -> setEditingTarget(EditingTarget.CLIENTS, jButtonClientsConfiguration));

		JToggleButton jButtonDepotsConfiguration = new JToggleButton(Icons.getIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setSelectedIcon(Icons.getSelectedIntellijIcon("dbms", 32));
		jButtonDepotsConfiguration.setToolTipText(Configed.getResourceValue("depotConfiguration"));
		jButtonDepotsConfiguration
				.addActionListener(event -> setEditingTarget(EditingTarget.DEPOTS, jButtonDepotsConfiguration));

		JToggleButton jButtonServerConfiguration = new JToggleButton(Icons.getIntellijIcon("settings", 32));
		jButtonServerConfiguration.setSelectedIcon(Icons.getSelectedIntellijIcon("settings", 32));
		jButtonServerConfiguration.setToolTipText(Configed.getResourceValue("MainFrame.labelServerConfiguration"));
		jButtonServerConfiguration
				.addActionListener(event -> setEditingTarget(EditingTarget.SERVER, jButtonServerConfiguration));

		JToggleButton jButtonDashboard = new JToggleButton(Icons.getIntellijIcon("dataSchema", 32));
		jButtonDashboard.setSelectedIcon(Icons.getSelectedIntellijIcon("dataSchema", 32));
		jButtonDashboard.setToolTipText(Configed.getResourceValue("Dashboard.title"));
		jButtonDashboard.addActionListener(event -> setEditingTarget(EditingTarget.DASHBOARD, jButtonDashboard));

		JToggleButton jButtonOpsiLicenses = new JToggleButton(Icons.getOpsiModulesIcon(32));
		jButtonOpsiLicenses.setSelectedIcon(Icons.getActiveOpsiModulesIcon(32));
		jButtonOpsiLicenses.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"));
		jButtonOpsiLicenses
				.addActionListener(event -> setEditingTarget(EditingTarget.OPSI_MODULES, jButtonOpsiLicenses));

		JToggleButton jButtonHealthCheck = new JToggleButton();
		Icons.addActiveHealthCheckIcon(jButtonHealthCheck, 32);
		jButtonHealthCheck.setToolTipText(Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"));
		jButtonHealthCheck.addActionListener(event -> setEditingTarget(EditingTarget.HEALTH_CHECK, jButtonHealthCheck));

		JToggleButton jButtonLicenses = new JToggleButton(Icons.getIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setSelectedIcon(Icons.getSelectedIntellijIcon("scriptingScript", 32));
		jButtonLicenses.setToolTipText(Configed.getResourceValue("MainFrame.labelLicenses"));
		jButtonLicenses.addActionListener(event -> setEditingTarget(EditingTarget.LICENSE_MANAGEMENT, jButtonLicenses));

		buttonGroup = new ButtonGroup();
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

	private void setEditingTarget(EditingTarget editingTarget, JToggleButton selectedButton) {
		if (ConfigedMain.setEditingTarget(editingTarget)) {
			this.selectedButton = selectedButton;
		} else {
			// revert selection
			this.selectedButton.setSelected(true);
		}
	}

	public void selectView(EditingTarget editingTarget) {
		Enumeration<AbstractButton> elements = buttonGroup.getElements();

		while (elements.hasMoreElements()) {
			AbstractButton element = elements.nextElement();
			String toolTipText = element.getToolTipText();

			switch (editingTarget) {
			case CLIENTS -> activateButtonIfMatched(element,
					Configed.getResourceValue("MainFrame.labelClientsConfiguration"), toolTipText);
			case DEPOTS -> activateButtonIfMatched(element, Configed.getResourceValue("depotConfiguration"),
					toolTipText);
			case SERVER -> activateButtonIfMatched(element,
					Configed.getResourceValue("MainFrame.labelServerConfiguration"), toolTipText);
			case DASHBOARD -> activateButtonIfMatched(element, Configed.getResourceValue("Dashboard.title"),
					toolTipText);
			case OPSI_MODULES -> activateButtonIfMatched(element,
					Configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), toolTipText);
			case HEALTH_CHECK -> activateButtonIfMatched(element,
					Configed.getResourceValue("MainFrame.jMenuHelpCheckHealth"), toolTipText);
			case LICENSE_MANAGEMENT -> activateButtonIfMatched(element,
					Configed.getResourceValue("MainFrame.labelLicenses"), toolTipText);
			default -> Logging.warning(this, "no case found for editingTarget in selectView");
			}
		}
	}

	private static void activateButtonIfMatched(AbstractButton button, String expectedText, String actualText) {
		if (actualText.equals(expectedText)) {
			button.setSelected(true);
			button.doClick();
		}
	}
}
