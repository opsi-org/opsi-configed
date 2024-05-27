/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * FEdit.java
 *
 * (c) uib  2009-2013,2021
 */

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class FEdit extends JDialog implements ActionListener, KeyListener {
	protected Dimension areaDimension = new Dimension(100, 40);

	protected String initialText = "";
	private String hint;

	private JPanel framingPanel;
	protected JComponent editingArea;
	private JLabel labelHint;

	protected JButton buttonCommit;
	private JButton buttonCancel;

	private boolean cancelled;
	private boolean starting = true;

	private AbstractCellEditor servedCellEditor;

	private JTextComponent caller;

	public FEdit(String initialText) {
		this(initialText, null);
	}

	public FEdit(String initialText, String hint) {
		super((Dialog) null);

		Logging.debug(this.getClass(), " FEdit constructed for >>" + initialText + "<< title " + hint);
		super.setIconImage(Utils.getMainIcon());

		if (initialText != null) {
			this.initialText = initialText;
		}

		this.hint = hint;

		createComponents();
		// components initialized lazily in init()
	}

	public void setHint(String hint) {
		labelHint.setVisible(hint != null);
		labelHint.setText(hint);
	}

	private void createComponents() {
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout());

		labelHint = new JLabel();

		buttonCommit = new JButton(Utils.getIntellijIcon("checkmark"));
		buttonCommit.setToolTipText(Configed.getResourceValue("save"));
		buttonCommit.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));

		buttonCancel = new JButton(Utils.getIntellijIcon("close"));
		buttonCancel.setToolTipText(Configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip"));
		buttonCancel.setPreferredSize(new Dimension(40, Globals.BUTTON_HEIGHT));
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

		setHint(hint);

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);

		GroupLayout layout1 = new GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(layout1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addGroup(layout1
				.createParallelGroup(Alignment.LEADING)
				.addComponent(labelHint, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(editingArea, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layout1.createSequentialGroup()
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.MIN_GAP_SIZE));

		layout1.setVerticalGroup(layout1.createParallelGroup(Alignment.LEADING).addGroup(layout1.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(labelHint, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(editingArea, 20, 80, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout1.createParallelGroup(Alignment.BASELINE)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout
						.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(framingPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE)));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout
						.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(framingPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addContainerGap(20, 20)));

		pack();
	}

	public void setDataChanged(boolean b) {
		buttonCommit.setEnabled(b);
		buttonCancel.setEnabled(true);
	}

	public void setCaller(JTextComponent c) {
		this.caller = c;
	}

	public void updateCaller(String s) {
		if (caller != null) {
			caller.setText(s);
		}
	}

	public void setStartText(String s) {
		Logging.debug(this, "FEdit.setStartText(): " + s);

		initialText = s;
		setDataChanged(false);
		cancelled = false;
	}

	public String getText() {
		return initialText;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setVisible(boolean b) {
		if (starting && b) {
			starting = false;
			setDataChanged(false);
		}

		super.setVisible(b);
	}

	public boolean init(Dimension usableAreaSize) {
		if (editingArea.getComponentCount() != 1) {
			Logging.info(this, " editing area not filled with component");
		}

		editingArea.getComponent(0).setPreferredSize(usableAreaSize);
		initComponents();
		return true;
	}

	public boolean init() {
		return init(areaDimension);
	}

	private void leave() {
		Logging.debug(this, "leave");
		updateCaller(initialText);
		buttonCommit.setEnabled(false);
		setVisible(false);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}

		super.processWindowEvent(e);
	}

	protected void commit() {
		Logging.info(this, "commit");
		setStartText(getText());

		if (servedCellEditor != null) {
			servedCellEditor.stopCellEditing();
		}

		leave();
	}

	protected void cancel() {
		Logging.info(this, "cancel, go back to " + initialText);
		setStartText(initialText);
		cancelled = true;

		if (servedCellEditor != null) {
			servedCellEditor.stopCellEditing();
		}

		leave();
	}

	public void setServedCellEditor(AbstractCellEditor cellEditor) {
		servedCellEditor = cellEditor;
	}

	// interface
	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {
			cancel();
		} else {
			Logging.warning(this, "unexpected event on source " + e.getSource());
		}
	}

	// interface
	// KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, " key event " + e);
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else {
			// Do nothing with other keys / events
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}
}
