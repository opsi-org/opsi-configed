
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
