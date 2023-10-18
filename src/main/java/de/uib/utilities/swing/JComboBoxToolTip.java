/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import de.uib.utilities.logging.Logging;

public class JComboBoxToolTip extends JComboBox<String> {

	private Map<String, String> selectValues;

	private boolean addEmpty;

	private List<String> tooltips = new ArrayList<>();

	public JComboBoxToolTip() {
		super();
	}

	private class NewComboBoxRenderer extends BasicComboBoxRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (isSelected) {

				Logging.debug(this, "index, tooltips " + index + ", " + tooltips);
				if (-1 < index && index < tooltips.size()) {
					list.setToolTipText(tooltips.get(index));
				}
			}

			if (value == null) {
				setText("");
			} else {
				setText(value.toString());
			}

			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	private void setToolTips() {
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

	private void setComboValues() {
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
