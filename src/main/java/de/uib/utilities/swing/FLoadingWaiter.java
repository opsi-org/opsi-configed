/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.DataLoadingObserver;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;

public class FLoadingWaiter extends JFrame implements DataLoadingObserver, WaitingSleeper {

	private static final long WAITING_MILLIS_FOR_LOADING = 50000;
	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private static final int JPROGRESSBAR_MAX_VALUE = 200;

	private JProgressBar progressBar;
	private JLabel infoLabel;

	private Object observingMesg = Configed.getResourceValue("LoadingObserver.start");

	private WaitingWorker worker;

	private static class MyPainter implements Painter<JProgressBar> {

		private final Color color;

		public MyPainter(Color c1) {
			this.color = c1;
		}

		@Override
		public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
			if (!Main.THEMES) {
				gd.setColor(color);
				gd.fillRect(0, 0, width, height);
			}
		}
	}

	public FLoadingWaiter(Component owner, String title, String startMessage) {
		super(title);

		createGUI(owner);
		observingMesg = startMessage;
	}

	public void stopWaiting() {
		worker.stop();
	}

	// DataLoadingObserver
	@Override
	public void gotNotification(Object mesg) {
		observingMesg = mesg;
	}

	private void createGUI(Component owner) {
		setIconImage(Globals.mainIcon);

		progressBar = new JProgressBar();

		progressBar.setEnabled(true);
		progressBar.setValue(0);
		progressBar.setMaximum(JPROGRESSBAR_MAX_VALUE);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new MyPainter(Globals.opsiLogoLightBlue));
		progressBar.putClientProperty("Nimbus.Overrides", defaults);

		infoLabel = new JLabel();

		JPanel panel = new JPanel();
		if (!Main.THEMES) {
			panel.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		ImageIcon icon = Globals.createImageIcon("images/configed_icon.png", "");
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
				.addComponent(infoLabel, 30, 30, 30)

		);

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
		return observingMesg + " ... ";

	}

}
