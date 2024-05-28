/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.StandardListCellRenderer;
import de.uib.utils.table.gui.SensitiveCellEditor;

public class FEditList<O> extends FEditObject implements ListSelectionListener, MouseListener {
	protected JButton buttonRemove;
	private JScrollPane scrollpane;

	protected JList<O> visibleList;

	private JTextComponent tracker;

	private List<O> initiallySelected;

	protected Object selValue = "";

	protected JPopupMenu popup;

	private SensitiveCellEditor celleditor;

	private boolean nullable;

	public FEditList() {
		this(null, null);
	}

	public FEditList(JTextComponent tracker, SensitiveCellEditor celleditor) {
		super("");
		this.tracker = tracker;
		this.celleditor = celleditor;

		visibleList = new JList<>();
		visibleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleList.addListSelectionListener(this);
		visibleList.setCellRenderer(new StandardListCellRenderer());

		visibleList.addMouseListener(this);

		scrollpane = new JScrollPane();
		scrollpane.setViewportView(visibleList);
		editingArea.add(scrollpane, BorderLayout.CENTER);
		editable = false;

		popup = new JPopupMenu();
		visibleList.setComponentPopupMenu(popup);
	}

	@Override
	protected void createComponents() {
		super.createComponents();

		// we define buttonRemove in a different way since it is used only to clear
		// selection

		buttonRemove = new JButton(Utils.getIntellijIcon("clearCash"));
		buttonRemove.setToolTipText(Configed.getResourceValue("FEditObject.RemoveButtonTooltip"));
		buttonRemove.setPreferredSize(new Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));
		buttonRemove.setVisible(false);
	}

	@Override
	protected void initComponents() {
		super.initComponents();
		visibleList.addKeyListener(this);
	}

	public void setCellRenderer(ListCellRenderer<Object> render) {
		visibleList.setCellRenderer(render);
	}

	public void setListModel(ListModel<O> model) {
		visibleList.setModel(model);
	}

	public void setSelectionMode(int selectionMode) {
		visibleList.setSelectionMode(selectionMode);
		buttonRemove.setVisible(selectionMode != ListSelectionModel.SINGLE_SELECTION);
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
	}

	private void addSelectedValues(List<O> toSelect) {
		if (toSelect == null) {
			return;
		}

		ListModel<O> model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			Object element = model.getElementAt(i);

			if (toSelect.contains(element)) {
				visibleList.addSelectionInterval(i, i);
			}
		}

		visibleList.ensureIndexIsVisible(visibleList.getMaxSelectionIndex());
	}

	public void initSelection() {
		if (visibleList.getModel().getSize() > 0) {
			visibleList.setSelectedIndex(0);
		}
	}

	public void setSelectedValues(List<O> toSelect) {
		initiallySelected = toSelect;

		visibleList.clearSelection();

		addSelectedValues(toSelect);
	}

	public void setSelectedValue(O ob) {
		visibleList.setSelectedValue(ob, true);
	}

	protected void addElement(O element) {
		ListModel<O> limo = visibleList.getModel();
		if (limo instanceof DefaultListModel) {
			if (!((DefaultListModel<O>) limo).contains(element)) {
				((DefaultListModel<O>) limo).addElement(element);
				List<O> list = new ArrayList<>();
				list.add(element);
				addSelectedValues(list);
			}
		} else {
			Logging.error(this, "for element adding required instance of DefaultListModel");
		}
	}

	public void setPreferredScrollPaneSize(Dimension size) {
		scrollpane.setPreferredSize(size);
	}

	@Override
	public Object getValue() {
		return getSelectedList();
	}

	private void setTracker(Object s) {
		if (tracker != null) {
			tracker.setText("" + s);
		}
	}

	@Override
	public void setStartValue(Object s) {
		super.setStartValue(s);
		setTracker(s);
	}

	@Override
	protected void cancel() {
		setSelectedValues(initiallySelected);

		super.cancel();

		if (celleditor != null) {
			celleditor.stopEditingAndSave();
		}
	}

	@Override
	protected void commit() {
		Logging.debug(this, "FEditList.commit");

		if (forbidEditingTargetSpecific()) {
			Logging.debug(this, "commit: forbidden");
			cancel();
		} else {
			if (celleditor != null) {
				celleditor.stopEditingAndSave();
			}

			if (leaveOnCommit) {
				setVisible(false);
			}
		}
	}

	// interface ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (e.getSource() == buttonRemove) {
			visibleList.clearSelection();
		}
	}

	// interface KeyListener
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);

		if (e.getSource() == buttonRemove) {
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
		if (e.getClickCount() == 2) {
			String txt = "" + visibleList.getModel().getElementAt(visibleList.locationToIndex(e.getPoint()));

			extraField.setText(txt);
			extraFieldChanged(false);
		}
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
	public void init() {
		setStartValue(selValue);
		super.init();
	}

	public List<O> getSelectedList() {
		List<O> result = new ArrayList<>();

		ListModel<O> model = visibleList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			O element = model.getElementAt(i);
			if (visibleList.isSelectedIndex(i)) {
				result.add(element);
			}
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
		List<O> selectedList = getSelectedList();

		if (!nullable && selectedList.isEmpty()) {
			// reset to some value
			initSelection();
		}
		setDataChanged(true);

		setTracker(selectedList);

		buttonRemove.setEnabled(!selectedList.isEmpty());
	}
}
