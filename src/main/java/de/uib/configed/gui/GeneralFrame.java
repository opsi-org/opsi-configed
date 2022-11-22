package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * FGeneralDialog
 * Copyright:     Copyright (c) 2001-2013
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class GeneralFrame extends JDialog
		implements ActionListener, KeyListener {
	boolean shiftPressed = true;

	protected FadingMirror glass;
	protected JPanel allpane = new JPanel();

	protected JPanel topPane = new JPanel();
	protected JPanel southPanel = new JPanel();
	// JTextArea jTextArea1 = new JTextArea();
	protected JButton jButton1 = new JButton();
	// protected String button1Text = "close";

	private static int defaultPreferredWidth = 250;
	private static int defaultPreferredHeight = 150;

	protected int preferredWidth;
	protected int preferredHeight;

	// protected String button0Text =
	// configed.getResourceValue("FGeneralDialog.close");

	protected int noOfButtons = 1;
	protected int result = -1;
	protected int DEFAULT = 0;

	Color myHintYellow = new java.awt.Color(255, 255, 230);

	protected JPanel jPanelButtonGrid = new JPanel();
	protected GridLayout gridLayout1 = new GridLayout();
	protected BorderLayout borderLayout1 = new BorderLayout();
	protected FlowLayout flowLayout1 = new FlowLayout();
	// JLabel jLabel1 = new JLabel();

	protected JPanel additionalPane;

	public GeneralFrame(Frame owner, String title, boolean modal) {
		super(owner, modal);
		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);
		initComponents();
	}

	public void setup() {
		// guiInit();
		pack();
	}

	// for overwriting
	protected void initComponents() {
		additionalPane = new JPanel();
		additionalPane.setVisible(true);
	}

	public void addPanel(JPanel pane) {
		// additionalPane.add(pane);
		getContentPane().add(pane);
	}

	private int intHalf(double x) {
		return (int) (x / 2);
	}

	public void centerOn(Component master) {
		logging.debug(this, "centerOn " + master);

		int startX = 0;
		int startY = 0;

		Point masterOnScreen = null;

		boolean centerOnMaster = (master != null);

		if (centerOnMaster) {
			try {
				masterOnScreen = master.getLocationOnScreen();
			} catch (Exception ex) {
				logging.info(this, "centerOn " + master + " ex: " + ex);
				centerOnMaster = false;
			}
		}

		if (!centerOnMaster) {
			// center on Screen
			// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// startX = (screenSize.width - getSize().width)/ 2;
			// startY = (screenSize.height - getSize().height)/2;

			GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();

			setLocation((gc.getBounds().width - getWidth()) / 2 + gc.getBounds().x,
					(gc.getBounds().height - getHeight()) / 2 + gc.getBounds().y);

		} else {
			// logging.debug(this, "centerOn (int) masterOnScreen.getX() " + (int)
			// masterOnScreen.getX());
			// logging.debug(this, "centerOn (int) masterOnScreen.getY() " + (int)
			// masterOnScreen.getY());
			// logging.debug(this, "centerOn master.getWidth() " + master.getWidth() / 2);
			// logging.debug(this, "centerOn master.getHeight() " + master.getHeight() / 2)
			// ;
			// logging.debug(this, "centerOn this.getSize() " + getSize());

			// logging.debug(this, "centerOn " + master.getClass() + ", " + master);

			startX = (int) masterOnScreen.getX() + intHalf(master.getWidth()) - intHalf(getSize().getWidth());
			startY = (int) masterOnScreen.getY() + intHalf(master.getHeight()) - intHalf(getSize().getHeight());

			// problem: in applet in windows, we may leave the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// logging.debug(this, "centerOn screenSize " + screenSize);

			if (startX + getSize().width > screenSize.width)
				startX = screenSize.width - getSize().width;

			if (startY + getSize().height > screenSize.height)
				startY = screenSize.height - getSize().height;

			setLocation(startX, startY);

		}

	}

	public void paint(Graphics g) {
		super.paint(g);
		jButton1.requestFocus();
	}

	protected void doAction1() {
		logging.debug(this, "doAction1");
		result = 1;
		leave();
	}

	public void leave() {
		setVisible(false);
		dispose();
	}

	// Events
	// window

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			result = DEFAULT;
			leave();
		}
		super.processWindowEvent(e);
	}

	// KeyListener
	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// ActionListener
	public void actionPerformed(ActionEvent e) {
		// System.out.println ("ActionEvent ...... ");
		if (e.getSource() == jButton1) {
			// System.out.println (".... on Button1. ");
			doAction1();
		}
	}

	public void glassTransparency(boolean vanishing, int initialWaitMs, int delayMs, float step) {
		glass.setVisible(true);
		glass.setOpaque(false);
		glass.setStep(step);
		glass.setDirection(vanishing);
		glass.setDelay(initialWaitMs, delayMs);
		glass.begin();
	}

	static class FadingMirror extends JPanel
			implements ActionListener {
		private float opacity = 1f;
		private float step = 0.3f;
		private Timer fadeTimer;
		private int initialDelay = 100;
		private int delay = 100;
		private boolean vanishing = true;

		public void setDirection(boolean vanishing) {
			this.vanishing = vanishing;

			if (vanishing)
				opacity = 1f;
			else
				opacity = 0f;
		}

		public void setStep(float f) {
			step = f;
		}

		public void setDelay(int initialDelayMs, int delayMs) {
			initialDelay = initialDelayMs;
			delay = delayMs;
		}

		public void begin() {
			fadeTimer = new javax.swing.Timer(initialDelay, this);
			fadeTimer.setDelay(delay);
			fadeTimer.start();
		}

		public void actionPerformed(ActionEvent e) {
			// logging.debug(this, "fade, opacity " + opacity);

			if (vanishing) {
				opacity -= step;
				if (opacity < 0) {
					opacity = 0;
					fadeTimer.stop();
					fadeTimer = null;
				}
			} else {
				opacity += step;

				if (opacity > 1) {
					opacity = 1;
					fadeTimer.stop();
					fadeTimer = null;
				}
			}

			repaint();
		}

		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(
					AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			g.setColor(new Color(230, 230, 250));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

}
