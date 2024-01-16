/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.utilities.observer.swing.AbstractValueChangeListener;

public class TextInputField extends JPanel {
	private JTextField textfield;
	private JComboBox<String> combo;
	private List<String> proposedValues;
	private Character[] orderedBeginChars;

	public enum InputType {
		TEXT, DATE, VALUELIST
	}

	private InputType inputType;

	public TextInputField(String initialValue) {
		this(initialValue, null);
	}

	public TextInputField(String initialValue, final List<String> proposedValues) {
		super(new BorderLayout());

		String initValue = initialValue;

		inputType = InputType.VALUELIST;

		if (proposedValues == null) {
			this.proposedValues = new ArrayList<>();

			if (initialValue == null) {
				inputType = InputType.DATE;
				initValue = "";
			} else {
				inputType = InputType.TEXT;
			}
		} else {
			this.proposedValues = proposedValues;
			proposedValues.add(0, "");
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

		textfield = new JTextField(initValue);
		textfield.getCaret().setBlinkRate(0);

		combo = new AutoCompletionComboBox<>();
		combo.setModel(new DefaultComboBoxModel<>(this.proposedValues.toArray(new String[0])));
		((JTextField) combo.getEditor().getEditorComponent()).getCaret().setBlinkRate(0);

		if (inputType == InputType.VALUELIST) {
			super.add(combo);
		} else {
			super.add(textfield);
		}
	}

	public void addValueChangeListener(AbstractValueChangeListener listener) {
		combo.addActionListener(listener);
		textfield.getDocument().addDocumentListener(listener);
	}

	public void setEditable(boolean b) {
		textfield.setEditable(b);
		combo.setEditable(b);
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
		if (inputType == InputType.VALUELIST) {
			return combo.getSelectedItem().toString();
		} else {
			return textfield.getText();
		}
	}
}
