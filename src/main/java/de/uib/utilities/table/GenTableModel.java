/*
 *
 * 	uib, www.uib.de, 2008-2013, 2017, 2020
 * 
 *	authors Rupert Röder, Martina Hammel
 *
 */

package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import de.uib.utilities.Mapping;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.provider.TableProvider;
import de.uib.utilities.table.updates.TableEditItem;
import de.uib.utilities.table.updates.TableUpdateItemInterface;

public class GenTableModel extends AbstractTableModel implements TableModelFunctions {

	public static final String DEFAULT_FILTER_NAME = "default";

	public static final String LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED = "showOnlySelected";

	private int rowsLength;
	private int colsLength;
	private List<String> columnNames;
	private List<String> classNames;
	private List<List<Object>> rows;

	private List<Integer> addedRows;
	// rows which are added and not yet saved
	private List<Integer> updatedRows;
	// rows which are updated and not yet saved

	private List<Integer> finalCols;
	// columns for which the values can only be entered and changed as long as the
	// row is not saved

	private boolean[] colEditable;
	// columns which are editable in principle (but they may be final)

	private int keyCol = -1;
	private List<TableEditItem> updates;
	private String tableName;
	private boolean modelDataValid;
	private boolean modelStructureValid;

	private TableProvider tableProvider;
	private TableUpdateItemInterface itemFactory;
	private int saveUpdatesSize;

	private final ChainedTableModelFilter chainedFilter;
	private final TableModelFilter emptyFilter;
	private TableModelFilter workingFilter;

	private Map<Integer, RowStringMap> primarykey2Rowmap;
	private Map<Integer, String> primarykeyTranslation;
	private Mapping<Integer, String> primarykeyRepresentation;

	private Integer sortCol;
	private boolean sorting;
	private int cursorrow = -1;
	private boolean markCursorRow;
	private int colMarkCursorRow = -1;

	private CursorrowObserved cursorrowObservable;

	public GenTableModel(TableUpdateItemInterface itemFactory, TableProvider dataProvider, int keyCol,
			int[] finalColumns, TableModelListener l, List<TableEditItem> updates) {
		this.keyCol = keyCol;
		this.updates = updates;
		this.tableProvider = dataProvider;
		this.itemFactory = itemFactory;

		cursorrowObservable = new CursorrowObserved();

		initColumns();
		setRows(dataProvider.getRows());

		addedRows = new ArrayList<>();
		updatedRows = new ArrayList<>();

		this.finalCols = new ArrayList<>();
		if (finalColumns == null) {
			if (keyCol > -1) {
				this.finalCols.add(keyCol);
			}
		} else {
			for (int i = 0; i < finalColumns.length; i++) {
				this.finalCols.add(finalColumns[i]);
			}
		}

		modelDataValid = false;
		modelStructureValid = true;

		if (rows == null) {
			rowsLength = 0;
		} else {
			rowsLength = rows.size();
		}

		if (l != null) {
			super.addTableModelListener(l);
		}

		chainedFilter = new ChainedTableModelFilter();
		emptyFilter = new TableModelFilter();
		setFilter(chainedFilter);

	}

	public GenTableModel(TableUpdateItemInterface itemFactory, TableProvider dataProvider, int keyCol,
			TableModelListener l, List<TableEditItem> updates) {
		this(itemFactory, dataProvider, keyCol, null, l, updates);
	}

	public void clear() {
		colsLength = 0;
		rowsLength = 0;
		colEditable = new boolean[0];
		if (columnNames != null) {
			columnNames.clear();
		}

		if (classNames != null) {
			classNames.clear();
		}

		if (rows != null) {
			rows.clear();
		}

		clearUpdates();
		fireTableStructureChanged();
	}

	private void initColumns() {
		columnNames = tableProvider.getColumnNames();
		Logging.info(this, "initColumns " + columnNames);

		classNames = tableProvider.getClassNames();
		Logging.info(this, "initColumns  classNames " + classNames);

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

	public List<String> getClassNames() {
		classNames = tableProvider.getClassNames();
		return classNames;
	}

	public int getKeyCol() {
		return keyCol;
	}

	public void setKeyCol(int keyCol) {
		this.keyCol = keyCol;
	}

	public List<Integer> getFinalCols() {
		return finalCols;
	}

	public void invalidate() {
		// is needed
		tableProvider.requestReturnToOriginal();
		modelDataValid = false;
		requestRefreshDerivedMaps();
	}

	public boolean isReloadRequested() {
		return !modelDataValid;
	}

	public void requestReload() {
		modelDataValid = false;
		requestRefreshDerivedMaps();
		tableProvider.requestReloadRows();
	}

	public void structureChanged() {
		tableProvider.structureChanged();
		modelStructureValid = false;
		requestReload();
	}

	public void startWithCurrentData() {
		tableProvider.setWorkingCopyAsNewOriginalRows();
		invalidate();
	}

	public void removeUpdates() {
		int newSize = updates.size();
		for (int i = newSize - 1; i >= saveUpdatesSize; i--) {

			updates.remove(i);
		}
		updatedRows.clear();
		invalidate();
	}

	private void setFilter(TableModelFilter filter) {
		Logging.info(this, "setFilter " + filter);
		workingFilter = filter;
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

	/**
	 * tell if some filter is working
	 */
	public boolean isFiltered() {
		return (workingFilter != null && workingFilter.isInUse());
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

			if (!modelStructureValid) {
				initColumns();
				fireTableStructureChanged();
				modelStructureValid = true;
			}

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

		if (chainedFilter == null) {
			Logging.info(this, "isUsingFilter, chainedFilter == null");
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

	public void toggleFilter(String name) {
		setUsingFilter(name, !isUsingFilter(name));
	}

	private void clearUpdates() {
		addedRows.clear();
		updatedRows.clear();
		if (updates == null) {
			saveUpdatesSize = 0;
		} else {
			saveUpdatesSize = updates.size();
		}
	}

	/**
	 * sets data to the source values (if model is not valid they are
	 * recollected) clears update collection
	 */
	public void reset() {
		Logging.info(this, "reset()");
		requestRefreshDerivedMaps();
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

		for (int j = 0; j < editable.length; j++) {

			colEditable[editable[j]] = true;
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

			result = (rows.get(row)).get(col);
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

	public List<Object> getRow(int row) {
		return rows.get(row);
	}

	public RowStringMap getRowStringMap(int row) {
		RowStringMap result = new RowStringMap();

		for (int col = 0; col < getColumnNames().size(); col++) {
			result.put(getColumnName(col), "" + getValueAt(row, col));
		}

		return result;
	}

	public RowMap<String, Object> getRowMap(int row) {
		RowMap<String, Object> result = new RowMap<>();

		for (int col = 0; col < getColumnNames().size(); col++) {
			Object value = getValueAt(row, col);
			if (value == null) {
				value = "";
			} else {
				value = "" + value;
			}

			result.put(getColumnName(col), value);
		}

		return result;
	}

	public List<Object> getColumn(int col) {
		List<Object> result = new ArrayList<>();
		for (int row = 0; row < rowsLength; row++) {
			result.add(getValueAt(row, col));
		}

		return result;
	}

	public List<String> getOrderedColumn(int col) {
		TreeSet<String> set = new TreeSet<>();
		for (int row = 0; row < rowsLength; row++) {
			set.add((String) getValueAt(row, col));
		}

		return new ArrayList<>(set);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (addedRows.indexOf(row) == -1 && finalCols.indexOf(col) > -1) {
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
	public void setValueAt(Object value, int row, int col) {
		// wenn row hinzugefügte zeile ist:

		//
		// insertRow aufrufen

		// sonst:

		String oldValueString = "" + (rows.get(row)).get(col);

		Logging.debug(this, " old value at " + row + ", " + col + " : " + oldValueString);
		Logging.debug(this, " new value at " + row + ", " + col + " : " + value);

		String newValueString = "" + value;

		boolean valueChanged = false;

		if (((rows.get(row)).get(col) == null && (value == null || "".equals(value)))
				|| (oldValueString.equals(newValueString))) {
			valueChanged = false;
		} else {
			valueChanged = true;
		}

		if (valueChanged && markCursorRow && col == colMarkCursorRow) {
			valueChanged = false;
		}

		if (valueChanged) {
			// we dont register updates for already registered rows, since there values are
			// passed via the row List
			if (addedRows.indexOf(row) == -1 && updatedRows.indexOf(row) == -1) {
				List<Object> oldValues = new ArrayList<>(rows.get(row));

				rows.get(row).set(col, value);

				if (itemFactory == null) {
					Logging.info("update item factory missing");
				} else if (updates == null) {
					Logging.info("updates not initialized");
				} else {

					updates.add(itemFactory.produceUpdateItem(oldValues, rows.get(row)));
				}

				Logging.debug(this, "updated rows add " + row);

				updatedRows.add(row);
			}

			// we cannot edit a key column after it is saved in the data backend
			if (addedRows.indexOf(row) == -1 && finalCols.indexOf(col) > -1) {
				// we should not get any more to this code, since for this condition the value
				// is marked as not editable
				Logging.warning("key column cannot be edited after saving the data");

				JOptionPane.showMessageDialog(null, "values in this column are fixed after saving the data",
						"Information", JOptionPane.OK_OPTION);

				return;
			}

			// in case of an updated row we did this already
			rows.get(row).set(col, value);
			fireTableCellUpdated(row, col);

			requestRefreshDerivedMaps();
		}
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

	public void setRow(int row, Object[] a) {
		int col = 0;
		if (colsLength != a.length) {
			Logging.info("update row values less than than row elements");
		}

		while (col < colsLength && col < a.length) {
			setValueAt(a[col], row, col);
			col++;
		}
	}

	public void addRow(List<Object> rowV) {
		Logging.debug(this, "--- addRow size, row " + rowV.size() + ", " + rowV);

		rows.add(rowV);
		addedRows.add(rowsLength);

		updates.add(itemFactory.produceInsertItem(rowV));

		// we shall have to reload the data if keys are newly generated
		requestReload();

		rowsLength++;
		try {
			fireTableRowsInserted(rowsLength - 1, rowsLength - 1);
		} catch (Exception ex) {
			Logging.warning(this, "addRow exception " + ex + " row " + rowV, ex);
		}

		requestRefreshDerivedMaps();
	}

	public void addRow(Object[] a) {

		List<Object> rowV = new ArrayList<>();
		for (int i = 0; i < colsLength; i++) {
			rowV.add(null);
		}
		for (int j = 0; j < a.length; j++) {

			rowV.set(j, a[j]);
		}

		addRow(rowV);
	}

	public List<Object> produceValueRowFromSomeEntries(RowMap entries) {
		Logging.debug(this, "produceValueRowFromSomeEntries " + entries);

		List<Object> result = new ArrayList<>();

		for (String col : columnNames) {

			if (entries.get(col) != null) {
				result.add(entries.get(col));
			} else {
				result.add(null);
			}
		}

		return result;
	}

	private boolean checkDeletionOfAddedRow(int rowNum) {
		if (addedRows.indexOf(rowNum) > -1) {

			// deletion of added rows is not adequately managed
			// therefore we reject it

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

		for (int i = 0; i < selection.length; i++) {
			if (!checkDeletionOfAddedRow(selection[i])) {
				return;
			}
		}

		// do with original row nums
		for (int i = 0; i < selection.length; i++) {
			List<Object> oldValues = new ArrayList<>(rows.get(selection[i]));
			Logging.debug(this, "deleteRow values " + oldValues);
			updates.add(itemFactory.produceDeleteItem(oldValues));

			if (updatedRows.contains(selection[i])) {
				Logging.debug(this, "deleteRows, remove from updatedRows  " + selection[i]);
				updatedRows.remove((Integer) selection[i]);
			}
		}

		// adapt the model from the upper index downto the lower index (selection has
		// been sorted)
		for (int i = selection.length - 1; i >= 0; i--) {
			rows.remove(selection[i]);
			rowsLength--;
			fireTableRowsDeleted(selection[i], selection[i]);
		}

		requestRefreshDerivedMaps();
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

		if (updatedRows.indexOf(rowNum) > -1) {
			Logging.debug(this, "deleteRow, remove from updatedRows  " + updatedRows.indexOf(rowNum));
			updatedRows.remove(updatedRows.indexOf(rowNum));
		}

		rows.remove(rowNum);
		rowsLength--;
		fireTableRowsDeleted(rowNum, rowNum);

		requestRefreshDerivedMaps();
		Logging.debug(this, "deleted row " + oldValues);
	}

	public boolean isRowAdded(int rowNum) {
		return (addedRows.indexOf(rowNum) > -1);
	}

	public boolean someRowAdded(int[] rowNums) {
		boolean result = false;
		if (rowNums != null) {
			int i = 0;
			while (!result && i < rowNums.length) {
				if (addedRows.indexOf(rowNums[i]) > -1) {
					result = true;
				}

				i++;
			}
		}
		return result;
	}

	private Map<Object, List<Object>> buildFunction(int col1, int col2,
			TableModelFilterCondition specialFilterCondition) {
		Map<Object, List<Object>> result = new HashMap<>();

		boolean saveUsingFilter = workingFilter != null && workingFilter.isInUse();

		if (specialFilterCondition != null) {
			// activate special filter and reproduce rows
			TableModelFilter filter = new TableModelFilter(specialFilterCondition);
			Logging.info(this, "buildFunction with filter " + filter);
			setFilter(filter);
			produceRows();
		} else if (saveUsingFilter) {
			// we filtered but do not want to do it now

			// turn off filter and reproduce rows
			setFilter(emptyFilter);
			produceRows();
		}

		for (int row = 0; row < getRowCount(); row++) {
			Object val1 = getValueAt(row, col1);

			List<Object> associatedValues = result.computeIfAbsent(val1, arg -> new ArrayList<>());

			Object val2 = getValueAt(row, col2);
			if (associatedValues.indexOf(val2) == -1) {
				associatedValues.add(val2);
			}
		}

		setFilter(chainedFilter);

		if (specialFilterCondition != null || saveUsingFilter) {
			// we changed filtering and have to reproduce the rows
			produceRows();
		}

		return result;
	}

	public boolean existsStringValueInCol(String value, int col) {
		boolean found = false;
		int i = 0;
		while (!found && i < getRowCount()) {
			String compValue = "" + getValueAt(i, col);

			if (compValue.equals(value)) {
				found = true;
			} else {
				i++;
			}

		}
		return found;
	}

	public boolean existsValueInCol(Object value, int col) {
		boolean found = false;
		int i = 0;
		while (!found && i < getRowCount()) {
			Object compValue = getValueAt(i, col);

			if (compValue.equals(value)) {
				found = true;
			} else {
				i++;
			}
		}
		return found;
	}

	// interface TableModelFunctions

	private void requestRefreshDerivedMaps() {
		primarykey2Rowmap = null;
		primarykeyTranslation = null;
		primarykeyRepresentation = null;
	}

	@Override
	public Map<Object, List<Object>> getFunction(int col1, int col2) {
		return getFunction(col1, col2, null);
	}

	public Map<Object, List<Object>> getFunction(int col1, int col2, TableModelFilterCondition specialFilterCondition) {
		TableModelFunctions.PairOfInt pair = new TableModelFunctions.PairOfInt(col1, col2);

		return buildFunction(pair.col1, pair.col2, specialFilterCondition);
	}

	@Override
	public Map<Integer, RowStringMap> getPrimarykey2Rowmap() {
		if (keyCol < 0) {
			return new HashMap<>();
		}

		if (primarykey2Rowmap != null) {
			return primarykey2Rowmap;
		}

		primarykey2Rowmap = new HashMap<>();

		for (int i = 0; i < rows.size(); i++) {
			Integer key = Integer.valueOf((String) getValueAt(i, keyCol));
			primarykey2Rowmap.put(key, getRowStringMap(i));
		}

		return primarykey2Rowmap;
	}

	@Override
	public Map<Integer, Mapping<Integer, String>> getID2Mapping(int col1st, int col2nd, Mapping col2ndMapping) {

		Map<Object, List<Object>> function = getFunction(col1st, col2nd);

		if (function == null) {
			return new HashMap<>();
		}
		Map<Integer, Mapping<Integer, String>> xFunction = new HashMap<>();

		for (Entry<Object, List<Object>> functionEntry : function.entrySet()) {
			Integer keyVal = (Integer) functionEntry.getKey();
			xFunction.put(keyVal, col2ndMapping.restrictedTo(new HashSet<>(functionEntry.getValue())));
		}

		return xFunction;
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
