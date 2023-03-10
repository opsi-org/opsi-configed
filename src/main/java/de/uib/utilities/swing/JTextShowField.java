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
		super.setEditable(editable);
	}

	public JTextShowField() {
		this("");
	}

	@Override
	public void setText(String s) {
		super.setText(s);
		setCaretPosition(0);
		setToolTipText(s);
	}
}
