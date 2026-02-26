/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.share.Utils;

public class TextInputField extends JPanel {
	private JTextField textfield;
	private JComboBox<String> combo;
	private List<String> proposedValues;
	private Character[] orderedBeginChars;

	private boolean isValueList;

	public TextInputField() {
		this(null);
	}

	public TextInputField(final List<String> proposedValues) {
		super(new BorderLayout());

		if (proposedValues == null) {
			this.proposedValues = new ArrayList<>();
			isValueList = false;
		} else {
			this.proposedValues = proposedValues;
			proposedValues.add(0, "");
			isValueList = true;
		}

		if (proposedValues != null) {
			TreeSet<Character> orderedValues = new TreeSet<>();

			for (String val : proposedValues) {
				if (val.length() > 0) {
					orderedValues.add(val.charAt(0));
				}
			}

			orderedBeginChars = new Character[orderedValues.size()];

			int i = 0;
			for (Character ch : orderedValues) {
				orderedBeginChars[i] = ch;
				i++;
			}
		}

		textfield = new JTextField();

		combo = new AutoCompletionComboBox<>();
		combo.setModel(new DefaultComboBoxModel<>(this.proposedValues.toArray(new String[0])));

		if (isValueList) {
			super.add(combo);
		} else {
			super.add(textfield);
		}
	}

	public void setClientSelectionDialog(ClientSelectionDialog dialog) {
		combo.addActionListener(e -> dialog.buildParentheses());
		textfield.getDocument().addDocumentListener(Utils.onDocumentChange(dialog::buildParentheses));
	}

	public void setEditable(boolean b) {
		textfield.setEditable(b);
		combo.setEditable(b);
	}

	public void setReadOnly(boolean readOnly) {
		if (isValueList) {
			combo.setEditable(true);
			JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
			editor.setEditable(!readOnly);
		} else {
			textfield.setEditable(!readOnly);
		}
	}

	@Override
	public void setToolTipText(String s) {
		textfield.setToolTipText(s);
		combo.setToolTipText(s);
	}

	public void setText(String s) {
		combo.setSelectedItem(s);
		textfield.setText(s);
	}

	public String getText() {
		return isValueList ? combo.getSelectedItem().toString() : textfield.getText();
	}
}
