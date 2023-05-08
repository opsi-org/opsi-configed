package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.thread.WaitCursor;

public class ActivityPanel extends JPanel implements Runnable {

	/** sets width = 30 pixel */
	private static final int WIDTH = 30;
	/** sets height = a0 pixel */
	private static final int HEIGHT = 10;

	private static final int NO_OF_PARTS = 4;

	public static final int SLEEPING_IN_MS = 750;

	/** inactive status is -1 */
	public static final int INACTIVE = -1;
	/** a blueGrey LineBorder */
	private static final LineBorder lineBorderActive = new LineBorder(
			ConfigedMain.THEMES ? Globals.opsiGrey : Globals.blueGrey, 1, true);

	/** a blackLightBlue LineBorder */
	private static final LineBorder lineBorderInactive = new LineBorder(new Color(0, 0, 0, 0), 1, true);

	/** acting status default is false */
	private static boolean acting;

	/** an List for panels */
	private ArrayList<JPanel> partPanels = new ArrayList<>();

	private Color[] colors;

	/**
	 * call the "initGui" method
	 */
	public ActivityPanel() {

		initGui();
	}

	/**
	 * Sets the state of the panals with background and border color
	 * 
	 * @param i number of selected panel of arraylist
	 */
	private void setState(int i) {
		for (int j = 0; j < partPanels.size(); j++) {
			setBorder(lineBorderActive);
			if (!ConfigedMain.THEMES) {
				partPanels.get(j).setBackground(colors[0]);
			} else {
				partPanels.get(j).setBackground(Globals.opsiBlue);
			}
			if (i == INACTIVE) {
				setBorder(lineBorderInactive);
				if (!ConfigedMain.THEMES) {
					partPanels.get(j).setBackground(Globals.BACKGROUND_COLOR_7);
				} else {
					partPanels.get(j).setBackground(new Color(0, 0, 0, 0));
				}
			} else {
				setBorder(lineBorderActive);
				if (!ConfigedMain.THEMES) {
					partPanels.get(j).setBackground(Globals.backNimbus);
				} else {
					partPanels.get(j).setBackground(new Color(0, 0, 0, 0));
				}
			}

			if (j == i) {
				if (!ConfigedMain.THEMES) {
					partPanels.get(j).setBackground(colors[1]);
				} else {
					partPanels.get(j).setBackground(Globals.opsiMagenta);
				}
			}

		}
		try {
			// class cast exceptions mit sleepingMS = 50 if not event dispatch thread
			paintImmediately(0, 0, WIDTH, HEIGHT);
		} catch (Exception strange) {
			Logging.warning(this, "strange exception " + strange);
			setState(INACTIVE);
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
				Globals.threadSleep(this, SLEEPING_IN_MS);

				if (acting) {
					finalizing = true;

					if (i == NO_OF_PARTS - 1) {
						forward = false;
					} else if (i == 0) {
						forward = true;
					}

					setState(i);
					if (forward) {
						i++;
					} else {
						i--;
					}
				} else if (finalizing) {
					finalizing = false;
					forward = true;
					i = INACTIVE;
					setState(i);
					i = 0;
					Globals.threadSleep(this, 2L * SLEEPING_IN_MS);
				}
			}
		} catch (Exception anyException) {
			Logging.warning(this, "on running, caught some exception", anyException);

			if (anyException instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private void initGui() {
		Logging.debug(this, "starting");
		setOpaque(true);
		setBorder(lineBorderInactive);
		colors = new Color[2];
		colors[1] = Globals.opsiLogoBlue;
		colors[0] = Globals.opsiLogoLightBlue;
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		partPanels = new ArrayList<>();

		for (int j = 0; j < NO_OF_PARTS; j++) {
			partPanels.add(new JPanel() {

				@Override
				public void paint(Graphics g) {
					try {
						super.paint(g);
					} catch (java.lang.ClassCastException ex) {
						setActing(false);
						Logging.warning(this, "the ugly well known exception " + ex);
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
		for (int j = 0; j < NO_OF_PARTS; j++) {
			hGroup.addComponent(partPanels.get(j), WIDTH / NO_OF_PARTS, WIDTH / NO_OF_PARTS, WIDTH / NO_OF_PARTS);
		}

		layout.setHorizontalGroup(hGroup);

		GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
		for (int j = 0; j < NO_OF_PARTS; j++) {
			vGroup.addComponent(partPanels.get(j), HEIGHT - 2, HEIGHT - 2, HEIGHT - 2);
		}

		layout.setVerticalGroup(vGroup);
	}
}
