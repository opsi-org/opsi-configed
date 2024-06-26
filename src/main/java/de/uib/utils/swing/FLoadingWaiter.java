/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.thread.WaitingSleeper;
import de.uib.utils.thread.WaitingWorker;

public class FLoadingWaiter extends JFrame implements WaitingSleeper {
	private static final long WAITING_MILLIS_FOR_LOADING = 50000;
	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private static final int JPROGRESSBAR_MAX_VALUE = 200;

	private JProgressBar progressBar;
	private JLabel infoLabel;

	private String observingMesg = Configed.getResourceValue("LoadingObserver.start");

	private WaitingWorker worker;

	public FLoadingWaiter(Component owner, String title, String startMessage) {
		super(title);

		createGUI(owner);
		observingMesg = startMessage;
	}

	private void createGUI(Component owner) {
		setIconImage(Utils.getMainIcon());

		progressBar = new JProgressBar();

		progressBar.setEnabled(true);
		progressBar.setValue(0);
		progressBar.setMaximum(JPROGRESSBAR_MAX_VALUE);

		infoLabel = new JLabel();

		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		ImageIcon icon = Utils.createImageIcon(Globals.ICON_CONFIGED, "");
		JLabel iconLabel = new JLabel(icon);

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(iconLabel, 150, 150, 150)
						.addGroup(layout.createSequentialGroup().addGap(10, 10, Short.MAX_VALUE)
								.addComponent(infoLabel, 100, 300, Short.MAX_VALUE).addGap(10, 10, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup().addGap(10, 10, 30)
								.addComponent(progressBar, 100, 350, Short.MAX_VALUE).addGap(10, 10, 30)));
		layout.setVerticalGroup(layout
				.createSequentialGroup().addComponent(iconLabel, 150, 150, 150).addComponent(progressBar,
						Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT, Globals.PROGRESS_BAR_HEIGHT)
				.addComponent(infoLabel, 30, 30, 30));

		this.getContentPane().add(panel);

		setSize(new Dimension(400, 250));

		worker = new WaitingWorker(this);
		Logging.info(this, "set Loction of FLoadingWaiter in center of screen of owner");

		setLocationRelativeTo(owner);
		setAlwaysOnTop(true);
		setVisible(true);

		Logging.info(this, "should be visible now");
	}

	public void startWaiting() {
		worker.execute();
	}

	public void setReady() {
		worker.setReady();
	}

	// WaitingSleeper
	@Override
	public void actAfterWaiting() {
		Logging.info(this, "actAfterWaiting");
		SwingUtilities.invokeLater(() -> setVisible(false));
	}

	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public JLabel getLabel() {
		return infoLabel;
	}

	@Override
	public long getStartActionMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public long getWaitingMillis() {
		return WAITING_MILLIS_FOR_LOADING;
	}

	@Override
	public long getOneProgressBarLengthWaitingMillis() {
		return ESTIMATED_TOTAL_WAIT_MILLIS;
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		Logging.debug(this, "setLabellingStrategy millis " + millisLevel);

		// produces strings with ascii null
		return observingMesg;
	}
}
