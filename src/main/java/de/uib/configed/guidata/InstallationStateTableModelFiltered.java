/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

public class InstallationStateTableModelFiltered extends InstallationStateTableModel {
	public static final String STATE_TABLE_FILTERS_PROPERTY = "stateTableFilters";

	private String savedStateObjTag;

	private int[] filter;
	// filter is a function
	// row --> somerow (from super.table)

	// it holds
	// product(row) = product(somerow)

	private int[] filterInverse;

	public InstallationStateTableModelFiltered(String[] selectedClients, ConfigedMain main,
			Map<String, Map<String, Map<String, String>>> collectChangedStates, List<String> listOfInstallableProducts,
			Map<String, List<Map<String, String>>> statesAndActions, Map<String, List<String>> possibleActions,
			Map<String, Map<String, Object>> productGlobalInfos, List<String> displayColumns, String savedStateObjTag) {
		super(selectedClients, main, collectChangedStates, listOfInstallableProducts, statesAndActions, possibleActions,
				productGlobalInfos, displayColumns);

		this.savedStateObjTag = savedStateObjTag;
	}

	private void saveFilterSet(Set<String> filterSet) {
		if (filterSet != null) {
			Configed.savedStates.setProperty(savedStateObjTag + "." + STATE_TABLE_FILTERS_PROPERTY,
					filterSet.toString());
			Logging.info(this, "saveFilterSet " + filterSet);
		} else {
			Configed.savedStates.remove(savedStateObjTag + "." + STATE_TABLE_FILTERS_PROPERTY);
		}
	}

	public void resetFilter() {
		Set<String> filterSaved = new HashSet<>(Arrays
				.asList(Configed.savedStates.getProperty(savedStateObjTag + "." + STATE_TABLE_FILTERS_PROPERTY, "")
						.replaceAll("\\[|\\]|\\s", "").split(",")));

		if (filterSaved.isEmpty()) {
			setFilterFrom((Set<String>) null);
		} else {
			Set<String> productsOnlyInFilterSet = new HashSet<>(filterSaved);
			productsOnlyInFilterSet.removeAll(tsProductNames);
			filterSaved.removeAll(productsOnlyInFilterSet);
			// A - (A - B) is the intersection

			setFilterFrom(filterSaved);
			Logging.debug(this, "resetFilter " + filterSaved);
		}
	}

	public void setFilterFrom(Set<String> ids) {
		saveFilterSet(ids);

		Set<String> reducedIds = null;
		if (ids != null) {
			Logging.info(this, "setFilterFrom, save set " + ids.size());
			reducedIds = new HashSet<>(productsV);
			if (!ids.isEmpty()) {
				reducedIds.retainAll(ids);
			}
		}

		if (reducedIds == null) {
			setFilter((int[]) null);
		} else {
			filter = new int[reducedIds.size()];
			int i = 0;

			String[] products = new String[reducedIds.size()];
			for (String id : reducedIds) {
				products[i] = id;
				i++;
			}

			for (i = 0; i < reducedIds.size(); i++) {
				filter[i] = productsV.indexOf(products[i]);
			}

			setFilter(filter);
		}
	}

	private void setFilter(int[] filter) {
		Logging.info(this, "setFilter " + Arrays.toString(filter));
		this.filter = filter;

		if (filter == null) {
			filterInverse = null;
		} else {
			filterInverse = new int[super.getRowCount()];
			for (int j = 0; j < super.getRowCount(); j++) {
				filterInverse[j] = -1;
			}
			for (int i = 0; i < filter.length; i++) {
				filterInverse[filter[i]] = i;
			}

			Logging.info(this, "setFilter: filter, filterInverse " + Arrays.toString(filter) + ", "
					+ Arrays.toString(filterInverse));
		}

		fireTableDataChanged();
	}

	private int originRow(int i) {
		if (filter == null) {
			return i;
		}

		if (i >= filter.length) {
			Logging.info(this, "originRow, error cannot evaluate filter; i, filter.length " + i + ", " + filter.length);
			return i;
		}

		return filter[i];
	}

	@Override
	public int getRowFromProductID(String id) {
		int superRow = super.getRowFromProductID(id);
		if (filterInverse == null) {
			return superRow;
		}

		return filterInverse[superRow];
	}

	// table model
	@Override
	public int getRowCount() {
		if (filter == null) {
			return super.getRowCount();
		} else {
			return filter.length;
		}
	}

	@Override
	public Object getValueAt(int row, int displayCol) {
		return super.getValueAt(originRow(row), displayCol);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return super.isCellEditable(originRow(row), col);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		super.changeValueAt(value, originRow(row), col);
		fireTableCellUpdated(row, col);
	}

	// ComboBoxModeller
	@Override
	public ComboBoxModel<String> getComboBoxModel(int row, int column) {
		return super.getComboBoxModel(originRow(row), column);
	}
}
