package de.uib.utilities.swing;

public class JTextShowField extends javax.swing.JTextField {

	public JTextShowField(boolean editable) {
		this("", editable);
	}

	public JTextShowField(String s) {
		this(s, false);
	}

	public JTextShowField(String s, boolean editable) {
		super(s);
		setEditable(editable);
	}

	public JTextShowField() {
		this("");
	}

	public void setText(String s) {
		super.setText(s);
		setCaretPosition(0);
		setToolTipText(s);
	}
}
