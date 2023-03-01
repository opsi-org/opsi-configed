/*
 * FEditStringList.java
 * 
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2023 uib.de
 *
 * This class represents a Frame so select certain values from a List, that
 * are all of the String type. We need to extend FEditList in another class
 * to add the possibility to add elements to the List from a Text Element. 
 * With other types than String this cannot be possible
 *
 * @author otto
 */

package de.uib.utilities.swing;

import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

import de.uib.utilities.table.gui.SensitiveCellEditor;

public class FEditStringList extends FEditList<String> {

	public FEditStringList() {
		super();
	}

	public FEditStringList(JTextComponent tracker, SensitiveCellEditor celleditor) {
		super(tracker, celleditor);
	}

	private void addElementFromExtraField(String element) {

		addElement(element);

		// ever event
		extraFieldChanged(false);
	}

	// interface ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		}
	}

	// interface KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		}
	}
}
