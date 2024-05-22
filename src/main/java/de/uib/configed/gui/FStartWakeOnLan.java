/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.observer.RunningInstances;
import de.uib.utils.thread.WaitingSleeper;
import de.uib.utils.thread.WaitingWorker;

public class FStartWakeOnLan extends FGeneralDialog implements WaitingSleeper {
	private static final String SCHEDULE_TITLE_STARTER = Configed.getResourceValue("FStartWakeOnLan.creation");

	public static final RunningInstances<FStartWakeOnLan> runningInstances = new RunningInstances<>(
			FStartWakeOnLan.class, Configed.getResourceValue("RunningInstances.askStop.text"));

	private Map<String, Integer> labelledDelays;
	private JSpinner spinnerDelay;
	private JSpinner spinnerHour;
	private JSpinner spinnerMinute;
	private JLabel jLabelStarttime;
	private JTextField fieldTaskname;
	private JLabel jLabelClientCount;
	private JLabel jLabelTimeYetToWait;
	private JProgressBar waitingProgressBar;

	private IconButton buttonRefreshTime;
	private JButton buttonSetNew;

	private Calendar cal;
	private long startActionMillis;
	private long waitingMillis;

	private boolean waitingMode;

	private String nullDelayValue;

	private List<String> currentlySelectedClients;

	private WaitingWorker waitingTask;

	private String scheduleTitle;

	private ConfigedMain configedMain;

	public FStartWakeOnLan(String title, ConfigedMain configedMain) {
		super(null, title, false, new String[] { Configed.getResourceValue("buttonClose"),
				Configed.getResourceValue("FStartWakeOnLan.start") }, 750, 350);
		this.configedMain = configedMain;

		setCalToNow();
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	public void setPredefinedDelays(Map<String, Integer> labelledDelays) {
		this.labelledDelays = labelledDelays;
		List<String> delays = new LinkedList<>(labelledDelays.keySet());

		spinnerDelay.setModel(new SpinnerListModel(delays));
		nullDelayValue = delays.get(0);
		((JSpinner.ListEditor) spinnerDelay.getEditor()).getTextField().setEditable(false);
	}

	public void setClients() {
		currentlySelectedClients = configedMain.getSelectedClients();

		int clientCount;
		if (currentlySelectedClients != null) {
			clientCount = currentlySelectedClients.size();
		} else {
			clientCount = 0;
		}
		Logging.info(this, "clientcount " + clientCount);
		jLabelClientCount.setText("" + clientCount);
	}

	private void disableSettingOfTimes() {
		jButton2.setEnabled(false);
		spinnerDelay.setEnabled(false);
		spinnerDelay.setEnabled(false);
		buttonRefreshTime.setEnabled(false);
		buttonSetNew.setEnabled(false);
		spinnerHour.setEnabled(false);
		spinnerMinute.setEnabled(false);
	}

	private static String formatNaturalNumber(long n) {
		if (n < 10) {
			return "0" + n;
		} else {
			return "" + n;
		}
	}

	private String readTime(Calendar cal) {
		String result = " (" + cal.get(Calendar.YEAR) + "-" + formatNaturalNumber(cal.get(Calendar.MONTH) + 1L) + "-"
				+ formatNaturalNumber(cal.get(Calendar.DAY_OF_MONTH)) + ") "
				+ formatNaturalNumber(cal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ formatNaturalNumber(cal.get(Calendar.MINUTE));
		Logging.info(this, "readTime " + result);

		return result;
	}

	private void setCalToNow() {
		cal = new GregorianCalendar();
	}

	private void produceTargetTime(int changedField, int reqValue) {
		cal.set(changedField, reqValue);

		Calendar now = new GregorianCalendar();
		if (cal.before(now)) {
			cal = now;
		}

		int newMinute = cal.get(Calendar.MINUTE);
		int newHour = cal.get(Calendar.HOUR_OF_DAY);

		if (newMinute != Integer.valueOf(spinnerMinute.getValue().toString())) {
			spinnerMinute.setValue(cal.get(Calendar.MINUTE));
		}

		if (newHour != Integer.valueOf(spinnerHour.getValue().toString())) {
			spinnerHour.setValue(cal.get(Calendar.HOUR_OF_DAY));
		}

		jLabelStarttime.setText(readTime(cal));
	}

	private void setNowTimeAsTarget() {
		setCalToNow();

		fieldTaskname.setText(SCHEDULE_TITLE_STARTER + " " + readTime(cal));

		spinnerHour.setValue(cal.get(Calendar.HOUR_OF_DAY));
		spinnerMinute.setValue(cal.get(Calendar.MINUTE));
	}

	@Override
	protected void initComponents() {
		super.checkAdditionalPane();

		JPanel contentPane = new JPanel();

		scrollpane.setViewportView(contentPane);

		jLabelTimeYetToWait = new JLabel(giveTimeSpan(0), SwingConstants.RIGHT);
		jLabelTimeYetToWait.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.timeLeft.toolTip"));

		waitingProgressBar = new JProgressBar();
		waitingProgressBar.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.timeElapsed.toolTip"));
		waitingProgressBar.setValue(0);
		waitingProgressBar.setEnabled(true);

		fieldTaskname = new JTextField();

		jLabelClientCount = new JLabel();

		int clientCountWidth = 100;

		spinnerHour = new JSpinner();
		JFormattedTextField textFieldHour = ((JSpinner.DefaultEditor) spinnerHour.getEditor()).getTextField();
		textFieldHour.setEditable(false);

		spinnerMinute = new JSpinner();
		JFormattedTextField textFieldMinute = ((JSpinner.DefaultEditor) spinnerMinute.getEditor()).getTextField();
		textFieldMinute.setEditable(false);

		InternationalFormatter internationalFormatter = new InternationalFormatter(new DecimalFormat("00"));

		((DefaultFormatterFactory) textFieldHour.getFormatterFactory()).setDefaultFormatter(internationalFormatter);
		((DefaultFormatterFactory) textFieldMinute.getFormatterFactory()).setDefaultFormatter(internationalFormatter);

		buttonSetNew = new JButton(Utils.getIntellijIcon("refresh"));
		buttonSetNew.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.buttonSetNew.tooltip"));

		buttonSetNew.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionPerformed");
			setClients();
		});

		buttonRefreshTime = new IconButton(Configed.getResourceValue("FStartWakeOnLan.buttonRefreshTime"),
				"images/clock16.png", "images/clock16.png", "images/clock16.png");

		buttonRefreshTime.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.buttonRefreshTime"));

		buttonRefreshTime.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionPerformed");
			setNowTimeAsTarget();
		});

		setNowTimeAsTarget();

		JLabel labelColon = new JLabel(":");
		JLabel labelDelay = new JLabel(Configed.getResourceValue("FStartWakeOnLan.delay"));
		JLabel labelStartdelay = new JLabel(Configed.getResourceValue("FStartWakeOnLan.setTime"));
		JLabel labelStartAt = new JLabel(Configed.getResourceValue("FStartWakeOnLan.resultingStartTime"));
		JLabel labelClientCount = new JLabel(Configed.getResourceValue("FStartWakeOnLan.countOfClients"));

		jLabelStarttime = new JLabel(readTime(cal));

		spinnerDelay = new JSpinner();

		JPanel panelTimeSelection = new JPanel();

		GroupLayout lPanelTimeSelection = new GroupLayout(panelTimeSelection);

		panelTimeSelection.setLayout(lPanelTimeSelection);

		lPanelTimeSelection.setVerticalGroup(lPanelTimeSelection.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(spinnerHour, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(labelColon, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(spinnerMinute, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT));

		lPanelTimeSelection.setHorizontalGroup(lPanelTimeSelection.createSequentialGroup()
				.addComponent(spinnerHour, Globals.TIME_SPINNER_WIDTH, Globals.TIME_SPINNER_WIDTH,
						Globals.TIME_SPINNER_WIDTH)
				.addGap(Globals.GAP_SIZE).addComponent(labelColon, Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(spinnerMinute, Globals.TIME_SPINNER_WIDTH, Globals.TIME_SPINNER_WIDTH,
						Globals.TIME_SPINNER_WIDTH));

		spinnerHour.addChangeListener(changeEvent -> produceTargetTime(Calendar.HOUR_OF_DAY,
				Integer.valueOf(spinnerHour.getValue().toString())));

		spinnerMinute.addChangeListener(changeEvent -> produceTargetTime(Calendar.MINUTE,
				Integer.valueOf(spinnerMinute.getValue().toString())));

		GroupLayout lPanel = new GroupLayout(contentPane);
		contentPane.setLayout(lPanel);

		lPanel.setVerticalGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(fieldTaskname,
						Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonRefreshTime, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(buttonSetNew, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelDelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(spinnerDelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(panelTimeSelection, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelStartdelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelStartAt, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jLabelStarttime, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelClientCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jLabelClientCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)

				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(waitingProgressBar, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT,
								Globals.PROGRESS_BAR_HEIGHT)
						.addComponent(jLabelTimeYetToWait, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)));

		lPanel.setHorizontalGroup(lPanel.createParallelGroup()
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(fieldTaskname, 2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH,
								3 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addGap(2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH)
						.addComponent(buttonSetNew, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH / 2,
								Globals.BUTTON_WIDTH / 2)
						.addComponent(buttonRefreshTime, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH / 2,
								Globals.BUTTON_WIDTH / 2)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(spinnerDelay, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelDelay, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
								3 * Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(panelTimeSelection, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE).addComponent(labelStartdelay, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGap(Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelStartAt, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jLabelStarttime, 1 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE).addGap(Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelClientCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jLabelClientCount, clientCountWidth, clientCountWidth, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))

				.addGroup(lPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(waitingProgressBar, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(jLabelTimeYetToWait, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
						.addGap(Globals.GAP_SIZE)));
	}

	private void startAction() {
		waitingMode = false;

		if (spinnerDelay.getValue() == nullDelayValue) {
			configedMain.wakeUp(currentlySelectedClients, scheduleTitle);
		} else {
			configedMain.wakeUpWithDelay(labelledDelays.get(spinnerDelay.getValue()), currentlySelectedClients,
					scheduleTitle);
		}

		leave();
	}

	private void startWaiting() {
		if (waitingTask != null) {
			waitingTask.stop();
		}

		waitingMode = true;
		Logging.info(this, "startWaiting " + runningInstances);
		runningInstances.add(this, scheduleTitle);

		waitingTask = new WaitingWorker(this);

		waitingTask.execute();
	}

	private static String giveTimeSpan(final long millis) {
		long seconds;
		long remseconds;
		String remSecondsS;
		long minutes;
		long remminutes;
		String remMinutesS;
		long hours;
		String hoursS;

		seconds = millis / 1000;
		minutes = seconds / 60;
		remseconds = seconds % 60;

		hours = minutes / 60;
		remminutes = minutes % 60;

		remSecondsS = formatlNumberUpTo99(remseconds);
		remMinutesS = formatlNumberUpTo99(remminutes);
		hoursS = formatlNumberUpTo99(hours);

		return hoursS + ":" + remMinutesS + ":" + remSecondsS;
	}

	private static String formatlNumberUpTo99(long n) {
		if (n < 10) {
			return "0" + n;
		} else {
			return "" + n;
		}
	}

	// implementing WaitingSleeper
	@Override
	public void actAfterWaiting() {
		startAction();
	}

	@Override
	public JProgressBar getProgressBar() {
		return waitingProgressBar;
	}

	@Override
	public JLabel getLabel() {
		return jLabelTimeYetToWait;
	}

	@Override
	public long getStartActionMillis() {
		return startActionMillis;
	}

	@Override
	public long getWaitingMillis() {
		return waitingMillis;
	}

	@Override
	public long getOneProgressBarLengthWaitingMillis() {
		return getWaitingMillis();
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		return "   " + giveTimeSpan(getWaitingMillis() - millisLevel);
	}

	@Override
	public void doAction2() {
		Logging.info(this, "doAction2");

		if (currentlySelectedClients == null || currentlySelectedClients.isEmpty()) {
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("FStartWakeOnLan.noClientsSelected.text"),
					Configed.getResourceValue("FStartWakeOnLan.noClientsSelected.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		disableSettingOfTimes();

		scheduleTitle = fieldTaskname.getText();
		if (scheduleTitle.startsWith(SCHEDULE_TITLE_STARTER)) {
			scheduleTitle = scheduleTitle.substring(SCHEDULE_TITLE_STARTER.length());
		}

		scheduleTitle = "  scheduler" + scheduleTitle;
		startActionMillis = System.currentTimeMillis();

		waitingMillis = cal.getTimeInMillis() - startActionMillis;

		if (waitingMillis < 0) {
			waitingMillis = 0;
		}

		if (waitingMillis < 100) {
			startAction();
		} else {
			startWaiting();
		}
	}

	@Override
	public void leave() {
		boolean reallyLeave = true;
		Logging.info(this, "leave  with runningInstances " + runningInstances);
		Logging.info(this, "leave  waitingMode  " + waitingMode);

		if (waitingMode) {
			int returnedOption = JOptionPane.showConfirmDialog(this,
					Configed.getResourceValue("FStartWakeOnLan.allowClose"),
					Configed.getResourceValue("FStartWakeOnLan.allowClose.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			reallyLeave = returnedOption == JOptionPane.YES_OPTION;
		}

		Logging.info(this, "leave  reallyLeave  " + reallyLeave);
		if (reallyLeave) {
			Logging.info(this, "waitingTask != null " + (waitingTask != null));
			if (waitingTask != null) {
				waitingTask.stop();
			}

			runningInstances.forget(this);

			super.leave();
		}
	}

	@Override
	public void doAction1() {
		Logging.info(this, "doAction2");
		leave();
	}
}
