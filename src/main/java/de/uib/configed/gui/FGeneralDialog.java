package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditObject;

public class FGeneralDialog extends JDialog implements ActionListener, KeyListener, MouseListener {
	boolean shiftPressed = true;

	protected FadingMirror glass;
	protected JPanel allpane = new JPanel();

	protected JScrollPane scrollpane = new JScrollPane();
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	protected IconButton jButton1 = new IconButton();
	protected JButton jButton2 = new JButton();
	protected JButton jButton3 = new JButton();
	private int defaultResult = 1;
	static final int OK = 1;
	static final int NO = 2;

	private static int defaultPreferredWidth = 250;
	private static int defaultPreferredHeight = 300;

	protected int preferredWidth;
	protected int preferredHeight;

	protected int additionalPaneMaxWidth = GroupLayout.PREFERRED_SIZE;

	protected String button1Text = null;
	protected String button2Text = null;
	protected String button3Text = null;

	protected String[] buttonNames;
	protected Icon[] icons;

	protected int noOfButtons = 3;

	int result = 1;
	int value1 = OK;
	int value2 = NO;

	protected JPanel jPanelButtonGrid = new JPanel();
	protected JPanel additionalPane = new JPanel();
	protected GridLayout gridLayout1 = new GridLayout();
	protected FlowLayout flowLayout1 = new FlowLayout();

	protected java.awt.Window owner;

	protected JProgressBar waitingProgressBar; // for use in derived classes

	public FGeneralDialog(JFrame owner, String title) {
		super(owner, false);
		this.owner = owner;

		Logging.info(this, "created by constructor 1, owner " + owner);
		registerWithRunningInstances();
		setIconImage(Globals.mainIcon);
		setTitle(title);
		setFont(Globals.defaultFont);
		checkAdditionalPane();
		setLocationRelativeTo(owner);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal) {
		super(owner, modal);
		this.owner = owner;

		Logging.info(this, "created by constructor 2, owner " + owner);
		registerWithRunningInstances();
		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);
		checkAdditionalPane();
		additionalPane.setVisible(false);
		guiInit();
	}

	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public void setDefaultResult(int d) {
		defaultResult = d;
	}

	public void registerWithRunningInstances() {
		Logging.info(this, "registerWithRunningInstances " + wantToBeRegisteredWithRunningInstances());
		if (wantToBeRegisteredWithRunningInstances()) {
			FEditObject.runningInstances.add(this, "");
		}
		Logging.info(this, "running instances " + FEditObject.runningInstances.size());
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList) {
		this(owner, title, modal, buttonList, defaultPreferredWidth, defaultPreferredHeight);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		this(owner, title, modal, buttonList, null, buttonList.length, preferredWidth, preferredHeight);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight) {
		this(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, false);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout) {
		this(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout, null);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		super(owner, modal);
		this.owner = owner;
		Logging.info(this, "created by constructor 3  owner " + owner);

		initFGeneralDialog(title, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout,
				addPane);
	}

	private void initFGeneralDialog(String title, String[] buttonList, Icon[] icons, int lastButtonNo,
			int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		registerWithRunningInstances();

		setIconImage(Globals.mainIcon);

		glass = new FadingMirror();
		setGlassPane(glass);

		if (lastButtonNo > -1) {
			this.noOfButtons = lastButtonNo;
		} else {
			this.noOfButtons = buttonList.length;
		}

		this.buttonNames = buttonList;

		this.icons = icons;
		initIcons();
		setButtons();

		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;

		setTitle(title);
		setFont(Globals.defaultFont);

		additionalPane = addPane;

		if (!lazyLayout) {
			// else we have to call setupLayout later explicitly
			guiInit();
		}
		setLocationRelativeTo(owner);
	}

	public FGeneralDialog(java.awt.Window owner, String title, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner);
		initFGeneralDialog(title, buttonList, null, -1, preferredWidth, preferredHeight, false, null);
	}

	public int getResult() {
		return result;
	}

	protected void initComponents() {
		checkAdditionalPane();
	}

	public void setCenterPane(JPanel p) {
		if (p == null)
			centerPanel = new JPanel();
		else
			centerPanel = p;
	}

	public void setCenterPaneInScrollpane(JPanel p) {
		if (p == null)
			centerPanel = new JPanel();
		else
			centerPanel = p;

		scrollpane.getViewport().add(centerPanel);
	}

	public void setAdditionalPane(JPanel p) {
		additionalPane = p;

	}

	public void checkAdditionalPane() {
		if (additionalPane == null) {
			additionalPane = new JPanel();
			additionalPane.setVisible(false);
			additionalPane.add(new JLabel("~~~~"));
		}
	}

	protected void initIcons() {
		if (icons != null && icons.length != buttonNames.length) {
			Logging.error(this, "icons not correctly specified");
			icons = null;
		}

		if (icons == null) {
			Logging.info(this, "init null icons");
			icons = new Icon[buttonNames.length];

			for (int i = 0; i < buttonNames.length; i++) {
				icons[i] = null;
			}
		}
	}

	protected void setButtons() {
		Logging.info(this, "setButtons and icons " + java.util.Arrays.asList(buttonNames));

		jButton1.setText(buttonNames[0]);
		button1Text = buttonNames[0];
		if (icons[0] != null) {
			jButton1.setIcon(icons[0]);
			((ImageIcon) icons[0]).setDescription(buttonNames[0]);
		}

		if (noOfButtons > 1) {
			jButton2.setText(buttonNames[1]);
			button2Text = buttonNames[1];
			if (icons[1] != null) {
				jButton2.setIcon(icons[1]);
				((ImageIcon) icons[1]).setDescription(buttonNames[1]);
			}
		}

		if (noOfButtons > 2) {
			jButton3.setText(buttonNames[2]);
			button3Text = buttonNames[2];
			if (icons[2] != null) {
				jButton3.setIcon(icons[2]);
				((ImageIcon) icons[2]).setDescription(buttonNames[2]);
			}
		}

		StringBuilder iconsLog = new StringBuilder();

		for (Icon icon : icons) {
			if (icon == null)
				iconsLog.append(icon + "  ");
			else
				iconsLog.append(((ImageIcon) icon).getDescription() + "    ");
		}

		Logging.info(this, "with icons " + iconsLog);
	}

	public void setContentBackground(java.awt.Color c) {
		allpane.setBackground(c);
	}

	protected void allLayout() {
		allpane.setBackground(Globals.BACKGROUND_COLOR_7);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		northPanel = new JPanel();
		northPanel.setOpaque(false);

		southPanel = new JPanel();
		southPanel.setOpaque(false);

		scrollpane.setBackground(Globals.F_GENERAL_DIALOG_BACKGROUND_COLOR);
		scrollpane.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 50, 100, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(southPanel,
						2 * Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		allLayout.setHorizontalGroup(allLayout.createParallelGroup().addComponent(northPanel, 100, 300, Short.MAX_VALUE)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(scrollpane, 100, 300, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2))
				.addComponent(southPanel, 100, 300, Short.MAX_VALUE));

		getContentPane().add(allpane);

	}

	public void setupLayout() {
		guiInit();
	}

	private void guiInit() {
		initComponents();
		allLayout();

		jPanelButtonGrid.setLayout(gridLayout1);
		jPanelButtonGrid.setOpaque(false);

		jButton1.setFont(Globals.defaultFont);
		jButton1.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));
		jButton1.setText(button1Text == null ? Configed.getResourceValue("FGeneralDialog.ok") : button1Text);
		jPanelButtonGrid.add(jButton1, null);

		if (noOfButtons > 1) {
			jButton2.setFont(Globals.defaultFont);
			jButton2.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));
			jButton2.setText(button2Text == null ? Configed.getResourceValue("FGeneralDialog.ignore") : button2Text);
			jPanelButtonGrid.add(jButton2, null);

		}
		if (noOfButtons > 2) {
			jButton3.setFont(Globals.defaultFont);
			jButton3.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));
			jButton3.setText(button3Text == null ? Configed.getResourceValue("FGeneralDialog.empty") : button3Text);
			jPanelButtonGrid.add(jButton3, null);
		}

		jButton1.addKeyListener(this);
		jButton2.addKeyListener(this);
		jButton3.addKeyListener(this);

		jButton1.addActionListener(this);
		jButton2.addActionListener(this);
		jButton3.addActionListener(this);

		allpane.add(southPanel, BorderLayout.SOUTH);

		getContentPane().add(allpane);

		pack();
		setLocationRelativeTo(owner);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		jButton1.requestFocus();
	}

	public void doAction1() {
		Logging.debug(this, "FGeneralDialog.doAction1");
		result = 1;
		leave();
	}

	public void doAction2() {
		Logging.debug(this, "FGeneralDialog.doAction2");
		result = 2;
		leave();
	}

	public void doAction3() {
		Logging.debug(this, "FGeneralDialog.doAction3");
		result = 3;
		leave();
	}

	public void leave() {
		Logging.debug(this, "FGeneralDialog.leave");
		setVisible(false);

		dispose();
		FEditObject.runningInstances.forget(this);
	}

	public void setButtonsEnabled(boolean b) {
		jButton1.setEnabled(b);
		jButton2.setEnabled(b);
		jButton3.setEnabled(b);
	}

	// Events
	// window

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			result = defaultResult;
			leave();
		} else
			super.processWindowEvent(e);
	}

	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, "key event " + e);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = true;
		} else {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
				if (e.getSource() == jButton1) {

					// comment out, Mon Sep 16 16:35:39 CEST 2019 @649 /Internet Time/
					// since otherwise doAction1 is called twice on Enter
				} else if (e.getSource() == jButton2) {
					doAction2();
				} else if (e.getSource() == jButton3) {
					doAction3();
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		Logging.debug(this, "mouseClicked");

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Logging.debug(this, "mouseEntered");
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Logging.debug(this, "mouseExited");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Logging.debug(this, "mousePressed");

		preAction1();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Logging.debug(this, "mouseReleased");
	}

	// activated by mouse and key listener events
	protected void preAction1() {
		Logging.info(this, "preAction1");
	}

	// executed at the end of action listener event
	protected void postAction1() {
		Logging.info(this, "postAction1");
	}

	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jButton1) {
			preAction1();
			doAction1();
			postAction1();
		}

		else if (e.getSource() == jButton2) {
			doAction2();
		} else if (e.getSource() == jButton3) {
			doAction3();
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

	public static class FadingMirror extends JPanel implements ActionListener {
		private float opacity = 1f;
		private float step = 0.3f;
		private javax.swing.Timer fadeTimer;
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

		@Override
		public void actionPerformed(ActionEvent e) {
			if (vanishing) {
				opacity -= step;
				if (opacity < 0) {
					opacity = 0;
					if (fadeTimer != null) {
						fadeTimer.stop();
						fadeTimer = null;
					}
				}
			} else {
				opacity += step;

				if (opacity > 1) {
					opacity = 1;

					if (fadeTimer != null) {
						fadeTimer.stop();
						fadeTimer = null;
					}
				}
			}

			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			g.setColor(Globals.F_GENERAL_DIALOG_FADING_MIRROR_COLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
}
