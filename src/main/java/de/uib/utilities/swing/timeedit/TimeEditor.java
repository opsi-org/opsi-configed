package de.uib.utilities.swing.timeedit;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class TimeEditor extends JPanel {
	JLabel labelTime;
	JSpinner spinnerHour;
	JSpinner spinnerMin;

	List<String> hours;
	List<String> mins;

	public TimeEditor() {
		this(0, 0);
	}

	public TimeEditor(int hours, int minutes) {
		super();
		init();

		setTime(hours, minutes);
	}

	private void setTime(int hours, int minutes) {
		setHour(hours);
		setMin(minutes);
	}

	private String fillTo2Chars(int i) {
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
		labelTime = new JLabel(" hh:mm");
		labelTime.setFont(Globals.defaultFontBig);

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
		} catch (Exception ex) {
			Logging.debug("Time Editor exception " + spinnerHour.getValue() + ", " + ex);
		}

		return result;
	}

	public int getMin() {
		int result = 0;

		try {
			result = Integer.parseInt((String) spinnerMin.getValue());
		} catch (Exception ex) {
			Logging.debug("Time Editor exception  " + spinnerMin.getValue() + ", " + ex);
		}

		return result;
	}

}
