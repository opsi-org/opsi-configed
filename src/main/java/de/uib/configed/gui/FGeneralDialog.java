package de.uib.configed.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

/**
 * FGeneralDialog
 * Copyright:     Copyright (c) 2001-2017,2020-2022
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.FEditObject;

public class FGeneralDialog extends JDialog
		implements ActionListener, KeyListener, MouseListener {

	/*
	 * public static class DialogCollector extends HashMap<FGeneralDialog, String>
	 * {
	 * private java.util.List<DialogInstancesFollower> followers;
	 * 
	 * DialogCollector()
	 * {
	 * followers = new ArrayList<DialogInstancesFollower>();
	 * }
	 * 
	 * public void addFollower(DialogInstancesFollower aFollower)
	 * {
	 * followers.add(aFollower);
	 * }
	 * 
	 * public void removeFollower(DialogInstancesFollower aFollower)
	 * {
	 * followers.remove(aFollower);
	 * }
	 * 
	 * 
	 * @Override
	 * public String put(FGeneralDialog key, String v)
	 * {
	 * String result = super.put(key, v);
	 * for (DialogInstancesFollower aFollower : followers)
	 * {
	 * aFollower.instancesChanged();
	 * }
	 * return result;
	 * }
	 * 
	 * 
	 * @Override
	 * public String remove(Object key)
	 * {
	 * String result = super.remove(key);
	 * for (DialogInstancesFollower aFollower : followers)
	 * {
	 * aFollower.instancesChanged();
	 * }
	 * return result;
	 * }
	 * 
	 * }
	 * 
	 * public static interface DialogInstancesFollower
	 * {
	 * public void instancesChanged();
	 * 
	 * public void actOnDialogInstances();
	 * }
	 * 
	 * public static DialogCollector existingInstances;
	 * 
	 * static {existingInstances = new DialogCollector();}
	 */

	boolean shiftPressed = true;

	protected FadingMirror glass;
	protected JPanel allpane = new JPanel();

	protected JScrollPane scrollpane = new JScrollPane();
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	// JTextArea jTextArea1 = new JTextArea();
	protected IconButton jButton1 = new IconButton();
	protected JButton jButton2 = new JButton();
	protected JButton jButton3 = new JButton();
	private int DEFAULT = 1;
	static final int OK = 1;
	static final int NO = 2;

	private static int defaultPreferredWidth = 250;
	private static int defaultPreferredHeight = 300;

	protected int preferredWidth;
	protected int preferredHeight;

	protected int additionalPaneMaxWidth = GroupLayout.PREFERRED_SIZE;

	protected String button1Text = configed.getResourceValue("FGeneralDialog.ok");
	protected String button2Text = configed.getResourceValue("FGeneralDialog.ignore");
	protected String button3Text = configed.getResourceValue("FGeneralDialog.empty");

	protected String[] buttonNames;
	protected Icon[] icons;
	protected String iconsLog;

	protected int noOfButtons = 3;

	Color myHintYellow = new java.awt.Color(255, 255, 230);

	int result = 1;
	int value1 = OK;
	int value2 = NO;

	protected JPanel jPanelButtonGrid = new JPanel();
	protected JPanel additionalPane = new JPanel();
	protected GridLayout gridLayout1 = new GridLayout();
	protected FlowLayout flowLayout1 = new FlowLayout();
	// JLabel jLabel1 = new JLabel();

	protected java.awt.Window owner;

	protected JProgressBar waitingProgressBar; // for use in derived classes

	public FGeneralDialog(JFrame owner, String title, JPanel pane) {
		super(owner, false);
		this.owner = owner;
		logging.info(this, "created by constructor 1, owner " + owner);
		registerWithRunningInstances();
		this.owner = owner;
		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);
		additionalPane = pane;
		checkAdditionalPane();
		centerOn(owner);

	}

	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public void setDefaultResult(int d) {
		DEFAULT = d;
	}

	public void registerWithRunningInstances() {
		// logging.info(this, "running instances " +
		// FEditObject.runningInstances.size());
		logging.info(this, "registerWithRunningInstances " + wantToBeRegisteredWithRunningInstances());
		// if ( !isModal() )
		if (wantToBeRegisteredWithRunningInstances()) {
			FEditObject.runningInstances.add(this, "");
			// logging.info(this, "added to running instances " + this);
		}
		logging.info(this, "running instances " + FEditObject.runningInstances.size());
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal) {
		super(owner, modal);
		this.owner = owner;
		logging.info(this, "created by constructor 2, owner " + owner);
		registerWithRunningInstances();
		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);
		checkAdditionalPane();
		additionalPane.setVisible(false);
		guiInit();
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, int lastButtonNo) {
		this(owner, title, modal,
				new String[] { configed.getResourceValue("FGeneralDialog.ok"),
						configed.getResourceValue("FGeneralDialog.ignore") },
				lastButtonNo, defaultPreferredWidth, defaultPreferredHeight);
	}

	/*
	 * public FInfoDialog(Frame owner, String title, String message, boolean modal,
	 * int lastButtonNo)
	 * {
	 * this (owner, title, modal, lastButtonNo);
	 * setMessage ();
	 * }
	 * 
	 */

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList) {
		this(owner, title, modal, buttonList, defaultPreferredWidth, defaultPreferredHeight);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		this(owner, title, modal, buttonList, buttonList.length, preferredWidth, preferredHeight);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, int lastButtonNo,
			int preferredWidth, int preferredHeight) {
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
		logging.info(this, "created by constructor 3  owner " + owner);

		construct(title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout, addPane);
	}
	/*
	 * registerWithRunningInstances();
	 * 
	 * 
	 * 
	 * setIconImage (Globals.mainIcon);
	 * 
	 * glass = new FadingMirror();
	 * setGlassPane(glass);
	 * 
	 * if (lastButtonNo > -1)
	 * this.noOfButtons = lastButtonNo;
	 * this.buttonNames = buttonList;
	 * 
	 * this.icons = icons;
	 * //if (icons != null)
	 * //logging.info(this, " icons " + Arrays.toString( icons ));
	 * initIcons();
	 * setButtons();
	 * 
	 * 
	 * this.preferredWidth = preferredWidth;
	 * this.preferredHeight = preferredHeight;
	 * 
	 * 
	 * setTitle (title);
	 * setFont(Globals.defaultFont);
	 * 
	 * additionalPane = addPane;
	 * 
	 * if (!lazyLayout)
	 * setupLayout();
	 * //else we have to call setupGUI in a calling method
	 * centerOn(owner);
	 * }
	 */

	private void construct(String title, boolean modal, String[] buttonList, Icon[] icons, int lastButtonNo,
			int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		registerWithRunningInstances();

		setIconImage(Globals.mainIcon);

		// logging.info(this, "construct " + lastButtonNo + " buttons " +
		// Arrays.toString( buttonList ));

		glass = new FadingMirror();
		setGlassPane(glass);

		if (lastButtonNo > -1)
			this.noOfButtons = lastButtonNo;
		else
			this.noOfButtons = buttonList.length;

		this.buttonNames = buttonList;

		this.icons = icons;
		// if (icons != null)
		// logging.info(this, " icons " + Arrays.toString( icons ));
		initIcons();
		setButtons();

		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;

		setTitle(title);
		setFont(Globals.defaultFont);

		additionalPane = addPane;

		if (!lazyLayout)
			setupLayout();
		// else we have to call setupLayout later explicitly
		centerOn(owner);

	}

	public FGeneralDialog(java.awt.Window owner, String title, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner);
		construct(title, false, buttonList, null, -1, preferredWidth, preferredHeight, false, null);
	}

	/*
	 * public FGeneralDialog(int test, java.awt.Window owner, String title, String[]
	 * buttonList, int preferredWidth, int preferredHeight)
	 * {
	 * super(owner);
	 * this.owner = owner;
	 * //super();??
	 * logging.info(this, "created by constructor 4, owner " + owner);
	 * registerWithRunningInstances();
	 * 
	 * setIconImage (Globals.mainIcon);
	 * 
	 * glass = new FadingMirror();
	 * setGlassPane(glass);
	 * 
	 * this.noOfButtons = buttonList.length;
	 * this.buttonNames = buttonList;
	 * initIcons();
	 * setButtons();
	 * 
	 * this.preferredWidth = preferredWidth;
	 * this.preferredHeight = preferredHeight;
	 * 
	 * setTitle (title);
	 * setFont(Globals.defaultFont);
	 * 
	 * guiInit();
	 * 
	 * 
	 * }
	 */

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

	public JPanel checkAdditionalPane() {
		if (additionalPane == null) {
			additionalPane = new JPanel();
			additionalPane.setVisible(false);
			additionalPane.add(new JLabel("~~~~"));
		}
		// else
		// additionalPane.setBackground(Color.RED);

		// additionalPane.setBackground( Color.BLUE );
		return additionalPane;
	}

	protected void initIcons() {
		if (icons != null && icons.length != buttonNames.length) {
			logging.error(this, "icons not correctly specified");
			icons = null;
		}

		if (icons == null) {
			logging.info(this, "init null icons");
			icons = new Icon[buttonNames.length];

			for (int i = 0; i < buttonNames.length; i++) {
				icons[i] = null;
			}
		}

		/*
		 * not working yet
		 * if ( icons[0] != null )
		 * {
		 * jButton1.setDefaultIcon( icons[0] );
		 * jButton1.setRunningActionIcon( "images/waitingcircle.png" );
		 * }
		 */

	}

	protected void setButtons() {
		logging.info(this, "setButtons and icons " + java.util.Arrays.asList(buttonNames));
		button1Text = buttonNames[0];
		jButton1.setText("hallo");
		if (icons[0] != null) {
			jButton1.setIcon(icons[0]);
			((ImageIcon) icons[0]).setDescription(button1Text);
		}

		if (noOfButtons > 1) {
			button2Text = buttonNames[1];
			jButton2.setText(button2Text);
			if (icons[1] != null) {
				jButton2.setIcon(icons[1]);
				((ImageIcon) icons[1]).setDescription(button2Text);
			}
		}

		if (noOfButtons > 2) {
			button3Text = buttonNames[2];
			jButton3.setText(button3Text);
			if (icons[2] != null) {
				jButton3.setIcon(icons[2]);
				((ImageIcon) icons[2]).setDescription(button2Text);
			}
		}

		iconsLog = "";

		for (Icon icon : icons) {
			if (icon == null)
				iconsLog = iconsLog + icon + "  ";
			else
				iconsLog = iconsLog + ((ImageIcon) icon).getDescription() + "    ";
		}

		logging.info(this, "with icons " + iconsLog);
	}

	public void setContentBackground(java.awt.Color c) {
		allpane.setBackground(c);
	}

	protected void allLayout() {
		// allpane = new JPanel();
		// BorderLayout borderLayout1 = new BorderLayout();
		// allpane.setLayout(borderLayout1);
		allpane.setBackground(Globals.backLightBlue);// Globals.nimbusBackground);//Globals.backgroundWhite);//(myHintYellow);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		// allpane.setBorder(BorderFactory.createEtchedBorder());

		// additionalPane.setBackground( Color.YELLOW );

		northPanel = new JPanel();
		northPanel.setOpaque(false);

		southPanel = new JPanel();
		southPanel.setOpaque(false);

		// scrollpane = new JScrollPane();

		scrollpane.setBackground(Color.white);
		scrollpane.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2)
						.addComponent(additionalPane, 50, 100, Short.MAX_VALUE)// GroupLayout.PREFERRED_SIZE)//Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addComponent(additionalPane, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jPanelButtonGrid, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, Globals.lineHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(southPanel, 2 * Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
		allLayout.setHorizontalGroup(allLayout.createParallelGroup()
				.addComponent(northPanel, 100, 300, Short.MAX_VALUE)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(scrollpane, 100, 300, Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2))
				.addComponent(southPanel, 100, 300, Short.MAX_VALUE));

		getContentPane().add(allpane);

	}

	public void setupLayout() {
		guiInit();
	}

	private void guiInit() {
		// scrollpane = new JScrollPane();
		initComponents();
		allLayout();
		jButton1.setFont(Globals.defaultFont);
		jButton1.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));// (new Dimension(91,
																								// 20));
		jButton1.setText(button1Text);
		jButton2.setFont(Globals.defaultFont);
		jButton2.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));// (new Dimension(91,
																								// 20));
		jButton2.setText(button2Text);
		jButton3.setFont(Globals.defaultFont);
		jButton3.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));// (new Dimension(91,
																								// 20));
		jButton3.setText(button3Text);
		jPanelButtonGrid.setLayout(gridLayout1);

		jPanelButtonGrid.setOpaque(false);

		// getContentPane().add(allpane);

		// southPanel.add(jPanelButtonGrid, null);
		jPanelButtonGrid.add(jButton1, null);
		// jPanelButtonGrid.add(jLabel1, null);
		if (noOfButtons > 1) {
			jPanelButtonGrid.add(jButton2, null);
		}
		if (noOfButtons > 2)
			jPanelButtonGrid.add(jButton3, null);

		// jTextArea1.addKeyListener(this);
		jButton1.addKeyListener(this);
		jButton2.addKeyListener(this);
		jButton3.addKeyListener(this);

		jButton1.addActionListener(this);
		jButton2.addActionListener(this);
		jButton3.addActionListener(this);

		allpane.add(southPanel, BorderLayout.SOUTH);

		getContentPane().add(allpane);

		pack();
		centerOn(owner);
	}

	private int intHalf(double x) {
		return (int) (x / 2);
	}

	public void centerOn(Component master) {
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

		logging.info(this, "master, centerOnMaster " + master + ", " + centerOnMaster);

		if (!centerOnMaster) {
			// center on Screen

			/*
			 * Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			 * startX = (screenSize.width - getSize().width)/ 2;
			 * startY = (screenSize.height - getSize().height)/2;
			 */

			if (Globals.mainFrame != null) {
				setLocation(
						(int) Globals.mainFrame.getX() + Globals.locationDistanceX,
						(int) Globals.mainFrame.getY() + Globals.locationDistanceY);
				logging.info(this, " ============================ ");
				logging.info(this, "setLocation based on mainFrame.getX(), .. "
						+ ((int) Globals.mainFrame.getX() + Globals.locationDistanceX)
						+ ", " +
						+((int) Globals.mainFrame.getY() + Globals.locationDistanceY));
			} else {

				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				GraphicsConfiguration gc = gd.getDefaultConfiguration();

				setLocation((gc.getBounds().width - getWidth()) / 2 + gc.getBounds().x,
						(gc.getBounds().height - getHeight()) / 2 + gc.getBounds().y);

				logging.info(this, " ============================ ");
				logging.info(this, " !centerOnMaster, " + gc.getBounds());
			}

		} else {

			logging.info(this, "centerOn  master.getX() " + ((int) master.getX()));
			logging.info(this, "centerOn  master.getY() " + ((int) master.getY()));

			logging.info(this, "centerOn (int) masterOnScreen.getX()  " + (int) masterOnScreen.getX());
			logging.info(this, "centerOn (int) masterOnScreen.getY()  " + (int) masterOnScreen.getY());
			logging.info(this, "centerOn master.getWidth()  " + master.getWidth() / 2);
			logging.info(this, "centerOn master.getHeight()  " + master.getHeight() / 2);
			logging.info(this, "centerOn this.getSize() " + getSize());

			logging.info(this, "centerOn " + master.getClass() + ", " + master);

			startX = (int) masterOnScreen.getX() + intHalf(master.getWidth()) - intHalf(getSize().getWidth());
			startY = (int) masterOnScreen.getY() + intHalf(master.getHeight()) - intHalf(getSize().getHeight());

			// problem: in applet in windows, we may leave the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			logging.info(this, "centerOn screenSize " + screenSize);

			// if (startX + getSize().width > screenSize.width)
			// startX = screenSize.width - getSize().width;

			// if (startY + getSize().height > screenSize.height)
			// startY = screenSize.height - getSize().height;

			setLocation(startX, startY);

			logging.info(this, " ============================ ");
			logging.info(this, " centerOnMaster, startX, startY " + startX + ", " + startY);

			// System.exit(0);

		}

	}

	/*
	 * private void initOther_protected()
	 * {
	 * topPane = new JPanel();
	 * topPane.setLayout(flowLayout1);
	 * //System.out.println("FInfoDialog initTopPane is working");
	 * }
	 */

	public void paint(Graphics g) {
		super.paint(g);
		jButton1.requestFocus();
	}

	public void doAction1() {
		logging.debug(this, "FGeneralDialog.doAction1");
		result = 1;
		leave();
	}

	public void doAction2() {
		logging.debug(this, "FGeneralDialog.doAction2");
		result = 2;
		leave();
	}

	public void doAction3() {
		logging.debug(this, "FGeneralDialog.doAction3");
		result = 3;
		leave();
	}

	public void leave() {
		logging.debug(this, "FGeneralDialog.leave");
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

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			result = DEFAULT;
			leave();
		} else
			super.processWindowEvent(e);
	}

	// KeyListener
	public void keyPressed(KeyEvent e) {
		logging.debug(this, "key event " + e);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = true;
			// System.out.println ("shift pressed");
		} else {
			if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
				if (e.getSource() == jButton1) {
					// System.out.println (".... on Button1. ");
					// preAction1(); integrated in doAction1
					// doAction1();
					// comment out, Mon Sep 16 16:35:39 CEST 2019 @649 /Internet Time/
					// since otherwise doAction1 is called twice on Enter
				} else if (e.getSource() == jButton2) {
					doAction2();
					// System.out.println (".... on Button2 ");
				} else if (e.getSource() == jButton3) {
					doAction3();
				}
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
			// System.out.println ("shift released");
		}

	}

	public void keyTyped(KeyEvent e) {
		// System.out.println ("KeyEvent ... " + e.getKeyChar) );
	}

	// MouseListener
	public void mouseClicked(MouseEvent e) {
		logging.debug(this, "mouseClicked");

	}

	public void mouseEntered(MouseEvent e) {
		logging.debug(this, "mouseEntered");
	}

	public void mouseExited(MouseEvent e) {
		logging.debug(this, "mouseExited");
	}

	public void mousePressed(MouseEvent e) {
		logging.debug(this, "mousePressed");

		preAction1();
	}

	public void mouseReleased(MouseEvent e) {
		logging.debug(this, "mouseReleased");
	}

	protected void preAction1()
	// activated by mouse and key listener events
	{
		logging.info(this, "preAction1");
	}

	protected void postAction1()
	// executed at the end of action listener event
	{
		logging.info(this, "postAction1");
	}

	// ActionListener
	public void actionPerformed(ActionEvent e) {
		// logging.info(this, "ActionEvent ...... ");
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

	public static class FadingMirror extends JPanel
			implements ActionListener {
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

		public void actionPerformed(ActionEvent e) {
			// logging.debug(this, "fade, opacity " + opacity);

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

		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(
					AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

			g.setColor(new Color(230, 230, 250));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

}
