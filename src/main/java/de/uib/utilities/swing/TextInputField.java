/*
*	TextInputField.java
*	(c) uib 2012
*	GPL licensed
*   Author Rupert Röder
*/

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextInputField extends JPanel
// common wrapper class for JTextField and JCombBox
{

	protected JTextField textfield;
	protected JComboBox combo;
	protected Vector<String> proposedValues;
	private Character[] orderedBeginChars;

	public enum InputType {
		TEXT, DATE, VALUELIST
	};

	private InputType inputType;

	public TextInputField(String initialValue) {
		this(initialValue, null);
	}

	public TextInputField(String initialValue, final Vector<String> proposedValues) {
		super(new BorderLayout());

		String initValue = initialValue;

		inputType = InputType.VALUELIST;

		if (proposedValues == null) {
			this.proposedValues = new Vector<>();;

			if (initialValue == null) {
				inputType = InputType.DATE;
				initValue = "";
			}

			else
				inputType = InputType.TEXT;

		}

		else {
			this.proposedValues = proposedValues;
			proposedValues.add(0, "");
		}

		if (proposedValues != null) {

			TreeSet<Character> orderedValues = new TreeSet<>();

			for (String val : proposedValues) {
				if (val.length() > 0)
					orderedValues.add(val.charAt(0));
			}

			orderedBeginChars = new Character[orderedValues.size()];

			int i = 0;
			for (Character ch : orderedValues) {
				orderedBeginChars[i] = ch;
				i++;
			}
		}

		combo = new JComboBox<>(this.proposedValues);

		JTextField comboField = (JTextField) combo.getEditor().getEditorComponent();
		comboField.getCaret().setBlinkRate(0);

		comboField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String s = comboField.getText();

				if (s.length() == 0) {
					combo.showPopup();

					if (orderedBeginChars != null) {

						Character ch = e.getKeyChar();

						int i = 0;
						boolean stop = false;
						while (i < orderedBeginChars.length && !stop) {

							if (orderedBeginChars[i] > ch) {
								stop = true;
								if (i > 0) {
									i--;
								}
								ch = orderedBeginChars[i];
							} else
								i++;
						}

						if (!stop && i > 0)
							ch = orderedBeginChars[i - 1];

						combo.selectWithKeyChar(ch);
						// advance to last entry with ch??
						if (e.getKeyChar() > ch) {
							int j = 0;
							boolean located = false;
							while (j < proposedValues.size() && !located) {
								String val = proposedValues.get(j);
								if (val.length() == 0)
									j++;
								else {
									if (val.charAt(0) <= ch) {
										j++;
									} else
									// first occurrence of next char
									{
										if (j > 0)
											j--;
										located = true;
									}
								}
							}

							if (!located)
								j--;

							combo.setSelectedItem(proposedValues.get(j));

						}

					}

					comboField.setText("");
				}

			}
		});

		textfield = new JTextField(initValue);

		textfield.getCaret().setBlinkRate(0);

		if (inputType == InputType.VALUELIST)
			add(combo);
		else
			add(textfield);

	}

	public void addValueChangeListener(de.uib.utilities.observer.swing.ValueChangeListener listener) {
		combo.addActionListener(listener);
		textfield.getDocument().addDocumentListener(listener);
	}

	public boolean isEmpty() {
		if (inputType == InputType.VALUELIST)
			return combo.getSelectedItem() == null || combo.getSelectedItem().toString().isEmpty();

		return textfield.getText().isEmpty();
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
		if (inputType == InputType.VALUELIST)
			return combo.getSelectedItem().toString();
		else
			return textfield.getText();
	}
}
