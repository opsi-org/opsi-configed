/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * FEditObject.java
 *
 * Created 28.6.2010
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.RunningInstances;
import utils.Utils;

public class FEditObject extends JDialog implements ActionListener, KeyListener, DocumentListener {
	public static final RunningInstances<JDialog> runningInstances = new RunningInstances<>(JDialog.class,
			"leaving dialog");

	protected static final int BUTTON_WIDTH = 30;

	private Dimension areaDimension = new Dimension(300, 240);

	private Object initialValue = "";
	protected boolean leaveOnCommit = true;

	private JPanel framingPanel;
	protected JPanel editingArea;
	protected JPanel loggingPanel;
	private JSplitPane splitPane;

	protected IconButton buttonCommit;
	protected IconButton buttonCancel;
	protected IconButton buttonAdd;
	protected IconButton buttonRemove;

	protected boolean editable = true;

	protected JTextField extraField;
	private JLabel extraLabel;
	protected JTextArea loggingArea;

	public FEditObject(Object initialValue) {
		super.setIconImage(Utils.getMainIcon());

		if (initialValue != null) {
			this.initialValue = initialValue;
		}

		createComponents();
		// components initialized lazily in init()

		addListeners();
	}

	private void addListeners() {
		editingArea.addKeyListener(this);

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);

		buttonRemove.addActionListener(this);
		buttonRemove.addKeyListener(this);

		buttonAdd.addActionListener(this);
		buttonAdd.addKeyListener(this);
	}

	protected void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout());
		loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.setVisible(false);

		loggingArea = new JTextArea("");
		loggingArea.setEditable(false);

		JScrollPane scrollpaneL = new JScrollPane();
		scrollpaneL.setViewportView(loggingArea);
		scrollpaneL.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		loggingPanel.add(scrollpaneL, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		buttonCommit = new IconButton(Configed.getResourceValue("save"), "images/apply.png", "images/apply_over.png",
				"images/apply_disabled.png", true);
		buttonCommit.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));

		buttonCancel = new IconButton(Configed.getResourceValue("FEditObject.CancelButtonTooltip"), "images/cancel.png",
				"images/cancel_over.png", "images/cancel_disabled.png", true);
		buttonCancel.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonCancel.setEnabled(true);

		buttonRemove = new IconButton(Configed.getResourceValue("FEditObject.RemoveButtonTooltip"),
				"images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png", true);
		buttonRemove.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonRemove.setVisible(false);

		buttonAdd = new IconButton(Configed.getResourceValue("FEditObject.AddButtonTooltip"), "images/list-add.png",
				"images/list-add.png", "images/list-add_disabled.png", true);
		buttonAdd.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonAdd.setVisible(false);

		extraField = new JTextField("");
		extraField.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		extraField.setVisible(false);

		extraLabel = new JLabel("");
		extraLabel.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		extraLabel.setVisible(false);
	}

	public void setDividerLocation(double loc) {
		splitPane.setDividerLocation(loc);
	}

	protected void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		extraField.getDocument().addDocumentListener(this);

		GroupLayout layout1 = new GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.LEADING)
						.addComponent(editingArea, 60, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(layout1.createSequentialGroup()
								.addComponent(buttonCancel, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCommit, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, 2 * Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
								.addComponent(buttonRemove, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonAdd, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(extraField, 20, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(extraLabel, 20, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));

		layout1.setVerticalGroup(layout1.createParallelGroup(Alignment.LEADING).addGroup(layout1.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(editingArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.BASELINE)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonRemove, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonAdd, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(extraField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(extraLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)));

		if (loggingPanel.isVisible()) {
			splitPane.setTopComponent(framingPanel);
			splitPane.setBottomComponent(loggingPanel);

			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setHorizontalGroup(layout
					.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING)
							.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
					.addContainerGap());

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		} else {
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addContainerGap()
							.addComponent(framingPanel, 100, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addContainerGap())
					.addGroup(layout.createSequentialGroup().addContainerGap()
							.addComponent(loggingPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addContainerGap()));
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addContainerGap(20, 20)
							.addComponent(framingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									Short.MAX_VALUE)
							.addComponent(loggingPanel, GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE)
							.addContainerGap(20, 20)));
		}

		pack();
	}

	public void setEditable(boolean b) {
		editable = b;
	}

	@Override
	public void setModal(boolean b) {
		super.setModal(b);
		Logging.debug(this, "setModal " + b);
		if (b) {
			setAlwaysOnTop(true);
		}
	}

	public void setDataChanged(boolean b) {
		Logging.debug(this, "setDataChanged " + b);

		if (forbidEditingTargetSpecific() && b) {
			return;
		}

		buttonCommit.setEnabled(b);
	}

	public void setStartValue(Object s) {
		initialValue = s;
	}

	public Object getValue() {
		return initialValue;
	}

	public void setLeaveOnCommit(boolean b) {
		leaveOnCommit = b;
	}

	public boolean init(Dimension usableAreaSize) {
		if (editingArea.getComponentCount() != 1) {
			Logging.error(" editing area not filled with component");
			return false;
		}

		editingArea.getComponent(0).setPreferredSize(usableAreaSize);
		initComponents();
		initEditing();
		return true;
	}

	protected void initEditing() {
		Logging.debug(this, "FEditObject.initEditing");
		setDataChanged(false);
		buttonAdd.setEnabled(false);
		buttonRemove.setEnabled(false);
		extraField.setText("");
	}

	public boolean init() {
		return init(areaDimension);
	}

	public void setExtraLabel(String s) {
		extraLabel.setVisible(true);
		extraLabel.setText(s);
	}

	public void enter() {
		Logging.debug(this, "enter");
	}

	public boolean forbidEditingTargetSpecific() {
		boolean forbidEditing = false;

		Logging.debug("forbidEditing for target " + ConfigedMain.getEditingTarget() + "?");

		if (ConfigedMain.getEditingTarget() == ConfigedMain.EditingTarget.SERVER) {
			forbidEditing = !PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.hasServerFullPermissionPD();
		} else {
			forbidEditing = PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
					.isGlobalReadOnly();
		}

		Logging.debug("forbidEditing " + forbidEditing);

		return forbidEditing;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			runningInstances.add(this, "");
		} else {
			runningInstances.forget(this);
		}
		super.setVisible(b);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		} else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
			enter();
		} else {
			// do nothing on other events
		}

		super.processWindowEvent(e);
	}

	protected void commit() {
		Logging.debug(this, "FEditObject.commit");

		if (forbidEditingTargetSpecific()) {
			cancel();
		} else {
			setStartValue(getValue());
			if (leaveOnCommit) {
				setVisible(false);
			}
		}
	}

	protected void cancel() {
		setStartValue(initialValue);
		setVisible(false);
	}

	// -------------
	// interface ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		Logging.debug(this, "actionPerformed");

		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {
			cancel();
		} else if (e.getSource() == buttonRemove || e.getSource() == buttonAdd) {
			// These buttons will be used only in subclasses
		} else {
			Logging.warning(this, "unexpected action on source " + e.getSource());
		}
	}
	// -------------

	// -------------
	// interface KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else {
			// Do nothing on other keys
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	// -------------

	protected void extraFieldChanged(boolean b) {
		buttonAdd.setEnabled(b);
	}

	// -------------
	// interface DocumentListener
	@Override
	public void changedUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		extraFieldChanged(true);
	}
	// -------------

}
