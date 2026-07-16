/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.ListSelectionList;
import de.uib.configed.gui.features.searchpane.SearchCriteriaEngine;
import de.uib.configed.share.logging.Logging;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public class SearchTargetModelFromJList extends SearchTargetModelFromTable {
	private JList<String> jList;

	private AbstractTableModel tableModel;
	private List<String> theValues;
	private List<String> theDescriptions;
	private List<String> unfilteredV;
	private List<String> unfilteredD;
	private int[] unfilteredSelection;

	private FilterContext filterContext;

	@Data
	@NoArgsConstructor
	public static class FilterContext {
		private String query;
		private int column;
		@Accessors(fluent = true)
		private boolean useRegex;
		@Accessors(fluent = true)
		private boolean caseSensitive;
	}

	public SearchTargetModelFromJList(JList<String> jList, final List<String> values, final List<String> descriptions) {
		this(jList, values, descriptions, null);
	}

	public SearchTargetModelFromJList(JList<String> jList, final List<String> values, final List<String> descriptions,
			FilterContext filterContext) {
		this.jList = jList;
		unfilteredV = values;
		unfilteredD = descriptions;
		unfilteredSelection = null;

		if (values == null || descriptions == null || values.size() != descriptions.size()) {
			Logging.error("missing data for List");
			theValues = new ArrayList<>();
			theDescriptions = new ArrayList<>();
			unfilteredV = new ArrayList<>();
			unfilteredD = new ArrayList<>();
		} else {
			theValues = new ArrayList<>(values);
			theDescriptions = new ArrayList<>(descriptions);
		}

		tableModel = setupTableModel(theValues, theDescriptions);

		super.setTable(new JTable(tableModel));

		if (filterContext != null) {
			reapplyFilter(filterContext);
		}
	}

	@SuppressWarnings({ "java:S1188" })
	private static AbstractTableModel setupTableModel(List<String> values, List<String> descriptions) {
		String[] columnNames = new String[] { Configed.getResourceValue("SearchTargetModelFromJList.columnName"),
				Configed.getResourceValue("description") };

		return new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return values.size();
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int col) {
				return columnNames[col];
			}

			@Override
			public Object getValueAt(int row, int col) {
				return col == 0 ? values.get(row) : descriptions.get(row);
			}
		};
	}

	@Override
	public int getColForVisualCol(int visualCol) {
		return visualCol;
	}

	@Override
	public int getRowForVisualRow(int visualRow) {
		return visualRow;
	}

	@Override
	public void clearSelection() {
		Logging.info(this, "clearSelection");
		jList.clearSelection();
	}

	@Override
	public int getSelectedRow() {
		return getSelectedRows().length > 0 ? getSelectedRows()[0] : -1;
	}

	@Override
	public int[] getSelectedRows() {
		Set<Integer> selection = new TreeSet<>();
		for (int j = 0; j < theValues.size(); j++) {
			if (jList.isSelectedIndex(j)) {
				selection.add(j);
			}
		}

		int[] result = new int[selection.size()];
		int i = 0;
		for (Integer j : selection) {
			result[i] = j;
			i++;
		}
		return result;
	}

	@Override
	public void ensureRowIsVisible(int row) {
		jList.ensureIndexIsVisible(jList.getSelectedIndex());
	}

	@Override
	public void setCursorRow(int row) {
		/* Should do nothing in this class */}

	@Override
	public void setSelectedRow(int row) {
		if (row == -1) {
			clearSelection();
			return;
		}

		jList.setSelectedIndex(row);
		ensureRowIsVisible(row);
	}

	@Override
	public void addSelectedRow(int row) {
		Logging.info(this, "addSelectedRow ", row);

		jList.addSelectionInterval(row, row);

		ensureRowIsVisible(row);
	}

	@Override
	public int[] getUnfilteredSelection() {
		if (unfilteredV == null || unfilteredSelection == null) {
			return new int[0];
		}

		return unfilteredSelection;
	}

	@Override
	public void setSelection(int[] selection) {
		setValueIsAdjusting(true);
		jList.clearSelection();

		for (int i : selection) {
			if (i > tableModel.getRowCount() - 1) {
				Logging.warning(this, "tableModel has index (as should be set from selection) ", i);
			} else {
				jList.addSelectionInterval(i, i);
			}
		}

		setValueIsAdjusting(false);
		if (selection.length > 0) {
			jList.ensureIndexIsVisible(selection[0]);
		}
	}

	@Override
	public void setValueIsAdjusting(boolean b) {
		jList.getSelectionModel().setValueIsAdjusting(b);
	}

	@Override
	public void setFiltered(boolean filtered) {
		Logging.info(this, "setFiltered ", filtered);

		if (filtered) {
			unfilteredSelection = jList.getSelectedIndices();
			theValues = new ArrayList<>();
			theDescriptions = new ArrayList<>();
			for (Integer i : jList.getSelectedIndices()) {
				theValues.add(unfilteredV.get(i));
				theDescriptions.add(unfilteredD.get(i));
			}
		} else {
			theValues = unfilteredV;
			theDescriptions = unfilteredD;
		}

		tableModel = setupTableModel(theValues, theDescriptions);
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
		tableModel.fireTableStructureChanged();

		jList.setListData(theValues.toArray(new String[0]));

		if (filtered) {
			// we mark all since we just filtered the marked ones

			// selectAll : (since it is assumed that we filter the selected)
			setValueIsAdjusting(true);
			jList.setSelectionInterval(0, jList.getModel().getSize() - 1);
			setValueIsAdjusting(false);
		} else {
			jList.setSelectionInterval(0, 0);
		}

		Logging.info(this, "setFilter ", theValues);
	}

	@Override
	public void applyFilter(String query, int column, boolean useRegex, boolean caseSensitive) {
		List<JListItemWrapper> allItems = new ArrayList<>();
		for (int i = 0; i < unfilteredV.size(); i++) {
			allItems.add(new JListItemWrapper(unfilteredV.get(i), unfilteredD.get(i)));
		}

		List<JListItemWrapper> filteredItems = filter(allItems, query, column, useRegex, caseSensitive);

		theValues = filteredItems.stream().map(JListItemWrapper::getValue).collect(Collectors.toList());
		theDescriptions = filteredItems.stream().map(JListItemWrapper::getDescription).collect(Collectors.toList());

		updateUI();
	}

	private static class JListItemWrapper {
		private final String val;
		private final String desc;

		public JListItemWrapper(String v, String d) {
			this.val = v;
			this.desc = d;
		}

		public Object getValue(int col) {
			return col == 0 ? val : desc;
		}

		public String getValue() {
			return val;
		}

		public String getDescription() {
			return desc;
		}
	}

	public <T extends JListItemWrapper> List<T> filter(List<T> allItems, String query, int columnIndex,
			boolean useRegex, boolean caseSensitive) {
		if (query == null || query.isEmpty() || allItems.isEmpty()) {
			return new ArrayList<>(allItems);
		}

		SearchCriteriaEngine searchCriteriaEngine = new SearchCriteriaEngine();

		Pattern pattern = searchCriteriaEngine.getPattern(useRegex, caseSensitive, query);

		List<T> result = new ArrayList<>();

		for (T item : allItems) {
			if (searchCriteriaEngine.matchCell(item.getValue(columnIndex), query, pattern, useRegex, caseSensitive)) {
				result.add(item);
			}
		}

		return result;
	}

	private void reapplyFilter(FilterContext filterContext) {
		applyFilter(filterContext.getQuery(), filterContext.getColumn(), filterContext.useRegex(),
				filterContext.caseSensitive());
	}

	public FilterContext getFilterContext() {
		return filterContext;
	}

	private void updateUI() {
		tableModel = setupTableModel(theValues, theDescriptions);
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
		tableModel.fireTableStructureChanged();

		List<String> selectedValues = jList.getSelectedValuesList();
		jList.setListData(theValues.toArray(new String[0]));
		if (!theValues.isEmpty()) {
			if (!selectedValues.isEmpty()) {
				switch (jList) {
				case ListSelectionList s -> s.setPreviousSelectionValues(selectedValues);
				case DepotsList l -> l.setSelectedValues(selectedValues);
				default -> Logging.debug(this, "caught unhandled type list", jList);
				}
			} else {
				jList.setSelectedIndex(0);
			}
		} else {
			jList.clearSelection();
		}
	}
}
