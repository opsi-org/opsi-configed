package de.uib.configed.gui.ssh;





import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class SSHCompletionComboBox<E> extends JComboBox {

	public SSHCompletionComboBox(ComboBoxModel<E> cbm) {
		super(cbm);
	}

	public SSHCompletionComboBox(E[] v) {
		super(v);
	}

	public SSHCompletionComboBox(Vector<E> v) {
		super(v);
	}

	@Override
	public void setSelectedItem(Object item) {
		super.setSelectedItem(item);
		ComboBoxEditor editor = getEditor();
		JTextField textField = (JTextField) editor.getEditorComponent();
		textField.setCaretPosition(textField.getText().length());
	}
}