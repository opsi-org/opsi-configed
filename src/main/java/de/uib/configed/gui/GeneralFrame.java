package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class GeneralFrame extends JDialog implements ActionListener {

	private FadingMirror glass;
	private JPanel allpane = new JPanel();

	private JPanel topPane = new JPanel();
	private JPanel southPanel = new JPanel();

	private JButton jButton1 = new JButton();

	private int preferredWidth;
	private int preferredHeight;

	private int noOfButtons = 1;
	private int result = -1;
	private int defaultResult;

	private JPanel jPanelButtonGrid = new JPanel();
	private GridLayout gridLayout1 = new GridLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private FlowLayout flowLayout1 = new FlowLayout();

	private JPanel additionalPane;

	public GeneralFrame(Frame owner, String title, boolean modal) {
		super(owner, modal);
		super.setTitle(title);
		super.setFont(Globals.defaultFont);
		super.setIconImage(Globals.mainIcon);

		initComponents();
	}

	public void setup() {

		pack();
	}

	// for overwriting
	private void initComponents() {
		additionalPane = new JPanel();
		additionalPane.setVisible(true);
	}

	public void addPanel(JPanel pane) {

		getContentPane().add(pane);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		jButton1.requestFocus();
	}

	private void doAction1() {
		Logging.debug(this, "doAction1");
		result = 1;
		leave();
	}

	public void leave() {
		setVisible(false);
		dispose();
	}

	// Events
	// window

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			result = defaultResult;
			leave();
		}
		super.processWindowEvent(e);
	}

	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jButton1) {

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

	private static class FadingMirror extends JPanel implements ActionListener {
		private float opacity = 1F;
		private float step = 0.3F;
		private Timer fadeTimer;
		private int initialDelay = 100;
		private int delay = 100;
		private boolean vanishing = true;

		public void setDirection(boolean vanishing) {
			this.vanishing = vanishing;

			if (vanishing) {
				opacity = 1F;
			} else {
				opacity = 0F;
			}
		}

		public void setStep(float f) {
			step = f;
		}

		public void setDelay(int initialDelayMs, int delayMs) {
			initialDelay = initialDelayMs;
			delay = delayMs;
		}

		public void begin() {
			fadeTimer = new Timer(initialDelay, this);
			fadeTimer.setDelay(delay);
			fadeTimer.start();
		}

		@Override
		public void actionPerformed(ActionEvent e) {

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

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			if (!ConfigedMain.THEMES) {
				g.setColor(Globals.F_GENERAL_DIALOG_FADING_MIRROR_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}

}
