/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FEditText extends FEdit implements DocumentListener {
	protected JScrollPane scrollpane;
	protected JTextArea textarea;
	private JTextField extraField;

	private boolean singleLine;

	public FEditText(String initialText, String hint) {
		super(initialText, hint);
		initFEditText();
		setSingleLine(false);
	}

	public FEditText(String initialText, String hint, String extraName) {
		super(initialText, hint);
		initFEditTextWithExtra(extraName);
		setSingleLine(false);
	}

	public FEditText(String initialText) {
		super(initialText);
		initFEditText();
	}

	private void initFEditText() {
		scrollpane = new JScrollPane();
		textarea = new JTextArea();
		scrollpane.setViewportView(textarea);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		textarea.setEditable(true);
		textarea.addKeyListener(this);
		textarea.getDocument().addDocumentListener(this);
		setStartText(this.initialText);
	}

	private void initFEditTextWithExtra(String extraName) {
		initFEditText();
		JPanel extraPanel = new JPanel();
		JLabel extraLabel = new JLabel(extraName);
		extraField = new JTextField();
		extraField.setColumns(20);
		extraPanel.add(extraLabel);
		extraPanel.add(extraField);
		editingArea.add(extraPanel, BorderLayout.NORTH);
	}

	public final void setSingleLine(boolean b) {
		singleLine = b;
		textarea.setLineWrap(!singleLine);
		textarea.setWrapStyleWord(!singleLine);
	}

	@Override
	public void setStartText(String s) {
		super.setStartText(s);
		textarea.setText(s);
	}

	@Override
	public String getText() {
		textarea.setText(textarea.getText().replace("\t", ""));
		if (singleLine) {
			textarea.setText(textarea.getText().replace("\n", ""));
		}

		// set new initial text for use in processWindowEvent
		initialText = textarea.getText();
		return initialText;
	}

	public String getExtra() {
		return extraField != null ? extraField.getText() : "";
	}

	public void select(int selectionStart, int selectionEnd) {
		textarea.select(selectionStart, selectionEnd);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textarea) {
			if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_TAB) {
				buttonCommit.requestFocusInWindow();
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER && singleLine) {
				commit();
			} else {
				// Do nothing on other Events
			}
		}

		super.keyPressed(e);
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		setDataChanged(true);
	}
}
