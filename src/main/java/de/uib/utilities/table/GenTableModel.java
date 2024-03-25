/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;

public class GenTableModel extends AbstractTableModel {
	private static final String DEFAULT_FILTER_NAME = "default";

	public static final String LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED = "showOnlySelected";

	private int rowsLength;
	private int colsLength;
	private List<String> columnNames;
	private List<List<Object>> rows;

	private Set<Integer> addedRows;
	// rows which are added and not yet saved
	private Set<Integer> updatedRows;
	// rows which are updated and not yet saved

	private List<Integer> finalCols;
	// columns for which the values can only be entered and changed as long as the
	// row is not saved

	private boolean[] colEditable;
	// columns which are editable in principle (but they may be final)

	private int keyCol = -1;
	private List<MapBasedTableEditItem> updates;
	private boolean modelDataValid;

	private DefaultTableProvider tableProvider;
	private MapTableUpdateItemFactory itemFactory;

	private final ChainedTableModelFilter chainedFilter = new ChainedTableModelFilter();
	private TableModelFilter workingFilter;

	private Integer sortCol;
	private boolean sorting;
	private int cursorrow = -1;
	private boolean markCursorRow;
	private int colMarkCursorRow = -1;

	private CursorrowObserved cursorrowObservable;

	public GenTableModel(MapTableUpdateItemFactory itemFactory, DefaultTableProvider dataProvider, int keyCol,
			int[] finalColumns, TableModelListener l, List<MapBasedTableEditItem> updates,
			boolean cancelRequestReload) {
		this.keyCol = keyCol;
		this.updates = updates;
		this.tableProvider = dataProvider;
		this.itemFactory = itemFactory;

		cursorrowObservable = new CursorrowObserved();

		initColumns();

		if (cancelRequestReload) {
			tableProvider.cancelRequestReload();
		}

		setRows(dataProvider.getRows());

		addedRows = new HashSet<>();
		updatedRows = new HashSet<>();

		this.finalCols = new ArrayList<>();
		if (finalColumns == null) {
			if (keyCol > -1) {
				this.finalCols.add(keyCol);
			}
		} else {
			for (int finalColumn : finalColumns) {
				this.finalCols.add(finalColumn);
			}
		}

		modelDataValid = false;

		if (rows == null) {
			rowsLength = 0;
		} else {
			rowsLength = rows.size();
		}

		if (l != null) {
			super.addTableModelListener(l);
		}

		workingFilter = chainedFilter;
	}

	public GenTableModel(MapTableUpdateItemFactory itemFactory, DefaultTableProvider dataProvider, int keyCol,
			int[] finalColumns, TableModelListener l, List<MapBasedTableEditItem> updates) {
		this(itemFactory, dataProvider, keyCol, finalColumns, l, updates, false);
	}

	public GenTableModel(MapTableUpdateItemFactory itemFactory, DefaultTableProvider dataProvider, int keyCol,
			TableModelListener l, List<MapBasedTableEditItem> updates) {
		this(itemFactory, dataProvider, keyCol, null, l, updates, false);
	}

	private void initColumns() {
		columnNames = tableProvider.getColumnNames();
		Logging.info(this, "initColumns " + columnNames);

		if (columnNames == null) {
			colsLength = 0;
		} else {
			colsLength = columnNames.size();
		}

		colEditable = new boolean[colsLength];
	}

	public List<String> getColumnNames() {
		columnNames = tableProvider.getColumnNames();
		colsLength = columnNames.size();
		return columnNames;
	}

	public int getKeyCol() {
		return keyCol;
	}

	public List<Integer> getFinalCols() {
		return finalCols;
	}

	public void invalidate() {
		// is needed
		tableProvider.requestReturnToOriginal();
		modelDataValid = false;
	}

	public boolean isReloadRequested() {
		return !modelDataValid;
	}

	public void requestReload() {
		modelDataValid = false;
		tableProvider.requestReloadRows();
	}

	public void startWithCurrentData() {
		tableProvider.setWorkingCopyAsNewOriginalRows();
		invalidate();
	}

	/**
	 * define a complete new filter by a TableModelFilterCondition
	 */
	public void setFilterCondition(TableModelFilterCondition cond) {
		Logging.info(this, "setFilterCondition " + cond);
		clearFilter();
		chainedFilter.set(DEFAULT_FILTER_NAME, new TableModelFilter(cond));

		// TableModelFilter
	}

	public void clearFilter() {
		chainedFilter.clear();
	}

	public String getFilterInfo() {
		return chainedFilter.toString();
	}

	/**
	 * sets (puts) a filter for a filtername
	 */
	public GenTableModel chainFilter(String filterName, TableModelFilter filter) {
		chainedFilter.set(filterName, filter);
		Logging.info(this, "chainFilter, we have chainedFilter " + chainedFilter);

		// for chaining in notation
		return this;
	}

	public Set<Object> getExistingKeys() {
		int keycol = getKeyCol();

		if (keycol < 0) {
			return new HashSet<>();
		}

		TreeSet<Object> result = new TreeSet<>();
		for (int row = 0; row < getRowCount(); row++) {
			if (getValueAt(row, keycol) != null) {
				result.add(getValueAt(row, keycol));
			}
		}

		return result;
	}

	public void produceRows() {
		Logging.info(this, " ---  produce rows");
		setRows(tableProvider.getRows());

		Logging.info(this, "produceRows(): count  " + rows.size());

		rowsLength = rows.size();

		// update columns and class names from tableProvider
		getColumnNames();

		Logging.info(this, " ---  rows produced, columnNames: " + columnNames);
		Logging.info(this,
				"produceRows --- using workingfilter  " + workingFilter.getClass().getName() + " : " + workingFilter);

		if (workingFilter != null && workingFilter.isInUse()) {
			Logging.info(this, " --- using workingfilter  " + workingFilter.getClass().getName());
			List<List<Object>> filteredRows = new ArrayList<>();

			for (List<Object> row : rows) {
				if (workingFilter.test(row)) {
					filteredRows.add(row);
				}
			}
			setRows(filteredRows);
			rowsLength = rows.size();
		}

		Logging.info(this, "produceRows  filtered size " + rows.size());
	}

	public void setSorting(Integer sortCol, boolean sorting) {
		if (this.sorting != sorting || !this.sortCol.equals(sortCol)) {
			this.sortCol = sortCol;

			if (sortCol == null || sortCol < 0 || sortCol >= getRowCount()) {
				this.sorting = false;
			} else {
				this.sorting = sorting;
			}

			Logging.info(this, "setSorting: we reset");
			invalidate();
			reset();
		}
	}

	public List<List<Object>> getRows() {
		return rows;
	}

	private void setRows(List<List<Object>> givenRows) {
		Logging.info(this, "setRows size " + givenRows.size());
		if (!sorting) {
			rows = givenRows;
		} else {
			java.text.Collator myCollator = java.text.Collator.getInstance();

			// as capitalization
			myCollator.setStrength(java.text.Collator.SECONDARY);

			TreeMap<String, List<Object>> mapRows = new TreeMap<>(myCollator);

			int col = sortCol;

			// we use the index to get unique values in any col
			int i = 0;
			for (List<Object> row : givenRows) {
				mapRows.put(row.get(col).toString() + ":" + i, row);
				i++;
			}
			rows = new ArrayList<>();
			for (List<Object> value : mapRows.values()) {
				rows.add(value);
			}
		}
	}

	private void refresh() {
		if (!modelDataValid) {
			// perhaps the action should not depend on this condition,
			// since the necessary requests are already sent to the
			// table provider

			produceRows();

			fireTableDataChanged();
			modelDataValid = true;
		}
	}

	public TableModelFilter getFilter(String name) {
		return chainedFilter.getElement(name);
	}

	public boolean isUsingFilter(String name) {
		if (name == null) {
			Logging.info(this, "isUsingFilter, name == null");
			return false;
		}

		if (chainedFilter.getElement(name) == null) {
			Logging.info(this, "isUsingFilter, chainedFilter.getElement for name " + name + " == null");
			return false;
		}

		return chainedFilter.getElement(name).isInUse();
	}

	public void setUsingFilter(String name, boolean newValue) {
		Logging.info(this, "setUsingFilter " + name + " to " + newValue + " value was " + isUsingFilter(name));
		if (isUsingFilter(name) != newValue) {
			invalidate();
			chainedFilter.getElement(name).setInUse(newValue);
			reset();
		}

		Logging.info(this, "setUsingFilter active filter chain: " + chainedFilter.getActiveFilters());
		Logging.info(this, "setUsingFilter we got rows: " + getRowCount());
	}

	private void clearUpdates() {
		addedRows.clear();
		updatedRows.clear();
	}

	/**
	 * Retrieves locally available data and applies it to a model (without
	 * retrieving it from the server).
	 */
	public void resetLocally() {
		Logging.info(this, "resetLocal");
		tableProvider.refreshRows();
		reset();
	}

	/**
	 * sets data to the source values (if model is not valid they are
	 * recollected) clears update collection
	 */
	public void reset() {
		Logging.info(this, "reset()");
		refresh();
		clearUpdates();
		cursorrow = -1;
	}

	public void setEditableColumns(int[] editable) {
		for (int i = 0; i < colsLength; i++) {
			colEditable[i] = false;
		}

		if (editable == null) {
			return;
		}

		for (int editableIndex : editable) {
			colEditable[editableIndex] = true;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames.get(col);
	}

	@Override
	public int getColumnCount() {
		return colsLength;
	}

	@Override
	public int getRowCount() {
		return rowsLength;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object result = null;

		if (markCursorRow && col == colMarkCursorRow && row == cursorrow) {
			result = true;
		} else {
			result = rows.get(row).get(col);
		}

		return result;
	}

	public void setColMarkCursorRow(int col) {
		if (col > -1 && col < getColumnCount()) {
			colMarkCursorRow = col;
			markCursorRow = true;
		} else {
			markCursorRow = false;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (!addedRows.contains(row) && finalCols.contains(col)) {
			// we cannot edit a key column but when it is not saved in the data backend
			return false;
		} else {
			return colEditable[col];
		}
	}

	public void addCursorrowObserver(CursorrowObserver o) {
		cursorrowObservable.addObserver(o);
	}

	private void notifyCursorrowObservers(int newrow) {
		cursorrowObservable.notifyObservers(newrow);
	}

	public int getCursorRow() {
		Logging.info(this, "cursorrow " + cursorrow);
		return cursorrow;
	}

	public void setCursorRow(int modelrow) {
		if (markCursorRow) {
			Logging.info(this, "setCursorRow modelrow " + modelrow + " markCursorRow " + markCursorRow);
			if (modelrow > 0 && colsLength > 2) {
				Logging.info(this, "setCursorRow val at (modelrow,2) ) " + getValueAt(modelrow, 2));
			}
		}

		if (markCursorRow) {
			// refresh of data display is very delicate, this seems to be a working
			// combination of methods
			boolean change = false;
			if (cursorrow > -1) {
				rows.get(cursorrow).set(colMarkCursorRow, false);
				change = true;
				fireTableCellUpdated(cursorrow, colMarkCursorRow);
			}

			cursorrow = modelrow;
			if (modelrow > -1) {
				rows.get(cursorrow).set(colMarkCursorRow, true);

				change = true;
			}

			if (change) {
				fireTableCellUpdated(cursorrow, colMarkCursorRow);
				notifyCursorrowObservers(cursorrow);
			}
		}
	}

	public boolean gotMarkCursorRow() {
		return markCursorRow;
	}

	public int getColMarkCursorRow() {
		return colMarkCursorRow;
	}

	@Override
	public void setValueAt(Object newValue, int row, int col) {
		Object oldValue = rows.get(row).get(col);

		Logging.debug(this, " old value at " + row + ", " + col + " : " + oldValue);
		Logging.debug(this, " new value at " + row + ", " + col + " : " + newValue);

		if (hasValueChanged(oldValue, newValue, col)) {
			// we dont register updates for already registered rows, since there values are
			// passed via the row List
			if (!addedRows.contains(row) && !updatedRows.contains(row)) {
				rows.get(row).set(col, newValue);

				if (itemFactory == null) {
					Logging.info("update item factory missing");
				} else if (updates == null) {
					Logging.info("updates not initialized");
				} else {
					updates.add(itemFactory.produceUpdateItem(rows.get(row)));
				}

				Logging.debug(this, "updated rows add " + row);

				updatedRows.add(row);
			}

			// we cannot edit a key column after it is saved in the data backend
			if (!addedRows.contains(row) && finalCols.contains(col)) {
				// we should not get any more to this code, since for this condition the value
				// is marked as not editable
				Logging.warning("key column cannot be edited after saving the data");

				JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
						"values in this column are fixed after saving the data", "Information", JOptionPane.OK_OPTION);

				return;
			}

			// in case of an updated row we did this already
			rows.get(row).set(col, newValue);
			fireTableCellUpdated(row, col);
		}
	}

	private boolean hasValueChanged(Object oldValue, Object newValue, int col) {
		boolean valueChanged;

		if (oldValue == null && newValue == null) {
			// Both are null
			valueChanged = false;
		} else if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
			// Both are non null and equal
			valueChanged = false;
		} else {
			// Only one is null or they are unequal
			valueChanged = true;
		}

		if (valueChanged && markCursorRow && col == colMarkCursorRow) {
			valueChanged = false;
		}

		return valueChanged;
	}

	// does not set values to null, leaves instead the original value
	// if the values map produces a null
	public void updateRowValues(int row, Map<String, Object> values) {
		for (int col = 0; col < columnNames.size(); col++) {
			Object val = values.get(getColumnName(col));

			if (val != null) {
				setValueAt(val, row, col);
			}
		}
	}

	public void addRow(Object[] a) {
		List<Object> rowV = new ArrayList<>();
		for (int i = 0; i < colsLength; i++) {
			rowV.add(null);
		}
		for (int j = 0; j < a.length; j++) {
			rowV.set(j, a[j]);
		}

		Logging.debug(this, "--- addRow size, row " + rowV.size() + ", " + rowV);

		rows.add(rowV);
		addedRows.add(rowsLength);

		updates.add(itemFactory.produceUpdateItem(rowV));

		// we shall have to reload the data if keys are newly generated
		requestReload();

		rowsLength++;

		fireTableRowsInserted(rowsLength - 1, rowsLength - 1);
	}

	private boolean checkDeletionOfAddedRow(int rowNum) {
		if (addedRows.contains(rowNum)) {
			// Deletion of added rows is not adequately managed,
			// therefore we reject it.
			Logging.info(this, "no deletion of added rows");

			JOptionPane.showMessageDialog(null, "no deletion of added rows, please save or cancel editing",
					"Information", JOptionPane.OK_OPTION);
			return false;
		}
		return true;
	}

	public void deleteRows(int[] selection) {
		Logging.debug(this, "deleteRows " + Arrays.toString(selection));

		if (selection == null || selection.length == 0) {
			return;
		}

		Arrays.sort(selection);
		Logging.debug(this, "deleteRows, sorted " + Arrays.toString(selection));

		if (updates == null) {
			Logging.info(this, "updates not initialized");
			return;
		}

		for (int element : selection) {
			deleteRow(element);
		}
	}

	public void deleteRow(int rowNum) {
		Logging.debug(this, "deleteRow " + rowNum);

		if (itemFactory == null) {
			Logging.info("update item factory missing");
			return;
		}

		if (updates == null) {
			Logging.info("updates not initialized");
			return;
		}

		if (rows.get(rowNum) == null) {
			Logging.info(this, "delete row null ");
			return;
		}

		if (!checkDeletionOfAddedRow(rowNum)) {
			return;
		}

		List<Object> oldValues = new ArrayList<>(rows.get(rowNum));
		Logging.debug(this, "deleteRow values " + oldValues);
		updates.add(itemFactory.produceDeleteItem(oldValues));
		// we have to delete the source values, not the possibly changed current row

		if (updatedRows.contains(rowNum)) {
			Logging.debug(this, "deleteRow, remove from updatedRows  " + rowNum);
			updatedRows.remove(rowNum);
		}

		rows.remove(rowNum);
		rowsLength--;
		fireTableRowsDeleted(rowNum, rowNum);
	}

	@Override
	public String toString() {
		String info = "";
		if (columnNames != null) {
			info = " columns: " + columnNames;
		}

		return super.toString() + info;
	}
}
