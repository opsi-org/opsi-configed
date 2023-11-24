/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.pdf;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.GeneralFrame;

public class OpenSaveDialog {
	private JButton openBtn;
	private JButton saveBtn;
	private Boolean saveAction;
	private GeneralFrame dialogView;

	public OpenSaveDialog(String title) {
		saveBtn = new JButton(Configed.getResourceValue("OpenSaveDialog.save"));
		saveBtn.addActionListener(event -> leave(true));

		openBtn = new JButton(Configed.getResourceValue("OpenSaveDialog.open"));
		openBtn.addActionListener(event -> leave(false));

		JPanel buttonPane = new JPanel();
		buttonPane.add(saveBtn);
		buttonPane.add(openBtn);
		JPanel qPanel = new JPanel();

		qPanel.add(buttonPane);
		dialogView = new GeneralFrame(null, title, true);
		dialogView.addPanel(qPanel);
		dialogView.setSize(new Dimension(400, 90));
		dialogView.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialogView.setVisible(true);
	}

	public Boolean getSaveAction() {
		return this.saveAction;
	}

	public void setVisible() {
		dialogView.setVisible(true);
	}

	private void leave(boolean saveAction) {
		this.saveAction = saveAction;

		dialogView.setVisible(false);
		dialogView.dispose();
	}
}
