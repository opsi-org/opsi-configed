package de.uib.utilities.swing;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FEditTextWithExtra extends FEditText {

	protected JPanel extra_panel;
	protected JLabel extra_label;
	protected JTextField extra_field;

	public FEditTextWithExtra(String initialText, String hint, String extra_name) {
		super(initialText, hint);
		initFEditTextWithExtra(extra_name);
		setSingleLine(false);
	}

	protected void initFEditTextWithExtra(String extra_name) {
		extra_panel = new JPanel();
		extra_label = new JLabel(extra_name);
		extra_field = new JTextField();
		extra_field.setColumns(20);
		extra_panel.add(extra_label);
		extra_panel.add(extra_field);
		editingArea.add(extra_panel, BorderLayout.NORTH);

		scrollpane = new JScrollPane();
		textarea = new JTextArea();
		scrollpane.setViewportView(textarea);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		textarea.setEditable(true);
		textarea.addKeyListener(this);
		textarea.addMouseListener(this);
		textarea.getDocument().addDocumentListener(this);
		setStartText(this.initialText);
	}

	public String getExtra() {
		return extra_field.getText();
	}
}
