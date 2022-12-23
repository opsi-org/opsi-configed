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

	/*
	 * private class MyInputListener extends KeyAdapter
	 * implements ActionListener
	 * {
	 * 
	 * @Override
	 * public void keyPressed(KeyEvent e)
	 * {
	 * logging.info(this, "key pressed " + e.getKeyChar());
	 * }
	 * 
	 * @Override
	 * public void actionPerformed(ActionEvent e)
	 * {
	 * logging.info(this, "action " + e);
	 * }
	 * }
	 */

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

		// logging.info(this, " proposedValues " + proposedValues);

		// if (inputType == InputType.TEXT)
		if (proposedValues != null) {

			TreeSet<Character> orderedValues = new TreeSet<>();

			for (String val : proposedValues) {
				if (val.length() > 0)
					orderedValues.add(val.charAt(0));
			}

			// logging.info(this, " orderedBeginChars " + Arrays.toString( orderedBeginChars
			// ) );

			orderedBeginChars = new Character[orderedValues.size()];

			int i = 0;
			for (Character ch : orderedValues) {
				orderedBeginChars[i] = ch;
				i++;
			}
		}

		// combo = new JComboBoxSearching(this.proposedValues);
		combo = new JComboBox<>(this.proposedValues);
		// logging.debug(this, "class of editor component "
		// + combo.getEditor().getEditorComponent().getClass());

		JTextField comboField = (JTextField) combo.getEditor().getEditorComponent();
		comboField.getCaret().setBlinkRate(0);

		comboField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String s = comboField.getText();
				// logging.info(this, " " + e.getKeyChar() + " content until now >" + s + "< " +
				// e);

				if (s.length() == 0) {
					combo.showPopup();
					// combo.selectWithKeyChar( e.getKeyChar() );

					// if ( s.length() > 0 && s.charAt( s.length() - 1) == e.getKeyChar() )
					// comboField.setText( s.substring(0, s.length() - 2 ) );

					// combo.selectWithKeyChar( e.getKeyChar() );

					// logging.info(this, " orderedBeginChars " + Arrays.toString( orderedBeginChars
					// ) );
					// if (inputType == InputType.TEXT)
					if (orderedBeginChars != null) {

						Character ch = e.getKeyChar();

						int i = 0;
						boolean stop = false;
						while (i < orderedBeginChars.length && !stop) {
							// logging.info(this, " orderedBeginChar compare ch " + ch + " to " +
							// orderedBeginChars[i]);
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

						// logging.info(this, " orderedBeginChar comparisons result " + ch + " input was
						// " + e.getKeyChar());

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

							// if (!located)
							// combo.setSelectedItem( proposedValues.get( proposedValues.size() - 1 ) );
						}

					}
					// e.consume();
					comboField.setText("");
				}

				/*
				 * if (proposedValues != null)
				 * {
				 * for (String val : proposedValues)
				 * {
				 * if (val != null && val.length() > 0 && val.charAt(0) == e.getKeyChar() )
				 * {
				 * combo.setSelectedItem(val);
				 * break;
				 * }
				 * }
				 * }
				 */
			}
		});

		/*
		 * if (inputType == InputType.DATE)
		 * textfield = new JFormattedTextField(Globals.getToday());
		 * 
		 * else
		 */

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
