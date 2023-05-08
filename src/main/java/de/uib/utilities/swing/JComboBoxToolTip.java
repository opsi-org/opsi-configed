package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class JComboBoxToolTip extends JComboBox<String> {

	private Map<String, String> selectValues;

	protected Color listBackgroundColorSelected;
	protected Color listBackgroundColorUnselected;
	protected Color listForegroundColor;

	protected boolean addEmpty;

	private List<String> tooltips = new ArrayList<>();

	public JComboBoxToolTip() {
		super();

		listBackgroundColorSelected = Globals.SECONDARY_BACKGROUND_COLOR;
		listBackgroundColorUnselected = Globals.BACKGROUND_COLOR_3;
		listForegroundColor = Globals.lightBlack;

	}

	protected class NewComboBoxRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (isSelected) {
				if (!ConfigedMain.THEMES) {
					setBackground(listBackgroundColorSelected);
					setForeground(listForegroundColor);
				}
				Logging.debug(this, "index, tooltips " + index + ", " + tooltips);
				if (-1 < index && index < tooltips.size()) {
					list.setToolTipText(tooltips.get(index));
				}
			} else {
				if (!ConfigedMain.THEMES) {
					setBackground(listBackgroundColorSelected);
					setForeground(listForegroundColor);
				}
			}
			setFont(list.getFont());

			if (value == null) {
				setText("");
			} else {
				setText(value.toString());
			}

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
		boolean addE = addEmpty && !selectValues.containsKey("");

		this.removeAllItems();

		tooltips = new ArrayList<>();

		if (addE) {
			addItem("");
			tooltips.add("");
		}

		if (selectValues != null) {
			for (Entry<String, String> selectEntry : selectValues.entrySet()) {
				addItem(selectEntry.getKey());
				tooltips.add(selectEntry.getValue());
			}

		}

	}

}
