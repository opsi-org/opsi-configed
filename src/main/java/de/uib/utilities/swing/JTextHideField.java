package de.uib.utilities.swing;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class JTextHideField extends javax.swing.JPanel {
	JPasswordField invisibleField;
	JTextField visibleField;
	JPanel panel;
	JButton button;
	boolean hiddenMode;
	boolean multiValue;

	class FixedDocument extends PlainDocument {

		private String fixed = "xxx";

		FixedDocument() {
			super();
		}

		void setFixed(String s) {
			fixed = s;
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			super.remove(0, super.getLength());

			super.insertString(0, fixed, a);

		}
	}

	public JTextHideField() {
		invisibleField = new JPasswordField();
		visibleField = new JTextField() {

			@Override
			protected Document createDefaultModel() {
				return new FixedDocument();
			}

		};

		hiddenMode = true;
		multiValue = true;
		visibleField.setVisible(true);
		invisibleField.setVisible(true);
		visibleField.setEnabled(true);
		invisibleField.setEnabled(false);

		button = new JButton(Globals.createImageIcon("images/eye_blue_open.png", "show"));
		button.addActionListener(actionEvent -> {
			if (!multiValue) {
				toggleHidden();
			}
		});
		button.setToolTipText(Configed.getResourceValue("JTextHideField.toggleHide"));

		setupPanel();
		setEnabled(false);
	}

	protected void setupPanel() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(invisibleField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addComponent(visibleField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
				.addComponent(button, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)));

		layout.setVerticalGroup(layout.createParallelGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(invisibleField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(visibleField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addComponent(button, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));
	}

	public void setHidden() {
		if (!hiddenMode) {
			toggleHidden();
		}
	}

	@Override
	public void setEnabled(boolean b) {
		visibleField.setEnabled(b);
		button.setEnabled(b);

	}

	public void setMultiValue(boolean b) {
		Logging.info(this, "************+ setMultiValue " + b);
		multiValue = b;
		if (multiValue) {
			setText("");
			setHidden();
		}
	}

	public void toggleHidden() {
		hiddenMode = !hiddenMode;
		visibleField.setVisible(!hiddenMode);
		invisibleField.setVisible(hiddenMode);
		validate();
		repaint();

	}

	@Override
	public void setBackground(java.awt.Color c) {
		if (visibleField != null) {
			visibleField.setBackground(c);
		}
	}

	public void setText(String s) {
		String s0 = s;
		if (multiValue) {
			s0 = "";
		}

		invisibleField.setText(s0);
		((FixedDocument) (visibleField.getDocument())).setFixed(s0);
		visibleField.setText(s0);
	}

}
