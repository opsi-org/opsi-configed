/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/* ActionEvent -> parenthesisAction(actionEvent)
	 * A spinner for big numbers, with a metric prefix (kilo, mega, ...) selection.
	 */
public class SpinnerWithExtension extends JPanel {
	private JSpinner spinner;
	private JComboBox<String> box;

	public SpinnerWithExtension() {
		spinner = new JSpinner(
				new SpinnerNumberModel((Number) Long.valueOf(0), Long.MIN_VALUE, Long.MAX_VALUE, Long.valueOf(1)));
		spinner.setMinimumSize(new Dimension());
		box = new JComboBox<>(new String[] { "", "k", "M", "G", "T" });
		box.setMinimumSize(new Dimension(50, 0));
		super.setLayout(new MigLayout("insets 0", "[]0[]", "[]0"));
		super.add(spinner);
		super.add(box);
	}

	public long getValue() {
		long value = (Long) spinner.getValue();
		for (int i = 0; i < box.getSelectedIndex(); i++) {
			value *= 1024L;
		}
		return value;
	}

	public void setValue(long val) {
		spinner.setValue(val);
		box.setSelectedIndex(0);
	}

	public void addChangeListener(ChangeListener listener) {
		spinner.addChangeListener(listener);
	}
}
