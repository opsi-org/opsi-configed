package de.uib.utilities.swing;

import java.awt.Component;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class JComboBoxToolTip extends javax.swing.JComboBox {

	private Map<String, String> selectValues;

	protected java.awt.Color listBackgroundColorSelected;
	protected java.awt.Color listBackgroundColorUnselected;
	protected java.awt.Color listForegroundColor;

	protected boolean addEmpty = false;

	public JComboBoxToolTip() {
		super(); // as it is

		listBackgroundColorSelected = Globals.backgroundWhite;
		listBackgroundColorUnselected = Globals.backgroundLightGrey;
		listForegroundColor = Globals.lightBlack;

	}

	Vector<String> tooltips = new Vector<String>();

	protected class NewComboBoxRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (isSelected) {
				setBackground(listBackgroundColorSelected); // list.getSelectionBackground());
				setForeground(listForegroundColor); // list.getSelectionForeground());
				logging.debug(this, "index, tooltips " + index + ", " + tooltips);
				if (-1 < index && index < tooltips.size() // we had an error only on linux with openjdk 8
				) {
					list.setToolTipText(tooltips.get(index));
				}
			} else {
				setBackground(listBackgroundColorSelected); // java.awt.Color.YELLOW); // list.getBackground());
				setForeground(listForegroundColor); // list.getForeground());
			}
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	public void setToolTips() {
		this.setRenderer(new NewComboBoxRenderer());
	}

	public void setValues(Map<String, String> v, boolean addEmpty) {
		this.addEmpty = addEmpty;
		setValues(v);
	}

	public void setValues(Map<String, String> v) {
		selectValues = v;
		setComboValues();

		this.setToolTips();
	}

	protected void setComboValues() {
		Set iterableKeys;
		String iterValue;
		boolean addE = addEmpty && !selectValues.containsKey("");

		this.removeAllItems();

		tooltips = new Vector<String>();

		if (addE) {
			addItem("");
			tooltips.add("");
		}

		if (selectValues != null) {
			for (String key : selectValues.keySet()) {
				addItem(key);
				tooltips.add(selectValues.get(key));
			}

		}

	}

}
