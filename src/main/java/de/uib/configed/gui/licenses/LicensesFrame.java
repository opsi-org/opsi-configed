/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.LicensesTabStatus;
import de.uib.utils.swing.SecondaryFrame;

public class LicensesFrame extends SecondaryFrame {
	private JTabbedPane jTabbedPaneMain;

	private List<LicensesTabStatus> tabOrder;

	public LicensesFrame(ConfigedMain configedMain) {
		super();
		super.add(createPanel(configedMain));
	}

	@Override
	public void start() {
		setVisible(true);
		setExtendedState(Frame.NORMAL);
	}

	private JPanel createPanel(ConfigedMain configedMain) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 400);

		panel.setLayout(new BorderLayout());
		jTabbedPaneMain = new JTabbedPane(SwingConstants.TOP);

		tabOrder = new ArrayList<>();

		jTabbedPaneMain.addChangeListener((ChangeEvent changeEvent) -> {
			int newVisualIndex = jTabbedPaneMain.getSelectedIndex();

			LicensesTabStatus newS = tabOrder.get(newVisualIndex);

			// report state change request to controller and look, what it produces
			LicensesTabStatus s = configedMain.reactToStateChangeRequest(newS);

			// if the controller did not accept the new index set it back
			// observe that we get a recursion since we initiate another state change
			// the recursion breaks since newVisualIndex is identical with
			// the old and does not yield a different value
			if (newS != s) {
				jTabbedPaneMain.setSelectedIndex(tabOrder.indexOf(s));
			}
		});

		panel.add(jTabbedPaneMain, BorderLayout.CENTER);
		return panel;
	}

	public void addTab(LicensesTabStatus s, String title, Component c) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(title, c);
	}

	public void removeTab(LicensesTabStatus s) {
		int tabIndex = tabOrder.indexOf(s);
		if (tabIndex > 0) {
			jTabbedPaneMain.remove(tabIndex);
			tabOrder.remove(tabIndex);
		}
	}
}
