/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.pdf;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.utilities.logging.Logging;

public class OpenSaveDialog implements ActionListener {

	private JButton openBtn;
	private JButton saveBtn;
	private Boolean saveAction;
	private GeneralFrame dialogView;

	public OpenSaveDialog(String title) {

		saveBtn = new JButton(Configed.getResourceValue("OpenSaveDialog.save"));

		if (!Main.FONT) {
			saveBtn.setFont(Globals.DEFAULT_FONT);
		}

		saveBtn.addActionListener(this);

		openBtn = new JButton(Configed.getResourceValue("OpenSaveDialog.open"));

		if (!Main.FONT) {
			openBtn.setFont(Globals.DEFAULT_FONT);
		}
		openBtn.addActionListener(this);

		JPanel buttonPane = new JPanel();
		buttonPane.add(saveBtn);
		buttonPane.add(openBtn);
		JPanel qPanel = new JPanel();

		qPanel.add(buttonPane);
		dialogView = new GeneralFrame(null, Globals.APPNAME + " " + title, true); // modal
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

	private void leave() {
		dialogView.setVisible(false);
		dialogView.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openBtn) {
			saveAction = false;
			leave();

		} else if (e.getSource() == saveBtn) {
			saveAction = true;
			leave();
		} else {
			Logging.warning(this, "unexpected action event on source " + e.getSource());
		}
	}
}
