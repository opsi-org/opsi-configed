/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public abstract class AbstractConfigurationTab extends JPanel {
	private static final String INFO_TAB = "CLIENT_INFO_TAB";
	private static final String CONTENT_TAB = "CLIENT_CONTENT_TAB";

	private boolean multiSelectionAllowed;
	private boolean clientConfiguration;

	private CardLayout cardLayout;

	protected AbstractConfigurationTab(boolean multiSelectionAllowed, boolean clientConfiguration) {
		this.multiSelectionAllowed = multiSelectionAllowed;
		this.clientConfiguration = clientConfiguration;

		cardLayout = new CardLayout();
		super.setLayout(cardLayout);
		super.add(generateInfoPanel(), INFO_TAB);
	}

	protected void setComponent(JComponent component) {
		super.add(component, CONTENT_TAB);
	}

	private String getLabel(boolean primary) {
		String clientOrDepot = clientConfiguration ? "Client" : "Depot";
		String singleSelection = multiSelectionAllowed ? "Selected" : "SingleSelection";
		String primaryOrSecondary = primary ? "primary" : "secondary";

		return "Configuration.requires" + clientOrDepot + singleSelection + "." + primaryOrSecondary;
	}

	private JPanel generateInfoPanel() {
		JLabel labelPrimary = new JLabel(Configed.getResourceValue(getLabel(true)));
		labelPrimary.setFont(labelPrimary.getFont().deriveFont(Font.BOLD));
		JLabel labelSecondary = new JLabel(Configed.getResourceValue(getLabel(false)));

		JPanel innerPanel = new JPanel(new MigLayout("wrap 1, aligny center, alignx center", "[center]", "[]10[]"));
		innerPanel.add(labelPrimary);
		innerPanel.add(labelSecondary);

		// We need this panel so that the outer panel can fill the available space
		// and center the inner panel
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new MigLayout("fill"));
		outerPanel.add(innerPanel, "grow, center");

		return outerPanel;
	}

	// this is used to update the content if needed when switching to this tab
	protected abstract void updateContent();

	public void updateTab(int numberOfSelectedClients) {
		if (numberOfSelectedClients == 1 || (numberOfSelectedClients > 1 && multiSelectionAllowed)) {
			updateContent();
			cardLayout.show(this, CONTENT_TAB);
		} else {
			cardLayout.show(this, INFO_TAB);
		}
	}
}
