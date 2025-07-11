/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.tree.AbstractGroupTree;
import de.uib.configed.share.logging.Logging;

public class ProductTable extends JTable {
	public ProductTable() {
		super.setDragEnabled(true);
		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		Set<String> saveSelectedProducts = getSelectedIDs();
		// only in case of setting ActionRequest needed, since we there call
		// fireTableDataChanged
		super.setValueAt(value, row, column);
		setSelection(saveSelectedProducts);
	}

	public void setSelection(Set<String> selectedIDs) {
		getSelectionModel().setValueIsAdjusting(true);

		clearSelection();

		int col = getColumnIndexByTitle(Configed.getResourceValue("InstallationStateTableModel.productId"));

		if (selectedIDs == null || selectedIDs.isEmpty()) {
			Logging.info("selectedIds is null or empty");
		} else {
			for (int row = 0; row < getRowCount(); row++) {
				Object productId = getValueAt(row, col);
				if (selectedIDs.contains(productId)) {
					addRowSelectionInterval(row, row);
				}
			}
		}

		getSelectionModel().setValueIsAdjusting(false);
	}

	private void reduceToSet(Set<String> filter) {
		InstallationStateTableModel tModel = (InstallationStateTableModel) getModel();
		tModel.setFilterFrom(filter);

		Logging.info(this, "reduceToSet  ", filter);

		revalidate();
	}

	public void reduceToSelected() {
		Set<String> selection = getSelectedIDs();
		Logging.debug(this, "reduceToSelected: selectedIds  ", selection);
		reduceToSet(selection);
		setSelection(selection);
	}

	public void nodeSelection(DefaultMutableTreeNode node) {
		if (node.getAllowsChildren()) {
			Set<String> productIds = AbstractGroupTree.getChildrenRecursively(node);
			setFilter(productIds);
		} else {
			Set<String> productIds = Collections.singleton(node.toString());
			setFilter(productIds);
			setSelection(productIds);
		}
	}

	public void setFilter(Set<String> filter) {
		if (getModel() instanceof InstallationStateTableModel installationStateTableModel) {
			installationStateTableModel.setFilterFrom(filter);
		}
	}

	public Set<String> getSelectedIDs() {
		Set<String> result = new HashSet<>();
		int col = getColumnIndexByTitle(Configed.getResourceValue("InstallationStateTableModel.productId"));

		for (int selectionElement : getSelectedRows()) {
			result.add((String) getValueAt(selectionElement, col));
		}

		return result;
	}

	/**
	 * Returns the index of the column with the given title.
	 * 
	 * @param columnTitle
	 * @return index of the column with the given title or -1 if not found
	 */
	private int getColumnIndexByTitle(String columnTitle) {
		try {
			return convertColumnIndexToView(getColumn(columnTitle).getModelIndex());
		} catch (IllegalArgumentException e) {
			Logging.info(this, e, "getColumnIndexByTitle: ", columnTitle, " not found");
			return -1;
		}
	}

	/**
	 * Returns the column title and sortorder of current sort keys.
	 * 
	 * @return List of pairs of column title and sort order
	 */
	public Map<String, SortOrder> getSortedNames() {
		List<? extends SortKey> saveSortKeys = getSortKeys();
		if (saveSortKeys == null || saveSortKeys.isEmpty() || getColumnCount() == 0) {
			Logging.debug(this, "getSortedNames sort keys is null or empty");
			return Collections.emptyMap();
		}
		Logging.debug(this, "getSortedNames sort keys ", saveSortKeys);
		// This needs to be a LinkedHashMap since the ordering is important
		Map<String, SortOrder> sortKeyNames = new LinkedHashMap<>();
		for (SortKey sortKey : saveSortKeys) {
			String columnKey = getColumnName(sortKey.getColumn());
			Logging.debug("\tColumn index ", sortKey.getColumn(), " columnKey ", columnKey, " sortOrder ",
					sortKey.getSortOrder());
			if (columnKey == null || columnKey.isEmpty()) {
				continue;
			}
			sortKeyNames.put(columnKey, sortKey.getSortOrder());
		}
		Logging.debug(this, "getSortedNames sort names ", sortKeyNames);
		return sortKeyNames;
	}

	/**
	 * Get list of SortKey objects (includes indexes) from list of column names
	 * 
	 * @param sortKeyNames
	 * @return
	 */
	private List<SortKey> getSortedKeysByNames(Map<String, SortOrder> sortKeyNames) {
		List<SortKey> newSortKeys = new ArrayList<>();
		for (Entry<String, SortOrder> entry : sortKeyNames.entrySet()) {
			int columnIndex = getColumnIndexByTitle(entry.getKey());
			if (columnIndex != -1) {
				newSortKeys.add(new SortKey(columnIndex, entry.getValue()));
			}
		}
		Logging.debug(this, "getSortedKeysByNames new sort keys ", newSortKeys);
		return newSortKeys;
	}

	/**
	 * Set the sort keys of the table model by the given column names and sort
	 * orders. Use default sort keys if the list is null or empty.
	 * 
	 * @param sortKeyNames
	 */
	public void setSortedByNames(Map<String, SortOrder> sortKeyNames) {
		Logging.debug(this, "sortKeyNames sort key names ", sortKeyNames);
		if (sortKeyNames == null || sortKeyNames.isEmpty()) {
			// use default sort keys
			setSortKeys(getPrimaryOrderingKeys());
		} else {
			setSortKeys(getSortedKeysByNames(sortKeyNames));
		}
	}

	/**
	 * Returns the (default) ordering keys of the table model. Tries to get the
	 * index of the default column name "productId". If not found, use 0 as
	 * index.
	 * 
	 * @return
	 */
	private List<SortKey> getPrimaryOrderingKeys() {
		Logging.debug(this, "getPrimaryOrderingKeys, use index of productId column or 0");
		List<SortKey> primaryOrderingKeys = new ArrayList<>();
		// try getting index of column clientName (it might not be zero, because of new column "platform")
		int sortIndex = getColumnIndexByTitle(Configed.getResourceValue("InstallationStateTableModel.productId"));
		if (sortIndex == -1) {
			sortIndex = 0;
		}
		primaryOrderingKeys.add(new SortKey(sortIndex, SortOrder.ASCENDING));
		return primaryOrderingKeys;
	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		if (getRowSorter() != null) {
			return getRowSorter().getSortKeys();
		} else {
			return getPrimaryOrderingKeys();
		}
	}

	public void setSortKeys(List<? extends SortKey> currentSortKeys) {
		Logging.info(this, "setSortKeys : ", currentSortKeys);
		if (getRowSorter() != null) {
			getRowSorter().setSortKeys(currentSortKeys);
		}
	}

	public List<Integer> getSelectedRowsInModelTerms() {
		int[] selection = getSelectedRows();
		List<Integer> selectionInModelTerms = new ArrayList<>(selection.length);
		for (int selectionElement : selection) {
			selectionInModelTerms.add(convertRowIndexToModel(selectionElement));
		}

		return selectionInModelTerms;
	}

	public JTable getStrippedTable() {
		boolean strippIt;
		List<String[]> data = new ArrayList<>();
		String[] headers = new String[getColumnCount()];
		for (int i = 0; i < getColumnCount(); i++) {
			headers[i] = getColumnName(i);
		}

		for (int j = 0; j < getRowCount(); j++) {
			strippIt = true;
			String[] actCol = new String[getColumnCount()];
			for (int i = 0; i < getColumnCount(); i++) {
				Object cellValue = getValueAt(j, i);
				String cellValueString = cellValue == null ? "" : cellValue.toString();
				actCol[i] = cellValueString;
				strippIt = shouldStrippIt(getColumnName(i), cellValueString, strippIt);
			}

			if (!strippIt) {
				data.add(actCol);
			}
		}

		// create jTable with selected rows
		int rows = data.size();
		int cols = getColumnCount();
		String[][] strippedData = new String[rows][cols];
		for (int i = 0; i < data.size(); i++) {
			strippedData[i] = data.get(i);
		}
		return new JTable(strippedData, headers);
	}

	private boolean shouldStrippIt(String columnName, String cellValueString, boolean previuosValue) {
		boolean strippIt = previuosValue;

		if (Configed.getResourceValue("InstallationStateTableModel.installationStatus").equals(columnName)
				&& !InstallationStatus.KEY_NOT_INSTALLED.equals(cellValueString)) {
			strippIt = false;
		} else if (Configed.getResourceValue("InstallationStateTableModel.report").equals(columnName)
				&& (cellValueString != null && !cellValueString.isEmpty())) {
			strippIt = false;
		} else if (Configed.getResourceValue("InstallationStateTableModel.actionRequest").equals(columnName)
				&& !"none".equals(cellValueString)) {
			strippIt = false;
		} else {
			Logging.warning(this, "no case found for columnName in jTable");
		}

		return strippIt;
	}

	public void valueChanged(boolean doSelection, TreePath[] selectionPaths) {
		if (selectionPaths == null) {
			setFilter(null);
		} else if (selectionPaths.length == 1) {
			nodeSelection((DefaultMutableTreeNode) selectionPaths[0].getLastPathComponent());
		} else {
			Set<String> productIds = new HashSet<>();
			for (TreePath path : selectionPaths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (!node.getAllowsChildren()) {
					productIds.add(node.getUserObject().toString());
				}
			}
			setFilter(productIds);

			if (doSelection) {
				setSelection(productIds);
			}
		}
	}
}
