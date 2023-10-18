/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.timeedit;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import de.uib.utilities.logging.Logging;

public class TimeEditor extends JPanel {
	private JSpinner spinnerHour;
	private JSpinner spinnerMin;

	private List<String> hours;
	private List<String> mins;

	public TimeEditor(int hours, int minutes) {
		super();
		init();

		setTime(hours, minutes);
	}

	private void setTime(int hours, int minutes) {
		setHour(hours);
		setMin(minutes);
	}

	private static String fillTo2Chars(int i) {
		String result = "00";
		if (i < 0 || i > 99) {
			return result;
		}

		result = "" + i;

		if (result.length() == 1) {
			result = "0" + result;
		}

		return result;
	}

	private void init() {
		setOpaque(false);
		setLayout(new GridLayout(1, 2));
		setPreferredSize(new Dimension(250, 22));
		JLabel labelTime = new JLabel(" hh:mm");

		hours = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			hours.add(fillTo2Chars(i));
		}

		spinnerHour = new JSpinner(new SpinnerListModel(hours));

		mins = new ArrayList<>();
		for (int i = 0; i < 60; i++) {
			mins.add(fillTo2Chars(i));
		}

		spinnerMin = new JSpinner(new SpinnerListModel(mins));

		add(labelTime);
		add(spinnerHour);
		add(spinnerMin);
	}

	public void setHour(int h) {
		spinnerHour.getModel().setValue(hours.get(h));
	}

	public void setMin(int m) {
		spinnerMin.getModel().setValue(mins.get(m));
	}

	public int getHour() {
		int result = 0;

		try {
			result = Integer.parseInt((String) spinnerHour.getValue());
		} catch (NumberFormatException ex) {
			Logging.debug("Time Editor exception " + spinnerHour.getValue() + ", " + ex);
		}

		return result;
	}

	public int getMin() {
		int result = 0;

		try {
			result = Integer.parseInt((String) spinnerMin.getValue());
		} catch (NumberFormatException ex) {
			Logging.debug("Time Editor exception  " + spinnerMin.getValue() + ", " + ex);
		}

		return result;
	}
}
