/*
 * FEditList.java
 * 
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2008-2015, 2017 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 *
 * @author roeder
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.list.StandardListCellRenderer;

public class FEditList extends FEditObject implements ListSelectionListener, MouseListener {
	private javax.swing.JScrollPane scrollpane;

	// The generic is Object here, because it could come from all kinds of objects,
	// not only Strings
	// TODO: Maybe make the class generic?
	protected JList<Object> visibleList;

	private JTextComponent tracker;

	private ListModel<Object> initialModel;
	private List<Object> initiallySelected;

	protected Object selValue = "";

	protected JPopupMenu popup;

	private CellEditor celleditor;

	boolean singleSelectionMode;
	boolean nullable;

	public FEditList() {
		this(null);
	}

	public FEditList(JTextComponent tracker) {
		this(tracker, null);
	}

	public FEditList(JTextComponent tracker, CellEditor celleditor) {
		super("");
		this.tracker = tracker;
		this.celleditor = celleditor;

		visibleList = new JList<>();
		visibleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleList.addListSelectionListener(this);
		visibleList.setCellRenderer(new StandardListCellRenderer());

		visibleList.addMouseListener(this);

		scrollpane = new javax.swing.JScrollPane();
		scrollpane.setViewportView(visibleList);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		editable = false;
		singleSelectionMode = true;

		popup = new JPopupMenu();
		visibleList.setComponentPopupMenu(popup);
	}

	@Override
	protected void createComponents() {
		super.createComponents();

		// we define buttonRemove in a different way since it is used only to clear
		// selection

		buttonRemove = new de.uib.configed.gui.IconButton(Configed.getResourceValue("FEditObject.RemoveButtonTooltip"),
				"images/list-clear.png", "images/list-clear.png", "images/list-clear_disabled.png", true);
		buttonRemove.setPreferredSize(new Dimension(buttonWidth, Globals.BUTTON_HEIGHT));
		buttonRemove.setVisible(false);
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		visibleList.addKeyListener(this);

	}

	public void setCellRenderer(ListCellRenderer render) {
		visibleList.setCellRenderer(render);
	}

	public void setListModel(ListModel<Object> model) {

		visibleList.setModel(model);
		initialModel = model;
	}

	public void setSelectionMode(int selectionMode) {

		visibleList.setSelectionMode(selectionMode);
		singleSelectionMode = (selectionMode == ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * determines if a null selection is allowed (extension of selectionModel
	 * concept)
	 * 
	 * @parameter boolean
	 */
	public void setNullable(boolean b) {
		nullable = b;
	}

	@Override
	public void setEditable(boolean b) {

		super.setEditable(b);
		extraField.setVisible(b);
		buttonRemove.setVisible(b && !singleSelectionMode);

		buttonAdd.setVisible(b);
	}

	protected Object getValueAt(Point location) {
		return visibleList.getModel().getElementAt(visibleList.locationToIndex(location));
	}

	protected void setExtraFieldToListValueAt(Point location) {
		String txt = "" + getValueAt(location);

		extraField.setText(txt);
		extraFieldChanged(false);
	}

	protected void addSelectedValues(List<Object> toSelect) {
		if (toSelect == null)
			return;

		ListModel<Object> model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			Object element = model.getElementAt(i);

			if (toSelect.contains(element)) {

				visibleList.addSelectionInterval(i, i);
			}
		}

		visibleList.ensureIndexIsVisible(visibleList.getMaxSelectionIndex());

	}

	public void initSelection() {
		if (visibleList.getModel().getSize() > 0)
			visibleList.setSelectedIndex(0);
	}

	public void setSelectedValues(List<Object> toSelect) {

		initiallySelected = toSelect;

		visibleList.clearSelection();

		addSelectedValues(toSelect);

	}

	public void setSelectedValue(Object ob) {

		visibleList.setSelectedValue(ob, true);
	}

	private void addElementFromExtraField(Object element) {

		addElement(element);

		// ever event

		extraFieldChanged(false);
	}

	protected void addElement(Object element) {

		ListModel<Object> limo = visibleList.getModel();
		if (limo instanceof DefaultListModel) {
			if (!((DefaultListModel<Object>) limo).contains(element)) {
				((DefaultListModel<Object>) limo).addElement(element);
				List<Object> list = new ArrayList<>();
				list.add(element);
				addSelectedValues(list);
			}
		} else {
			Logging.error(this, "for element adding required instance of DefaultListModel");
		}

	}

	@Override
	public Object getValue() {
		return getSelectedList();
	}

	private void setTracker(Object s) {
		if (tracker != null)
			tracker.setText("" + s);
	}

	@Override
	public void setStartValue(Object s) {

		super.setStartValue(s);
		setTracker(s);
	}

	@Override
	protected void cancel() {
		setListModel(initialModel);
		setSelectedValues(initiallySelected);

		super.cancel();

		if (celleditor != null)
			celleditor.stopCellEditing();
	}

	@Override
	protected void commit() {
		Logging.debug(this, "FEditList.commit");

		if (Globals.forbidEditingTargetSpecific()) {
			Logging.debug(this, "commit: forbidden");
			cancel();
		} else {
			if (celleditor != null)
				celleditor.stopCellEditing();

			if (leaveOnCommit)
				leave();
		}
	}

	// interface ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		} else if (e.getSource() == buttonRemove) {
			visibleList.clearSelection();
		}
	}

	// interface KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);

		if (e.getSource() == buttonAdd) {
			addElementFromExtraField(extraField.getText());
		} else if (e.getSource() == buttonRemove) {
			visibleList.clearSelection();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	// interface MouseListener

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1)
			setExtraFieldToListValueAt(e.getPoint());

	}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	@Override
	public boolean init() {
		setStartValue(selValue);
		return super.init();
	}

	public List getSelectedList() {
		List result = new ArrayList<>();

		ListModel model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			Object element = model.getElementAt(i);
			if (visibleList.isSelectedIndex(i))
				result.add(element);
		}

		return result;
	}

	@Override
	protected void initEditing() {
		super.initEditing();
		Logging.debug(this, "FEditList.initEditing");
		buttonRemove.setEnabled(true);
	}

	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		List selectedList = getSelectedList();

		if (!nullable && selectedList.isEmpty()) {
			// reset to some value
			initSelection();
		}
		setDataChanged(true);

		setTracker(selectedList);

		buttonRemove.setEnabled(!selectedList.isEmpty());
	}

}
