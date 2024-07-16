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

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.observer.RunningInstances;

public class FEditObject extends JDialog implements ActionListener, KeyListener, DocumentListener {
	public static final RunningInstances<JDialog> runningInstances = new RunningInstances<>(JDialog.class,
			"leaving dialog");

	protected static final int BUTTON_WIDTH = 30;

	private Object initialValue = "";
	protected boolean leaveOnCommit = true;

	private JPanel framingPanel;
	protected JPanel editingArea;
	protected JPanel loggingPanel;
	private JSplitPane splitPane;

	protected JButton buttonCommit;
	protected JButton buttonCancel;
	protected JButton buttonClear;
	protected JButton buttonAdd;

	protected boolean editable = true;

	protected FlatTextField extraField;
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
		buttonClear.addActionListener(this);
		buttonAdd.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);
		buttonClear.addKeyListener(this);
		buttonAdd.addKeyListener(this);
	}

	protected void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));
		loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.setBorder(new EmptyBorder(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
				Globals.MIN_GAP_SIZE));
		loggingPanel.setVisible(false);

		loggingArea = new JTextArea();
		loggingArea.setEditable(false);

		JScrollPane scrollpaneL = new JScrollPane(loggingArea);
		scrollpaneL.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		loggingPanel.add(scrollpaneL, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		buttonCommit = new JButton(Utils.getIntellijIcon("checkmark"));
		buttonCommit.setToolTipText(Configed.getResourceValue("save"));
		buttonCommit.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));

		buttonCancel = new JButton(Utils.getIntellijIcon("close"));
		buttonCancel.setToolTipText(Configed.getResourceValue("FEditObject.CancelButtonTooltip"));
		buttonCancel.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonCancel.setEnabled(true);

		buttonClear = new JButton();
		buttonClear.setIcon(Utils.getIntellijIcon("clearCash"));
		buttonClear.setToolTipText(Configed.getResourceValue("FEditObject.RemoveButtonTooltip"));
		buttonClear.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonClear.setVisible(false);

		buttonAdd = new JButton(Utils.getIntellijIcon("add"));
		buttonAdd.setToolTipText(Configed.getResourceValue("FEditObject.AddButtonTooltip"));
		buttonAdd.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonAdd.setEnabled(false);

		extraField = new FlatTextField();
		extraField.setTrailingComponent(buttonAdd);
		extraField.setPreferredSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		extraField.setVisible(false);
	}

	public void setDividerLocation(double loc) {
		splitPane.setDividerLocation(loc);
	}

	protected void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		extraField.getDocument().addDocumentListener(this);

		GroupLayout layout1 = new GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.LEADING)
						.addComponent(editingArea, 60, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(layout1.createSequentialGroup()
								.addComponent(buttonCancel, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonCommit, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
								.addComponent(buttonClear, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(extraField, 20, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
				.addGap(Globals.MIN_GAP_SIZE));

		layout1.setVerticalGroup(layout1.createParallelGroup(Alignment.LEADING).addGroup(layout1.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(editingArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.CENTER)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonClear, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(extraField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)));

		if (loggingPanel.isVisible()) {
			splitPane.setTopComponent(framingPanel);
			splitPane.setBottomComponent(loggingPanel);

			GroupLayout layout = new GroupLayout(getContentPane());
			this.setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

			layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
					GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		} else {
			GroupLayout layout = new GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
							.addComponent(framingPanel, 100, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(Globals.MIN_GAP_SIZE))
					.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
							.addComponent(loggingPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addGap(Globals.MIN_GAP_SIZE)));
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

	public void init() {
		initComponents();
		initEditing();
	}

	protected void initEditing() {
		Logging.debug(this, "FEditObject.initEditing");
		setDataChanged(false);
		extraField.setText("");
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
		} else if (e.getSource() == buttonAdd || e.getSource() == buttonClear) {
			// These buttons will be used only in subclasses
		} else {
			Logging.warning(this, "unexpected action on source ", e.getSource());
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
}
