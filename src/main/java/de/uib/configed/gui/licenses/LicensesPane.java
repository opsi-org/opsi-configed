/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import de.uib.configed.ConfigedMain;
import de.uib.configed.ConfigedMain.LicensesTabStatus;

public class LicensesPane extends JTabbedPane {
	private List<LicensesTabStatus> tabOrder;

	public LicensesPane(ConfigedMain configedMain) {
		super(SwingConstants.TOP);
		tabOrder = new ArrayList<>();

		super.addChangeListener((ChangeEvent changeEvent) -> {
			int newVisualIndex = getSelectedIndex();

			LicensesTabStatus newS = tabOrder.get(newVisualIndex);

			// report state change request to controller and look, what it produces
			LicensesTabStatus s = configedMain.reactToStateChangeRequest(newS);

			// if the controller did not accept the new index set it back
			// observe that we get a recursion since we initiate another state change
			// the recursion breaks since newVisualIndex is identical with
			// the old and does not yield a different value
			if (newS != s) {
				setSelectedIndex(tabOrder.indexOf(s));
			}
		});
	}

	public void addTab(LicensesTabStatus s, String title, Component c) {
		tabOrder.add(s);
		addTab(title, c);
	}
}
