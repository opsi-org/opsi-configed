/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FEditObject;

public class FGeneralDialog extends JDialog {
	private static final int DEFAULT_PREFERRED_WIDTH = 250;
	private static final int DEFAULT_PREFERRED_HEIGHT = 300;

	protected JPanel allpane = new JPanel();

	protected JScrollPane scrollpane = new JScrollPane();
	protected JPanel northPanel;
	protected JPanel centerPanel;
	protected JPanel southPanel;
	protected IconButton jButton1 = new IconButton();
	protected IconButton jButton2 = new IconButton();
	private IconButton jButton3 = new IconButton();
	private int defaultResult = 1;

	protected int preferredWidth;
	protected int preferredHeight;

	private String button1Text;
	private String button2Text;
	private String button3Text;

	private String[] buttonNames;
	private Icon[] buttonIcons;

	private int noOfButtons = 3;

	protected int result = 1;

	protected JPanel jPanelButtonGrid = new JPanel();
	protected JPanel additionalPane = new JPanel();

	public FGeneralDialog(JFrame owner, String title) {
		super(owner, false);

		Logging.info(this.getClass(), "created by constructor 1, owner " + owner);
		registerWithRunningInstances();
		super.setIconImage(Utils.getMainIcon());
		super.setTitle(title);

		checkAdditionalPane();
		super.setLocationRelativeTo(owner);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal) {
		super(owner, modal);

		Logging.info(this.getClass(), "created by constructor 2, owner " + owner);
		registerWithRunningInstances();
		super.setTitle(title);

		super.setIconImage(Utils.getMainIcon());
		checkAdditionalPane();
		additionalPane.setVisible(false);
		guiInit();
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList) {
		this(owner, title, modal, buttonList, DEFAULT_PREFERRED_WIDTH, DEFAULT_PREFERRED_HEIGHT);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		this(owner, title, modal, buttonList, null, buttonList.length, preferredWidth, preferredHeight);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight) {
		this(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, false, null);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, int lastButtonNo,
			int preferredWidth, int preferredHeight, boolean lazyLayout) {
		this(owner, title, modal, buttonList, null, lastButtonNo, preferredWidth, preferredHeight, lazyLayout, null);
	}

	public FGeneralDialog(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		super(owner, modal);
		Logging.info(this.getClass(), "created by constructor 3  owner " + owner);

		initFGeneralDialog(title, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout,
				addPane);
	}

	public FGeneralDialog(Window owner, String title, String[] buttonList, int preferredWidth, int preferredHeight) {
		super(owner);
		initFGeneralDialog(title, buttonList, null, -1, preferredWidth, preferredHeight, false, null);
	}

	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public void setDefaultResult(int d) {
		defaultResult = d;
	}

	private void registerWithRunningInstances() {
		Logging.info(this, "registerWithRunningInstances " + wantToBeRegisteredWithRunningInstances());
		if (wantToBeRegisteredWithRunningInstances()) {
			FEditObject.runningInstances.add(this, "");
		}
		Logging.info(this, "running instances " + FEditObject.runningInstances.size());
	}

	private void initFGeneralDialog(String title, String[] buttonList, Icon[] icons, int lastButtonNo,
			int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		registerWithRunningInstances();

		setIconImage(Utils.getMainIcon());

		if (lastButtonNo > -1) {
			this.noOfButtons = lastButtonNo;
		} else {
			this.noOfButtons = buttonList.length;
		}

		this.buttonNames = buttonList;

		this.buttonIcons = icons;
		initIcons();
		setButtons();

		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;

		setTitle(title);

		additionalPane = addPane;

		if (!lazyLayout) {
			// else we have to call setupLayout later explicitly
			guiInit();
		}
		setLocationRelativeTo(getOwner());
	}

	public int getResult() {
		return result;
	}

	protected void initComponents() {
		checkAdditionalPane();
	}

	public void setCenterPane(JPanel p) {
		if (p == null) {
			centerPanel = new JPanel();
		} else {
			centerPanel = p;
		}
	}

	public void setCenterPaneInScrollpane(JPanel p) {
		if (p == null) {
			centerPanel = new JPanel();
		} else {
			centerPanel = p;
		}

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

	private void initIcons() {
		if (buttonIcons != null && buttonIcons.length != buttonNames.length) {
			Logging.error(this, "icons not correctly specified");
			buttonIcons = null;
		}

		if (buttonIcons == null) {
			Logging.info(this, "init null icons");
			buttonIcons = new Icon[buttonNames.length];

			for (int i = 0; i < buttonNames.length; i++) {
				buttonIcons[i] = null;
			}
		}
	}

	private void setButtons() {
		Logging.info(this, "setButtons and icons " + Arrays.toString(buttonNames));

		jButton1.setText(buttonNames[0]);
		button1Text = buttonNames[0];
		if (buttonIcons[0] != null) {
			jButton1.setIcon(buttonIcons[0]);
		}

		if (noOfButtons > 1) {
			jButton2.setText(buttonNames[1]);
			button2Text = buttonNames[1];
			if (buttonIcons[1] != null) {
				jButton2.setIcon(buttonIcons[1]);
			}
		}

		if (noOfButtons > 2) {
			jButton3.setText(buttonNames[2]);
			button3Text = buttonNames[2];
			if (buttonIcons[2] != null) {
				jButton3.setIcon(buttonIcons[2]);
			}
		}

		Logging.info(this, "with icons " + Arrays.toString(buttonNames));
	}

	public void setTooltipButtons(String tooltip1, String tooltip2, String tooltip3) {
		jButton1.setToolTipText(tooltip1);
		jButton2.setToolTipText(tooltip2);
		jButton3.setToolTipText(tooltip3);
	}

	protected void allLayout() {
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		northPanel = new JPanel();
		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(additionalPane, 50, 100, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(southPanel,
						2 * Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		allLayout.setHorizontalGroup(allLayout.createParallelGroup().addComponent(northPanel, 100, 300, Short.MAX_VALUE)
				.addGroup(allLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(scrollpane, 100, 300, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE))
				.addComponent(southPanel, 100, 300, Short.MAX_VALUE));

		getContentPane().add(allpane);
	}

	public void setupLayout() {
		guiInit();
	}

	private void guiInit() {
		initComponents();
		allLayout();

		jPanelButtonGrid.setLayout(new GridLayout());

		jButton1.setMinimumSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));

		if (button1Text == null) {
			jButton1.setText(Configed.getResourceValue("buttonClose"));
		} else {
			jButton1.setText(button1Text);
		}

		jPanelButtonGrid.add(jButton1, null);

		if (noOfButtons > 1) {
			jButton2.setMinimumSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));

			if (button2Text == null) {
				jButton2.setText(Configed.getResourceValue("FGeneralDialog.ignore"));
			} else {
				jButton2.setText(button2Text);
			}

			jPanelButtonGrid.add(jButton2, null);
		}
		if (noOfButtons > 2) {
			jButton3.setMinimumSize(new Dimension(Globals.BUTTON_WIDTH, Globals.BUTTON_HEIGHT - 2));

			if (button3Text == null) {
				jButton3.setText("");
			} else {
				jButton3.setText(button3Text);
			}

			jPanelButtonGrid.add(jButton3, null);
		}

		jButton1.addActionListener(event -> doAction1());
		jButton2.addActionListener(event -> doAction2());
		jButton3.addActionListener(event -> doAction3());

		allpane.add(southPanel, BorderLayout.SOUTH);

		getContentPane().add(allpane);

		pack();
		setLocationRelativeTo(getOwner());
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
		} else {
			super.processWindowEvent(e);
		}
	}
}
