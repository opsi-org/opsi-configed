package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import de.uib.configed.Configed;
/**
 * FStartWakeOnLan
 * Copyright:     Copyright (c) 2015-2016
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.RunningInstances;
import de.uib.utilities.swing.ProgressBarPainter;
import de.uib.utilities.thread.WaitingWorker;

public class FStartWakeOnLan extends FGeneralDialog implements de.uib.utilities.thread.WaitingSleeper {

	public static final RunningInstances<FStartWakeOnLan> runningInstances = new RunningInstances<>(
			FStartWakeOnLan.class, Configed.getResourceValue("RunningInstances.askStop.text"));

	String scheduleTitleStarter;
	private Map<String, Integer> labelledDelays;
	JSpinner spinnerDelay;
	JSpinner spinnerHour;
	JSpinner spinnerMinute;
	JLabel labelStarttime;
	JTextField fieldTaskname;
	JTextField fieldClientCount;
	JTextField fieldInvolvedDepotsCount;
	JLabel labelTimeYetToWait;

	IconButton buttonRefreshTime;
	IconButton buttonSetNew;

	Calendar cal;
	long startActionMillis;
	long waitingMillis;

	int stepsTotal;
	boolean waitingMode;

	List<String> oneDayHours;
	List<String> minutes;
	String nullDelayValue;

	Map<String, List<String>> hostSeparationByDepots;
	Set<String> usedDepots;
	int clientCount;
	String[] currentlySelectedClients;

	private WaitingWorker waitingTask;

	String scheduleTitle;

	ConfigedMain main;

	public FStartWakeOnLan(String title, ConfigedMain main) {
		super(null, title, false, new String[] { Configed.getResourceValue("FStartWakeOnLan.cancel"),
				Configed.getResourceValue("FStartWakeOnLan.start") }, 750, 310);
		this.main = main;

		setCalToNow();
		super.setLocationRelativeTo(Globals.frame1);
	}

	public void setPredefinedDelays(Map<String, Integer> labelledDelays) {
		this.labelledDelays = labelledDelays;
		LinkedList<String> delays = new LinkedList<>(labelledDelays.keySet());

		spinnerDelay.setModel(new SpinnerListModel(delays));
		nullDelayValue = delays.get(0);
		((JSpinner.ListEditor) spinnerDelay.getEditor()).getTextField().setEditable(false);
	}

	public void setClients() {
		hostSeparationByDepots = main.getPersistenceController().getHostSeparationByDepots(main.getSelectedClients());
		usedDepots = hostSeparationByDepots.keySet();
		currentlySelectedClients = main.getSelectedClients();
		if (main.getSelectedClients() != null) {
			clientCount = main.getSelectedClients().length;
		} else {
			clientCount = 0;
		}
		Logging.info(this, "clients count " + clientCount + ", used depots " + usedDepots.size());
		fieldClientCount.setText("" + clientCount);
		fieldInvolvedDepotsCount.setText("" + usedDepots.size());
	}

	protected void disableSettingOfTimes() {
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
		String result = " (" + cal.get(Calendar.YEAR) + "-" + formatNaturalNumber((cal.get(Calendar.MONTH) + 1)) + "-"
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

		labelStarttime.setText(readTime(cal));
	}

	protected void setNowTimeAsTarget() {
		setCalToNow();

		if (scheduleTitleStarter == null) {
			scheduleTitleStarter = Configed.getResourceValue("FStartWakeOnLan.creation");
		}

		fieldTaskname.setText(scheduleTitleStarter + " " + readTime(cal));

		spinnerHour.setValue(cal.get(Calendar.HOUR_OF_DAY));
		spinnerMinute.setValue(cal.get(Calendar.MINUTE));
	}

	@Override
	protected void initComponents() {
		super.checkAdditionalPane();

		JPanel contentPane = new JPanel();
		if (!ConfigedMain.THEMES) {
			contentPane.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		scrollpane.setViewportView(contentPane);

		labelTimeYetToWait = new JLabel(Globals.giveTimeSpan(0), SwingConstants.RIGHT);
		labelTimeYetToWait.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.timeLeft.toolTip"));

		waitingProgressBar = new JProgressBar();
		waitingProgressBar.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.timeElapsed.toolTip"));
		waitingProgressBar.setValue(0);
		waitingProgressBar.setEnabled(true);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new ProgressBarPainter(Globals.opsiLogoLightBlue));
		waitingProgressBar.putClientProperty("Nimbus.Overrides", defaults);

		fieldTaskname = new JTextField();
		fieldTaskname.getDocument().addDocumentListener(new DocumentListener() {

			private void actOnChange() {
				Logging.info(this, "changed text");

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				actOnChange();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				actOnChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				actOnChange();
			}
		});

		fieldClientCount = new JTextField();
		fieldClientCount.setEditable(false);
		fieldInvolvedDepotsCount = new JTextField();
		fieldInvolvedDepotsCount.setEditable(false);

		int clientCountWidth = 100;

		spinnerHour = new JSpinner();
		JFormattedTextField textFieldHour = (((JSpinner.DefaultEditor) spinnerHour.getEditor()).getTextField());
		textFieldHour.setEditable(false);

		spinnerMinute = new JSpinner();
		JFormattedTextField textFieldMinute = (((JSpinner.DefaultEditor) spinnerMinute.getEditor()).getTextField());
		textFieldMinute.setEditable(false);

		InternationalFormatter internationalFormatter = new InternationalFormatter(new DecimalFormat("00"));

		((DefaultFormatterFactory) textFieldHour.getFormatterFactory()).setDefaultFormatter(internationalFormatter);
		((DefaultFormatterFactory) textFieldMinute.getFormatterFactory()).setDefaultFormatter(internationalFormatter);

		buttonSetNew = new IconButton(Configed.getResourceValue("FStartWakeOnLan.buttonSetNew"), "images/reload16.png",
				"images/reload16_over.png", "images/reload16_disabled.png");

		buttonSetNew.setToolTipText(Configed.getResourceValue("FStartWakeOnLan.buttonSetNew.tooltip"));

		buttonSetNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionPerformed");
				setClients();

			}
		});

		buttonRefreshTime = new IconButton(Configed.getResourceValue("FStartWakeOnLan.buttonRefreshTime"),
				"images/clock16.png", "images/clock16.png", "images/clock16.png");

		buttonRefreshTime.setToolTipText("Zeit neu setzen");

		buttonRefreshTime.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionPerformed");
				setNowTimeAsTarget();
			}
		});

		setNowTimeAsTarget();

		JLabel labelColon = new JLabel(":");

		JLabel labelDelay = new JLabel(Configed.getResourceValue("FStartWakeOnLan.delay"));
		JLabel labelStartdelay = new JLabel(Configed.getResourceValue("FStartWakeOnLan.setTime"));
		JLabel labelStartAt = new JLabel(Configed.getResourceValue("FStartWakeOnLan.resultingStartTime"));
		JLabel labelClientCount = new JLabel(Configed.getResourceValue("FStartWakeOnLan.countOfClients"));
		JLabel labelDepotCount = new JLabel(Configed.getResourceValue("FStartWakeOnLan.countOfDepots"));

		labelStarttime = new JLabel(readTime(cal));

		spinnerDelay = new JSpinner();

		JPanel panelTimeSelection = new JPanel();
		panelTimeSelection.setOpaque(false);

		GroupLayout lPanelTimeSelection = new GroupLayout(panelTimeSelection);

		panelTimeSelection.setLayout(lPanelTimeSelection);

		lPanelTimeSelection.setVerticalGroup(lPanelTimeSelection.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(spinnerHour, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(labelColon, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(spinnerMinute, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT));

		lPanelTimeSelection.setHorizontalGroup(lPanelTimeSelection.createSequentialGroup()
				.addComponent(spinnerHour, Globals.TIME_SPINNER_WIDTH, Globals.TIME_SPINNER_WIDTH,
						Globals.TIME_SPINNER_WIDTH)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelColon, Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(spinnerMinute, Globals.TIME_SPINNER_WIDTH, Globals.TIME_SPINNER_WIDTH,
						Globals.TIME_SPINNER_WIDTH));

		spinnerHour.addChangeListener(changeEvent -> produceTargetTime(Calendar.HOUR_OF_DAY,
				Integer.valueOf(spinnerHour.getValue().toString())));

		spinnerMinute.addChangeListener(changeEvent -> produceTargetTime(Calendar.MINUTE,
				Integer.valueOf(spinnerMinute.getValue().toString())));

		JPanel panelSpinnerDelay = new JPanel();
		panelSpinnerDelay.setOpaque(false);
		GroupLayout lPanelSpinnerDelay = new GroupLayout(panelSpinnerDelay);
		panelSpinnerDelay.setLayout(lPanelSpinnerDelay);

		lPanelSpinnerDelay.setVerticalGroup(lPanelSpinnerDelay.createParallelGroup().addComponent(spinnerDelay,
				Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT));
		lPanelSpinnerDelay.setHorizontalGroup(lPanelSpinnerDelay.createSequentialGroup().addComponent(spinnerDelay,
				2 * Globals.TIME_SPINNER_WIDTH, 2 * Globals.TIME_SPINNER_WIDTH, 2 * Globals.TIME_SPINNER_WIDTH));

		GroupLayout lPanel = new GroupLayout(contentPane);
		contentPane.setLayout(lPanel);

		lPanel.setVerticalGroup(lPanel.createSequentialGroup().addGap(Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(fieldTaskname,
						Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonRefreshTime, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(buttonSetNew, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelDelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(panelSpinnerDelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(panelTimeSelection, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelStartdelay, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelStartAt, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelStarttime, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelClientCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldClientCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelDepotCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldInvolvedDepotsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE)

				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(waitingProgressBar, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT,
								Globals.PROGRESS_BAR_HEIGHT)
						.addComponent(labelTimeYetToWait, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)));

		lPanel.setHorizontalGroup(
				lPanel.createParallelGroup()
						.addGroup(lPanel.createSequentialGroup()
								.addGap(Globals.MIN_HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
								.addComponent(fieldTaskname, 2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH,
										3 * Globals.BUTTON_WIDTH)
								.addGap(Globals.MIN_HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
						.addGroup(lPanel.createSequentialGroup()
								.addGap(Globals.MIN_HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
								.addGap(2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH)
								.addComponent(buttonSetNew, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH / 2,
										Globals.BUTTON_WIDTH / 2)
								.addComponent(buttonRefreshTime, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH / 2,
										Globals.BUTTON_WIDTH / 2)
								.addGap(Globals.MIN_HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
						.addGroup(lPanel.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(panelSpinnerDelay, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(labelDelay, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										3 * Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
						.addGroup(lPanel.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(panelTimeSelection, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(labelStartdelay, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(lPanel.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addGap(Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(labelStartAt, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(labelStarttime, 1 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										2 * Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
						.addGroup(lPanel.createSequentialGroup().addGap(Globals.HGAP_SIZE).addGap(Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE)
								.addComponent(labelClientCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2)
								.addComponent(fieldClientCount, clientCountWidth, clientCountWidth, Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(labelDepotCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2)
								.addComponent(fieldInvolvedDepotsCount, clientCountWidth, clientCountWidth,
										Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE))

						.addGroup(lPanel.createSequentialGroup().addGap(Globals.HGAP_SIZE)
								.addComponent(waitingProgressBar, Globals.BUTTON_WIDTH * 2, Globals.BUTTON_WIDTH * 2,
										Short.MAX_VALUE)
								.addGap(Globals.HGAP_SIZE).addComponent(labelTimeYetToWait, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE))

		);

	}

	private void startAction() {
		waitingMode = false;

		if (spinnerDelay.getValue() == nullDelayValue) {
			main.wakeUp(currentlySelectedClients, scheduleTitle);
		} else {
			main.wakeUpWithDelay(labelledDelays.get(spinnerDelay.getValue()), currentlySelectedClients, scheduleTitle);
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
		return labelTimeYetToWait;
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
		return waitingMillis;
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		return " " + Configed.getResourceValue("FStartWakeOnLan.timeLeft") + "  "
				+ Globals.giveTimeSpan(getWaitingMillis() - millisLevel);
	}

	@Override
	public void doAction2() {

		Logging.info(this, "doAction2");

		if (currentlySelectedClients == null || currentlySelectedClients.length == 0) {
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("FStartWakeOnLan.noClientsSelected.text"),
					Configed.getResourceValue("FStartWakeOnLan.noClientsSelected.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		disableSettingOfTimes();

		scheduleTitle = fieldTaskname.getText();
		if (scheduleTitle.startsWith(scheduleTitleStarter)) {
			scheduleTitle = scheduleTitle.substring(scheduleTitleStarter.length());
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
			int returnedOption = JOptionPane.showOptionDialog(this,
					Configed.getResourceValue("FStartWakeOnLan.allowClose"),
					Configed.getResourceValue("FStartWakeOnLan.allowClose.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			reallyLeave = (returnedOption == JOptionPane.YES_OPTION);
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
