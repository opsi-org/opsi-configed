package de.uib.configed.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

/**
 * FStartWakeOnLan
 * Copyright:     Copyright (c) 2015-2016
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.observer.RunningInstances;
import de.uib.utilities.swing.ProgressBarPainter;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;

//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;

public class FStartWakeOnLan extends FGeneralDialog implements de.uib.utilities.thread.WaitingSleeper
// implements PropertyChangeListener
{
	public static RunningInstances<FStartWakeOnLan> runningInstances = new RunningInstances(FStartWakeOnLan.class,
			configed.getResourceValue("RunningInstances.askStop.text"));
	String scheduleTitleStarter;
	private LinkedHashMap<String, Integer> labelledDelays;
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
	// int waitingSeconds;
	// int sleepingIntervalCount;
	int stepsTotal;
	boolean waitingMode = false;

	LinkedList<String> oneDayHours;
	LinkedList<String> minutes;
	String nullDelayValue;

	Map<String, java.util.List<String>> hostSeparationByDepots;
	Set<String> usedDepots;
	int clientCount;
	String[] currentlySelectedClients;

	private WaitingWorker waitingTask;

	String scheduleTitle;

	ConfigedMain main;

	public FStartWakeOnLan(Frame owner, String title, ConfigedMain main) {
		super(null, title, false, new String[]
		// {"start", "cancel"},
		{ configed.getResourceValue("FStartWakeOnLan.start"), configed.getResourceValue("FStartWakeOnLan.cancel") },
				750, 310);
		this.main = main;
		// scheduleTitleStarter = configed.getResourceValue("FStartWakeOnLan.creation");
		setCalToNow();
		centerOn(de.uib.configed.Globals.frame1);
	}

	public void setPredefinedDelays(LinkedHashMap<String, Integer> labelledDelays) {
		this.labelledDelays = labelledDelays;
		LinkedList<String> delays = new LinkedList<String>(labelledDelays.keySet());
		// comboDelay.setModel(new DefaultComboBoxModel(new
		// Vector<String>(labelledDelays.keySet() ) ) );
		spinnerDelay.setModel(new SpinnerListModel(delays));
		nullDelayValue = delays.get(0);
		((JSpinner.ListEditor) spinnerDelay.getEditor()).getTextField().setEditable(false);
	}

	public void setClients() {
		hostSeparationByDepots = main.getPersistenceController().getHostSeparationByDepots(main.getSelectedClients());
		usedDepots = hostSeparationByDepots.keySet();
		currentlySelectedClients = main.getSelectedClients();
		if (main.getSelectedClients() != null)
			clientCount = main.getSelectedClients().length;
		else
			clientCount = 0;
		logging.info(this, "clients count " + clientCount + ", used depots " + usedDepots.size());
		fieldClientCount.setText("" + clientCount);
		fieldInvolvedDepotsCount.setText("" + usedDepots.size());
	}

	protected void disableSettingOfTimes() {
		jButton1.setEnabled(false);
		spinnerDelay.setEnabled(false);
		spinnerDelay.setEnabled(false);
		buttonRefreshTime.setEnabled(false);
		buttonSetNew.setEnabled(false);
		spinnerHour.setEnabled(false);
		spinnerMinute.setEnabled(false);
	}

	private String formatNaturalNumber(long n) {
		if (n < 10)
			return "0" + n;
		else
			return "" + n;
	}

	private String readTime(Calendar cal) {
		String result = " (" + cal.get(Calendar.YEAR) + "-" + formatNaturalNumber((cal.get(Calendar.MONTH) + 1)) + "-"
				+ formatNaturalNumber(cal.get(Calendar.DAY_OF_MONTH)) + ") "
				+ formatNaturalNumber(cal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ formatNaturalNumber(cal.get(Calendar.MINUTE));
		logging.info(this, "readTime " + result);

		return result;
	}

	private void setCalToNow() {
		cal = new GregorianCalendar();
	}

	private void produceTargetTime(int changedField, int reqValue) {
		cal.set(changedField, reqValue);

		Calendar now = new GregorianCalendar();
		if (cal.before(now))
			cal = now;

		int newMinute = cal.get(Calendar.MINUTE);
		int newHour = cal.get(Calendar.HOUR_OF_DAY);

		if (newMinute != Integer.valueOf(spinnerMinute.getValue().toString()))
			spinnerMinute.setValue(cal.get(Calendar.MINUTE));

		if (newHour != Integer.valueOf(spinnerHour.getValue().toString()))
			spinnerHour.setValue(cal.get(Calendar.HOUR_OF_DAY));

		labelStarttime.setText(readTime(cal));
	}

	protected void setNowTimeAsTarget() {
		setCalToNow();

		if (scheduleTitleStarter == null)
			scheduleTitleStarter = configed.getResourceValue("FStartWakeOnLan.creation");
		fieldTaskname.setText(scheduleTitleStarter + " " + readTime(cal));

		spinnerHour.setValue(cal.get(Calendar.HOUR_OF_DAY));
		spinnerMinute.setValue(cal.get(Calendar.MINUTE));
	}

	@Override
	protected void initComponents() {
		super.checkAdditionalPane();

		JPanel contentPane = new JPanel();
		contentPane.setBackground(de.uib.configed.Globals.backLightBlue);
		// scrollpane = new JScrollPane();
		scrollpane.setViewportView(contentPane);

		labelTimeYetToWait = new JLabel(de.uib.utilities.Globals.giveTimeSpan(0), SwingConstants.RIGHT);
		labelTimeYetToWait.setToolTipText(configed.getResourceValue("FStartWakeOnLan.timeLeft.toolTip"));

		waitingProgressBar = new JProgressBar();
		waitingProgressBar.setToolTipText(configed.getResourceValue("FStartWakeOnLan.timeElapsed.toolTip"));
		waitingProgressBar.setValue(0);
		waitingProgressBar.setEnabled(true);
		// waitingProgressBar.setMaximum(max);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter",
				new ProgressBarPainter(de.uib.configed.Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter",
				new ProgressBarPainter(de.uib.configed.Globals.opsiLogoLightBlue));
		waitingProgressBar.putClientProperty("Nimbus.Overrides", defaults);

		fieldTaskname = new JTextField();
		// JTextField fieldStartTime = new CheckedTimeField("");
		fieldTaskname.getDocument().addDocumentListener(new DocumentListener() {

			private void actOnChange() {
				logging.info(this, "changed text");

			}

			public void changedUpdate(DocumentEvent e) {
				actOnChange();
			}

			public void insertUpdate(DocumentEvent e) {
				actOnChange();
			}

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
		JFormattedTextField textFieldHour = ((JFormattedTextField) ((JSpinner.DefaultEditor) spinnerHour.getEditor())
				.getTextField());
		textFieldHour.setEditable(false);

		spinnerMinute = new JSpinner();
		JFormattedTextField textFieldMinute = ((JFormattedTextField) ((JSpinner.DefaultEditor) spinnerMinute
				.getEditor()).getTextField());
		textFieldMinute.setEditable(false);

		try {
			InternationalFormatter internationalFormatter = new InternationalFormatter(new DecimalFormat("00"));

			((DefaultFormatterFactory) textFieldHour.getFormatterFactory()).setDefaultFormatter(internationalFormatter);
			((DefaultFormatterFactory) textFieldMinute.getFormatterFactory())
					.setDefaultFormatter(internationalFormatter);

		} catch (Exception e) {
		} ;

		buttonSetNew = new IconButton(configed.getResourceValue("FStartWakeOnLan.buttonSetNew"), "images/reload16.png",
				"images/reload16_over.png", "images/reload16_disabled.png");
		// buttonSetNew.setBackground(de.uib.utilities.de.uib.configed.Globals.backgroundLightGrey);
		buttonSetNew.setToolTipText(configed.getResourceValue("FStartWakeOnLan.buttonSetNew.tooltip"));

		buttonSetNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.debug(this, "actionPerformed");
				setClients();
				// setNowTimeAsTarget();
			}
		});

		buttonRefreshTime = new IconButton(configed.getResourceValue("FStartWakeOnLan.buttonRefreshTime"),
				"images/clock16.png", "images/clock16.png", "images/clock16.png");
		// buttonRefreshTime.setBackground(de.uib.utilities.de.uib.configed.Globals.backgroundLightGrey);
		buttonRefreshTime.setToolTipText("Zeit neu setzen");

		buttonRefreshTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logging.debug(this, "actionPerformed");
				setNowTimeAsTarget();
			}
		});

		setNowTimeAsTarget();

		JLabel labelColon = new JLabel(":");

		JLabel labelDelay = new JLabel(configed.getResourceValue("FStartWakeOnLan.delay"));
		JLabel labelStartdelay = new JLabel(configed.getResourceValue("FStartWakeOnLan.setTime"));
		JLabel labelStartAt = new JLabel(configed.getResourceValue("FStartWakeOnLan.resultingStartTime"));
		JLabel labelClientCount = new JLabel(configed.getResourceValue("FStartWakeOnLan.countOfClients"));
		JLabel labelDepotCount = new JLabel(configed.getResourceValue("FStartWakeOnLan.countOfDepots"));

		labelStarttime = new JLabel(readTime(cal));
		// labelStarttime.setFont(de.uib.configed.Globals.defaultFont);
		// labelStarttime.setEditable(false);
		// labelStarttime.setEnabled(false);

		// = new JLabel("delay between triggering WOL events (per depot)");
		spinnerDelay = new JSpinner();

		JPanel panelTimeSelection = new JPanel();
		panelTimeSelection.setOpaque(false);

		GroupLayout lPanelTimeSelection = new GroupLayout(panelTimeSelection);

		panelTimeSelection.setLayout(lPanelTimeSelection);

		lPanelTimeSelection.setVerticalGroup(lPanelTimeSelection.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(spinnerHour, de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT,
						de.uib.configed.Globals.BUTTON_HEIGHT)
				.addComponent(labelColon, de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT,
						de.uib.configed.Globals.BUTTON_HEIGHT)
				.addComponent(spinnerMinute, de.uib.configed.Globals.BUTTON_HEIGHT,
						de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT));

		lPanelTimeSelection.setHorizontalGroup(lPanelTimeSelection.createSequentialGroup()
				.addComponent(spinnerHour, de.uib.configed.Globals.TIME_SPINNER_WIDTH,
						de.uib.configed.Globals.TIME_SPINNER_WIDTH, de.uib.configed.Globals.TIME_SPINNER_WIDTH)
				.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
						de.uib.configed.Globals.HGAP_SIZE)
				.addComponent(labelColon, de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
						de.uib.configed.Globals.HGAP_SIZE)
				.addComponent(spinnerMinute, de.uib.configed.Globals.TIME_SPINNER_WIDTH,
						de.uib.configed.Globals.TIME_SPINNER_WIDTH, de.uib.configed.Globals.TIME_SPINNER_WIDTH));

		spinnerHour.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				// logging.info(this, "stateChanged " + spinnerHour.getValue() + " " +
				// spinnerHour.getValue().getClass().getName());

				produceTargetTime(Calendar.HOUR_OF_DAY, Integer.valueOf(spinnerHour.getValue().toString()));

			}
		});

		spinnerMinute.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				// logging.info(this, "stateChanged " + spinnerMinute.getValue() + " " +
				// spinnerMinute.getValue().getClass().getName());

				produceTargetTime(Calendar.MINUTE, Integer.valueOf(spinnerMinute.getValue().toString()));

			}
		});

		JPanel panelSpinnerDelay = new JPanel();
		panelSpinnerDelay.setOpaque(false);
		GroupLayout lPanelSpinnerDelay = new GroupLayout(panelSpinnerDelay);
		panelSpinnerDelay.setLayout(lPanelSpinnerDelay);

		lPanelSpinnerDelay.setVerticalGroup(lPanelSpinnerDelay.createParallelGroup().addComponent(spinnerDelay,
				de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT,
				de.uib.configed.Globals.BUTTON_HEIGHT));
		lPanelSpinnerDelay.setHorizontalGroup(lPanelSpinnerDelay.createSequentialGroup().addComponent(spinnerDelay,
				2 * de.uib.configed.Globals.TIME_SPINNER_WIDTH, 2 * de.uib.configed.Globals.TIME_SPINNER_WIDTH,
				2 * de.uib.configed.Globals.TIME_SPINNER_WIDTH));

		// contentPane.setBackground( java.awt.Color.BLUE );

		GroupLayout lPanel = new GroupLayout(contentPane);
		contentPane.setLayout(lPanel);

		lPanel.setVerticalGroup(lPanel.createSequentialGroup().addGap(de.uib.configed.Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(fieldTaskname,
						de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT,
						de.uib.configed.Globals.BUTTON_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonRefreshTime, de.uib.configed.Globals.BUTTON_HEIGHT,
								de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT)
						.addComponent(buttonSetNew, de.uib.configed.Globals.BUTTON_HEIGHT,
								de.uib.configed.Globals.BUTTON_HEIGHT, de.uib.configed.Globals.BUTTON_HEIGHT))
				.addGap(de.uib.configed.Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelDelay, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(panelSpinnerDelay, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT))
				.addGap(de.uib.configed.Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(panelTimeSelection, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(labelStartdelay, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT))
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelStartAt, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(labelStarttime, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT))
				.addGap(de.uib.configed.Globals.VGAP_SIZE)
				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelClientCount, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(fieldClientCount, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(labelDepotCount, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)
						.addComponent(fieldInvolvedDepotsCount, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT))
				.addGap(de.uib.configed.Globals.VGAP_SIZE)

				.addGroup(lPanel.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(waitingProgressBar, de.uib.configed.Globals.PROGRESS_BAR_HEIGHT,
								de.uib.configed.Globals.PROGRESS_BAR_HEIGHT,
								de.uib.configed.Globals.PROGRESS_BAR_HEIGHT)
						.addComponent(labelTimeYetToWait, de.uib.configed.Globals.LINE_HEIGHT,
								de.uib.configed.Globals.LINE_HEIGHT, de.uib.configed.Globals.LINE_HEIGHT)));

		lPanel.setHorizontalGroup(lPanel.createParallelGroup().addGroup(lPanel.createSequentialGroup()
				.addGap(de.uib.configed.Globals.MIN_HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE, Short.MAX_VALUE)
				.addComponent(fieldTaskname, 2 * de.uib.configed.Globals.BUTTON_WIDTH,
						3 * de.uib.configed.Globals.BUTTON_WIDTH, 3 * de.uib.configed.Globals.BUTTON_WIDTH)
				.addGap(de.uib.configed.Globals.MIN_HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup()
						.addGap(de.uib.configed.Globals.MIN_HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								Short.MAX_VALUE)
						.addGap(2 * de.uib.configed.Globals.BUTTON_WIDTH, 3 * de.uib.configed.Globals.BUTTON_WIDTH,
								3 * de.uib.configed.Globals.BUTTON_WIDTH)
						.addComponent(buttonSetNew, de.uib.configed.Globals.BUTTON_WIDTH / 2,
								de.uib.configed.Globals.BUTTON_WIDTH / 2, de.uib.configed.Globals.BUTTON_WIDTH / 2)
						.addComponent(buttonRefreshTime, de.uib.configed.Globals.BUTTON_WIDTH / 2,
								de.uib.configed.Globals.BUTTON_WIDTH / 2, de.uib.configed.Globals.BUTTON_WIDTH / 2)
						.addGap(de.uib.configed.Globals.MIN_HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup()
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(panelSpinnerDelay, de.uib.configed.Globals.BUTTON_WIDTH,
								de.uib.configed.Globals.BUTTON_WIDTH, de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelDelay, 2 * de.uib.configed.Globals.BUTTON_WIDTH,
								2 * de.uib.configed.Globals.BUTTON_WIDTH, 3 * de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup()
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(panelTimeSelection, de.uib.configed.Globals.BUTTON_WIDTH,
								de.uib.configed.Globals.BUTTON_WIDTH, de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelStartdelay, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(lPanel.createSequentialGroup()
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addGap(de.uib.configed.Globals.BUTTON_WIDTH, de.uib.configed.Globals.BUTTON_WIDTH,
								de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelStartAt, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelStarttime, 1 * de.uib.configed.Globals.BUTTON_WIDTH,
								2 * de.uib.configed.Globals.BUTTON_WIDTH, 2 * de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(lPanel.createSequentialGroup().addGap(de.uib.configed.Globals.HGAP_SIZE)
						.addGap(de.uib.configed.Globals.BUTTON_WIDTH).addGap(de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelClientCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE / 2)
						.addComponent(fieldClientCount, clientCountWidth, clientCountWidth, Short.MAX_VALUE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE, de.uib.configed.Globals.HGAP_SIZE,
								de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelDepotCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE / 2)
						.addComponent(fieldInvolvedDepotsCount, clientCountWidth, clientCountWidth, Short.MAX_VALUE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE))

				.addGroup(lPanel.createSequentialGroup().addGap(de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(waitingProgressBar, de.uib.configed.Globals.BUTTON_WIDTH * 2,
								de.uib.configed.Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
						.addGap(de.uib.configed.Globals.HGAP_SIZE)
						.addComponent(labelTimeYetToWait, de.uib.configed.Globals.BUTTON_WIDTH,
								de.uib.configed.Globals.BUTTON_WIDTH, de.uib.configed.Globals.BUTTON_WIDTH)
						.addGap(de.uib.configed.Globals.HGAP_SIZE))

		);

	}

	private void startAction() {
		waitingMode = false;

		if (spinnerDelay.getValue() == nullDelayValue) {
			main.wakeUp(currentlySelectedClients, scheduleTitle);
		} else {
			main.wakeUpWithDelay(labelledDelays.get((String) spinnerDelay.getValue()), currentlySelectedClients,
					scheduleTitle);
		}

		leave();
	}

	private void startWaiting() {
		if (waitingTask != null)
			waitingTask.stop();
		waitingMode = true;
		logging.info(this, "startWaiting " + runningInstances);
		runningInstances.add(this, scheduleTitle);
		// setCalToNowAsStart(); //update start time values
		waitingTask = new WaitingWorker((WaitingSleeper) this);
		// waitingTask.addPropertyChangeListener(this);
		waitingTask.execute();

	}

	// implementing WaitingSleeper
	public void actAfterWaiting() {
		startAction();
	}

	public JProgressBar getProgressBar() {
		return waitingProgressBar;
	}

	public JLabel getLabel() {
		return labelTimeYetToWait;
	}

	public long getStartActionMillis() {
		return startActionMillis;
	}

	public long getWaitingMillis() {
		return waitingMillis;
	}

	public long getOneProgressBarLengthWaitingMillis() {
		return waitingMillis;
	}

	public String setLabellingStrategy(long millisLevel) {
		return " " + configed.getResourceValue("FStartWakeOnLan.timeLeft") + "  "
				+ de.uib.utilities.Globals.giveTimeSpan(getWaitingMillis() - millisLevel);
	}

	@Override
	public void doAction1() {

		logging.info(this, "doAction1");
		// super.doAction1(); //includes leave()

		if (currentlySelectedClients == null || currentlySelectedClients.length == 0) {
			JOptionPane.showMessageDialog(this, configed.getResourceValue("FStartWakeOnLan.noClientsSelected.text"),
					configed.getResourceValue("FStartWakeOnLan.noClientsSelected.title"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		disableSettingOfTimes();

		scheduleTitle = fieldTaskname.getText();
		if (scheduleTitle.startsWith(scheduleTitleStarter))
			scheduleTitle = scheduleTitle.substring(scheduleTitleStarter.length());

		scheduleTitle = "  scheduler" + scheduleTitle;
		startActionMillis = new GregorianCalendar().getTimeInMillis();
		// logging.info(this, "doAction1 calTimeMillis " + calTimeMillis);
		// logging.info(this, "doAction1 startActionMillis " + startActionMillis);
		waitingMillis = cal.getTimeInMillis() - startActionMillis;
		// logging.info(this, "doAction1 waitingMillis " + waitingMillis);
		if (waitingMillis < 0)
			waitingMillis = 0;
		// waitingSeconds = (int) (waitingMillis / 1000);
		// logging.info(this, "doAction1 " + waitingSeconds);

		if (waitingMillis < 100) {
			startAction();
		} else {
			startWaiting();
		}

	}

	@Override
	public void leave() {
		boolean reallyLeave = true;
		logging.info(this, "leave  with runningInstances " + runningInstances);
		logging.info(this, "leave  waitingMode  " + waitingMode);

		if (waitingMode) {
			int returnedOption = JOptionPane.NO_OPTION;
			returnedOption = JOptionPane.showOptionDialog(this, configed.getResourceValue("FStartWakeOnLan.allowClose"),
					configed.getResourceValue("FStartWakeOnLan.allowClose.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION)
				reallyLeave = true;
			else
				reallyLeave = false;

		}

		logging.info(this, "leave  reallyLeave  " + reallyLeave);
		if (reallyLeave) {
			logging.info(this, "waitingTask != null " + (waitingTask != null));
			if (waitingTask != null) {
				waitingTask.stop();
			}

			runningInstances.forget(this);

			super.leave();
		}
	}

	@Override
	public void doAction2() {
		logging.info(this, "doAction2");
		leave();
	}

	/*
	 * PropertyChangeListener
	 * public void propertyChange(PropertyChangeEvent evt)
	 * {
	 * logging.info(this, "propertyChange event " + evt);
	 * if ("progress" == evt.getPropertyName())
	 * {
	 * int progress = (Integer) evt.getNewValue();
	 * 
	 * logging.info(this, "propertyChange  waitingProgressBar max " +
	 * waitingProgressBar.getMaximum() + " value " + waitingProgressBar.getValue());
	 * 
	 * waitingProgressBar.setValue(progress);
	 * }
	 * }
	 * 
	 */

	/*
	 * class WaitingTask extends SwingWorker<Void, Long> {
	 * //
	 * // Main task. Executed in background thread.
	 * //
	 * 
	 * private boolean stopped = false;
	 * private final JLabel statusLabel;
	 * private final JProgressBar progressBar;
	 * 
	 * WaitingTask(JProgressBar progressBar, JLabel statusLabel)
	 * {
	 * this.progressBar = progressBar;
	 * this.statusLabel = statusLabel;
	 * }
	 * 
	 * 
	 * public void stop()
	 * {
	 * logging.info(this, "stop");
	 * stopped = true;
	 * cancel(true);
	 * }
	 * 
	 * 
	 * @Override
	 * public Void doInBackground() {
	 * 
	 * int progress = 0;
	 * setProgress( progress );
	 * 
	 * //int noOfSteps = 100;
	 * //long timeStepMillis = (long) (waitingMillis / noOfSteps );
	 * 
	 * long timeStepMillis = (long) 1000;
	 * 
	 * //long noOfSteps = (long) (waitingMillis/ timeStepMillis);
	 * 
	 * //logging.info(this, " doInBackground waitingMillis " + waitingMillis );
	 * 
	 * 
	 * long elapsedMillis = 0;
	 * long elapsedMins = 0;
	 * 
	 * 
	 * 
	 * 
	 * //while (progress < 100 && !stopped)
	 * while (elapsedMillis < waitingMillis && !stopped)
	 * {
	 * try {
	 * Thread.sleep( timeStepMillis );
	 * }
	 * catch (InterruptedException ignore)
	 * {
	 * logging.info(this, "InterruptedException");
	 * }
	 * 
	 * long nowMillis = new GregorianCalendar().getTimeInMillis();
	 * //elapsedMillis = timeStepMillis * progress;;
	 * 
	 * elapsedMillis = nowMillis - startActionMillis;
	 * elapsedMins = (elapsedMillis / 1000) / 60;
	 * 
	 * //logging.info(this, " doInBackground progress elapsedMillis " +
	 * elapsedMillis);
	 * //logging.info(this, " doInBackground progress " + progress +
	 * " totalTimeElapsed  [min] " + elapsedMins );
	 * 
	 * publish(elapsedMillis);
	 * 
	 * //firePropertyChange("elapsedMins", 0, elapsedMins);
	 * 
	 * 
	 * 
	 * //progress++;
	 * //setProgress( progress );
	 * 
	 * //setElapsedMins(elapsedMins);
	 * }
	 * return null;
	 * }
	 * 
	 * 
	 * @Override
	 * protected void process( java.util.List<Long> listOfMillis )
	 * {
	 * //update the steps which are done
	 * //logging.info(this, "process, we have got list " + listOfMillis);
	 * 
	 * long millis = listOfMillis.get( listOfMillis.size() - 1);
	 * //logging.info(this, "process :: millis " + millis);
	 * statusLabel.setText(
	 * //"passed " + giveTimeSpan( millis) +
	 * " " + configed .getResourceValue("FStartWakeOnLan.timeLeft") + "  " +
	 * giveTimeSpan( waitingMillis - millis ) );
	 * 
	 * int barLength = progressBar.getMaximum() - progressBar.getMinimum();
	 * 
	 * //logging.info(this, "progressBar.getMaximum() " + progressBar.getMaximum() +
	 * ":: progressBar.getMinimum() " + progressBar.getMinimum()
	 * // + ":: millis " + millis + " :: waitingMillis " + waitingMillis +
	 * " :: min + " + ((int) ((barLength * millis) / waitingMillis)));
	 * 
	 * progressBar.setValue( ( int ) (progressBar.getMinimum() + (int) ( (barLength
	 * * millis) / waitingMillis)) ) ;
	 * 
	 * }
	 * 
	 * //
	 * // Executed in event dispatching thread
	 * //
	 * 
	 * @Override
	 * public void done() {
	 * logging.info(this, "done,  stopped is " + stopped );
	 * if (!stopped) startAction();
	 * }
	 * }
	 * 
	 */
}
