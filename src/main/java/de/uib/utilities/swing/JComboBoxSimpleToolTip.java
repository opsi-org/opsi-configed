package de.uib.utilities.swing;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.uib.configed.Globals;

public class JComboBoxSimpleToolTip extends javax.swing.JComboBox {

	protected int FILL_LENGTH = 40;

	public JComboBoxSimpleToolTip() {
		super(); // as it is
		setRenderer(new MyComboBoxRenderer());
	}

	protected class MyComboBoxRenderer extends BasicComboBoxRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			String val = (value == null) ? "" : value.toString();
			setText(val);

			String tooltipText = Globals.fillStringToLength(val + " ", FILL_LENGTH);

			setToolTipText(tooltipText);

			return this;
		}
	}

}
