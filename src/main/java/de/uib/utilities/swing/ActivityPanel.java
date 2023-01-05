package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitCursor;

public class ActivityPanel extends JPanel implements Runnable {
	Color[] colors;

	Thread colorSwitching;

	/** sets width = 30 pixel */
	public static int w = 30;
	/** sets height = a0 pixel */
	public static int h = 10;

	private final int noOfParts = 4;
	/** an List for panels */
	ArrayList<JPanel> partPanels = new ArrayList<>();

	public static int sleepingMS = 750;

	/** inactive status is -1 */
	public static int inactive = -1;
	/** a blueGrey LineBorder */
	public static javax.swing.border.LineBorder lineBorderActive;
	/** a blackLightBlue LineBorder */
	public static javax.swing.border.LineBorder lineBorderInactive;

	/**
	 * call the "initGui" method
	 */
	public ActivityPanel() {
		initGui();
	}

	/** acting status default is false */
	private static boolean acting = false;

	/**
	 * Sets the state of the panals with background and border color
	 * 
	 * @param i number of selected panel of arraylist
	 */
	private void setState(int i)

	{
		for (int j = 0; j < partPanels.size(); j++) {
			setBorder(lineBorderActive);
			partPanels.get(j).setBackground(colors[0]);
			if (i == inactive) {
				setBorder(lineBorderInactive);
				partPanels.get(j).setBackground(Globals.BACKGROUND_COLOR_7);
			} else {
				setBorder(lineBorderActive);
				partPanels.get(j).setBackground(Globals.backNimbus);
			}

			if (j == i) {
				partPanels.get(j).setBackground(colors[1]);
			}

		}
		try {

			{

				paintImmediately(0, 0, w, h); // class cast exceptions mit sleepingMS = 50 if not event dispatch thread
			}

		} catch (Exception strange) {
			logging.warning(this, "strange exception " + strange);
			setState(inactive);

		}
	}

	/**
	 * Sets global variable "acting" with value of b
	 * 
	 * @param b acting status (true or false)
	 */
	public static void setActing(boolean b) {
		acting = b;
	}

	// runnable
	/**
	 * endless loop
	 */
	@Override
	public void run() {
		int i = 0;
		boolean finalizing = false;
		boolean forward = true;
		try {
			while (true) {
				Globals.threadSleep(this, sleepingMS);

				if (acting) {
					finalizing = true;

					if (i == noOfParts - 1) {
						forward = false;
					} else if (i == 0) {
						forward = true;
					}

					setState(i);
					if (forward)
						i++;
					else
						i--;
				} else if (finalizing) {
					finalizing = false;
					forward = true;
					i = inactive;
					setState(i);
					i = 0;
					Globals.threadSleep(this, 2 * sleepingMS);
				}
			}
		} catch (Exception anyException) {
			logging.warning(this, "on running, caught some exception", anyException);

			if (anyException instanceof InterruptedException)
				Thread.currentThread().interrupt();
		}

	}

	protected void initGui() {
		lineBorderInactive = new javax.swing.border.LineBorder(Globals.BACKGROUND_COLOR_7, 1, true);
		lineBorderActive = new javax.swing.border.LineBorder(Globals.blueGrey, 1, true);
		logging.debug(this, "starting");
		setOpaque(true);
		setBorder(lineBorderInactive);
		colors = new Color[2];
		colors[1] = Globals.opsiLogoBlue;
		colors[0] = Globals.opsiLogoLightBlue;
		setPreferredSize(new Dimension(w, h));

		partPanels = new ArrayList<>();

		for (int j = 0; j < noOfParts; j++) {
			partPanels.add(new JPanel() {

				@Override
				public void paint(Graphics g) {
					try {
						super.paint(g);
					} catch (java.lang.ClassCastException ex) {
						setActing(false);
						logging.warning(this, "the ugly well known exception " + ex);
						try {
							Thread.sleep(10000);
							WaitCursor.stopAll();
						} catch (InterruptedException x) {
							Thread.currentThread().interrupt();
						}

					}
				}

			});

			partPanels.get(j).setOpaque(true);
		}

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		for (int j = 0; j < noOfParts; j++)
			hGroup.addComponent(partPanels.get(j), w / noOfParts, w / noOfParts, w / noOfParts);
		layout.setHorizontalGroup(hGroup);

		GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
		for (int j = 0; j < noOfParts; j++)
			vGroup.addComponent(partPanels.get(j), h - 2, h - 2, h - 2);
		layout.setVerticalGroup(vGroup);
	}

}
