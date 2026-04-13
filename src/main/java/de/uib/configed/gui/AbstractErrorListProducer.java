/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.List;

import javax.swing.JOptionPane;

public abstract class AbstractErrorListProducer extends Thread {
	String title;

	AbstractErrorListProducer(String title) {
		this.title = title;
	}

	protected abstract List<String> getErrors();

	@Override
	public void run() {
		List<String> errors = getErrors();

		if (!errors.isEmpty()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					errors.toString().replace("[", "").replace("]", "").replace(",", "\n"), title,
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
