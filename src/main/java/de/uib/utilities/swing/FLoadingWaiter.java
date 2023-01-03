package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
/**
 * FLoadingWaiter
 * Copyright:     Copyright (c) 2016
 * Organisation:  uib
 * @author Rupert Röder
 */
import java.util.GregorianCalendar;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitInfoString;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;

public class FLoadingWaiter extends JFrame implements de.uib.utilities.observer.DataLoadingObserver, WaitingSleeper {

	private static final long WAITING_MILLIS_FOR_LOADING = 50000;
	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;
	JProgressBar progressBar;
	JLabel infoLabel;
	protected String info;
	protected static WaitInfoString waitInfoString;

	protected Object observingMesg = configed.getResourceValue("LoadingObserver.start");

	String[] waitStrings;
	int waitStringsIndex = -1;

	int max = 200;

	private WaitingWorker worker;

	class MyPainter implements Painter<JProgressBar> {

		private final Color color;

		public MyPainter(Color c1) {
			this.color = c1;
		}

		@Override
		public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
			gd.setColor(color);
			gd.fillRect(0, 0, width, height);
		}
	}

	public FLoadingWaiter(Component owner, String title) {
		this(title);

		logging.info(this, "set Loction of FLoadingWaiter in center of screen of owner");
		Rectangle screenRectangle = owner.getGraphicsConfiguration().getBounds();
		setLocation((int) (screenRectangle.getCenterX() - getSize().getWidth() / 2),
				(int) (screenRectangle.getCenterY() - getSize().getHeight() / 2));

		setAlwaysOnTop(true);
	}

	public FLoadingWaiter(String title, String startMessage) {
		this(title);
		observingMesg = startMessage;
	}

	public FLoadingWaiter(String title) {
		super(title);

		createGUI();
		setVisible(true);
		logging.info(this, "should be visible ");
		if (waitInfoString == null)
			waitInfoString = new WaitInfoString();

		worker = new WaitingWorker(this);

	}

	public void stopWaiting() {
		worker.stop();
	}

	// DataLoadingObserver
	@Override
	public void gotNotification(Object mesg) {
		observingMesg = mesg;
	}

	private void createGUI() {
		setIconImage(Globals.mainIcon);

		progressBar = new JProgressBar();

		progressBar.setEnabled(true);
		progressBar.setMaximum(max);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new MyPainter(Globals.opsiLogoLightBlue));
		progressBar.putClientProperty("Nimbus.Overrides", defaults);

		infoLabel = new JLabel();

		JPanel panel = new JPanel();
		panel.setBackground(Globals.BACKGROUND_COLOR_7);

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

		setLocationRelativeTo(null);
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
		logging.info(this, "actAfterWaiting");
		SwingUtilities.invokeLater(

				() -> setVisible(false)

		);

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
		return new GregorianCalendar().getTimeInMillis();
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
		logging.debug(this, "setLabellingStrategy millis " + millisLevel);
		return "" + observingMesg + " " + waitInfoString.next(); // ??produces strings with ascii null

	}

}
