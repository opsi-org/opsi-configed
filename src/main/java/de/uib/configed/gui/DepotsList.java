/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import de.uib.configed.ConfigedMain;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;

public class DepotsList extends JList<String> implements ComponentListener {
	private DepotListCellRenderer myListCellRenderer;
	private List<String> saveV;
	private Map<String, Map<String, Object>> depotInfo;

	public DepotsList(ConfigedMain configedMain) {
		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		myListCellRenderer = new DepotListCellRenderer(configedMain);
		super.setCellRenderer(myListCellRenderer);
		super.setSelectionModel(new DepotListSelectionModel());
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

	/*
	 * We want to override this method to assure that selecting all these indices
	 * will fire only one event in ListSelectionListener
	 */
	@Override
	public void setSelectedIndices(int[] indices) {
		getSelectionModel().setValueIsAdjusting(true);
		super.setSelectedIndices(indices);
		getSelectionModel().setValueIsAdjusting(false);
	}

	public void setSelectedValues(Iterable<String> valuesToSelect) {
		List<Integer> savedSelectedDepots = new ArrayList<>();
		// we collect the indices of the old depots in the current list

		for (String selectDepot : valuesToSelect) {
			for (int j = 0; j < getModel().getSize(); j++) {
				if (getModel().getElementAt(j).equals(selectDepot)) {
					savedSelectedDepots.add(j);
				}
			}
		}

		int[] depotsToSelect = new int[savedSelectedDepots.size()];
		for (int j = 0; j < depotsToSelect.length; j++) {
			// conversion to int
			depotsToSelect[j] = savedSelectedDepots.get(j);
		}

		setSelectedIndices(depotsToSelect);
	}

	public void selectAll() {
		setSelectionInterval(0, getModel().getSize() - 1);
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

	private class DepotListSelectionModel extends DefaultListSelectionModel {
		@SuppressWarnings({ "java:S2234" })
		@Override
		public void setSelectionInterval(int index0, int index1) {
			if (index0 < index1) {
				selectAllowedDepots(index0, index1);
			} else {
				selectAllowedDepots(index1, index0);
			}
		}

		private void selectAllowedDepots(int index0, int index1) {
			for (int i = index0; i <= index1; i++) {
				Object value = getModel().getElementAt(i);
				if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
						.hasDepotPermission((String) value)) {
					super.setSelectionInterval(index0, index1);
				}
			}
		}
	}
}
