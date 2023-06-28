/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.ListSelectionModel;

import de.uib.Main;
import de.uib.configed.Globals;

public class DepotsList extends JList<String> implements ComponentListener {

	private MyListCellRenderer myListCellRenderer;
	private List<String> saveV;

	private Map<String, Map<String, Object>> depotInfo;

	public DepotsList() {
		if (!Main.THEMES) {
			super.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			super.setSelectionBackground(Globals.defaultTableCellSelectedBgColor);
			super.setSelectionForeground(Globals.DEPOTS_LIST_FOREGROUND_COLOR);
		}

		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		myListCellRenderer = new MyListCellRenderer();
		super.setCellRenderer(myListCellRenderer);
	}

	public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
		myListCellRenderer.setInfo(extendedInfo);
		this.depotInfo = extendedInfo;
	}

	public Map<String, Map<String, Object>> getDepotInfo() {
		return depotInfo;
	}

	public void setListData(List<String> v) {
		super.setListData(v.toArray(new String[0]));
		saveV = v;
	}

	// interface ComponentListene
	@Override
	public void componentHidden(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentMoved(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentResized(ComponentEvent e) {
		ensureIndexIsVisible(getSelectedIndex());
	}

	@Override
	public void componentShown(ComponentEvent e) {
		ensureIndexIsVisible(getSelectedIndex());
	}
	// ===

	private int getIndexOf(Object value) {
		return saveV.indexOf(value);
	}

	public void selectAll() {
		getSelectionModel().setValueIsAdjusting(true);
		setSelectionInterval(0, getModel().getSize() - 1);
		getSelectionModel().setValueIsAdjusting(false);
	}

	public void addToSelection(List<String> depots) {
		if (depots == null || depots.isEmpty()) {
			return;
		}

		getSelectionModel().setValueIsAdjusting(true);

		for (String depot : depots) {
			int i = getIndexOf(depot);
			if (i > -1) {
				addSelectionInterval(i, i);
			}
		}
		getSelectionModel().setValueIsAdjusting(false);
	}
}
