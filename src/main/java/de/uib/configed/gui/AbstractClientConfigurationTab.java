/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.CardLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class AbstractClientConfigurationTab extends JPanel {
	private static final String INFO_TAB = "CLIENT_INFO_TAB";
	private static final String CONTENT_TAB = "CLIENT_CONTENT_TAB";

	private boolean multiSelectionAllowed;

	private CardLayout cardLayout;

	protected AbstractClientConfigurationTab(boolean multiSelectionAllowed) {
		this.multiSelectionAllowed = multiSelectionAllowed;

		cardLayout = new CardLayout();
		super.setLayout(cardLayout);
		super.add(generateInfoPanel(), INFO_TAB);
	}

	protected void setComponent(JComponent component) {
		super.add(component, CONTENT_TAB);
	}

	private JPanel generateInfoPanel() {
		String messageKey = multiSelectionAllowed ? "MainFrame.TabRequiresClientSelected"
				: "MainFrame.TabActiveForSingleClient";

		JLabel label = new JLabel(Configed.getResourceValue(messageKey));
		JPanel panel = new JPanel();
		panel.add(label);
		return panel;
	}

	protected void updateContent() {
		// this is used to update the content if needed when switching to this tab
	}

	public void updateTab(int numberOfSelectedClients) {
		if (numberOfSelectedClients == 1 || (numberOfSelectedClients > 1 && multiSelectionAllowed)) {
			updateContent();
			cardLayout.show(this, CONTENT_TAB);
		} else {
			cardLayout.show(this, INFO_TAB);
		}
	}
}
