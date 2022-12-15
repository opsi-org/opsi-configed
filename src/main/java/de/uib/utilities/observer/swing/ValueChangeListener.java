package de.uib.utilities.observer.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class ValueChangeListener implements ActionListener, ChangeListener, DocumentListener {
	protected abstract void actOnChange();

	public void actionPerformed(ActionEvent event) {
		actOnChange();
	}

	public void stateChanged(ChangeEvent e) {
		actOnChange();
	}

	public void changedUpdate(DocumentEvent e) {
		actOnChange();
	}

	public void insertUpdate(DocumentEvent e) {
		actOnChange();
	}

	public void removeUpdate(DocumentEvent e) {
		actOnChange();
	}
}
