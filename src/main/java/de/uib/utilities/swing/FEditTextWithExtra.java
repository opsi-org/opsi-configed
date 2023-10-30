/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FEditTextWithExtra extends FEditText {
	private JTextField extraField;

	public FEditTextWithExtra(String initialText, String hint, String extraName) {
		super(initialText, hint);
		initFEditTextWithExtra(extraName);
		super.setSingleLine(false);
	}

	private void initFEditTextWithExtra(String extraName) {
		JPanel extraPanel = new JPanel();
		JLabel extraLabel = new JLabel(extraName);
		extraField = new JTextField();
		extraField.setColumns(20);
		extraPanel.add(extraLabel);
		extraPanel.add(extraField);
		editingArea.add(extraPanel, BorderLayout.NORTH);

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
		return extraField.getText();
	}
}
