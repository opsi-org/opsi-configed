/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class FEditStringList extends FEditList<String> {
	public FEditStringList() {
		super();
	}

	private void addElementFromExtraField(String element) {
		addElement(element);
		extraFieldChanged(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		}
	}
}
