/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.observer.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class AbstractValueChangeListener implements ActionListener, ChangeListener, DocumentListener {
	protected abstract void actOnChange();

	@Override
	public void actionPerformed(ActionEvent event) {
		actOnChange();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		actOnChange();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		actOnChange();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		actOnChange();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		actOnChange();
	}
}
