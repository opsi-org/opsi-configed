/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import de.uib.configed.Configed;
import de.uib.utils.logging.Logging;

public class SearchTargetModelFromJList extends SearchTargetModelFromTable {
	private JList<String> jList;

	private AbstractTableModel tableModel;
	private List<String> theValues;
	private List<String> theDescriptions;
	private List<String> unfilteredV;
	private List<String> unfilteredD;
	private int[] unfilteredSelection;

	public SearchTargetModelFromJList(JList<String> jList, final List<String> values, final List<String> descriptions) {
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
				if (col == 0) {
					return values.get(row);
				} else {
					return "" + descriptions.get(row);
				}
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
		if (getSelectedRows().length == 0) {
			return -1;
		}
		return getSelectedRows()[0];
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
		theValues.clear();
		theDescriptions.clear();

		if (query == null || query.isEmpty()) {
			restoreUnfiltered();
		} else {
			Pattern pattern = compilePattern(query, useRegex, caseSensitive);
			if (useRegex && pattern == null) {
				return;
			}
			filterValues(query, column, useRegex, caseSensitive, pattern);
		}

		updateUI();
	}

	private void restoreUnfiltered() {
		theValues.addAll(unfilteredV);
		theDescriptions.addAll(unfilteredD);
	}

	private Pattern compilePattern(String query, boolean useRegex, boolean caseSensitive) {
		if (!useRegex) {
			return null;
		}
		try {
			return caseSensitive ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			Logging.warning(this, "Invalid regex in applyFilter", e);
			return null;
		}
	}

	private void filterValues(String query, int column, boolean useRegex, boolean caseSensitive, Pattern pattern) {
		String cmpQuery = caseSensitive ? query : query.toLowerCase(Locale.ROOT);

		for (int i = 0; i < unfilteredV.size(); i++) {
			String value = unfilteredV.get(i);
			String description = unfilteredD.get(i);
			String valueStr = value != null ? value : "";
			String descStr = description != null ? description : "";

			boolean match = useRegex ? matchesRegex(pattern, valueStr, descStr, column)
					: matchesString(cmpQuery, valueStr, descStr, column, caseSensitive);

			if (match) {
				theValues.add(value);
				theDescriptions.add(description);
			}
		}
	}

	private static boolean matchesRegex(Pattern pattern, String valueStr, String descStr, int column) {
		return switch (column) {
		case 0 -> pattern.matcher(valueStr).find();
		case 1 -> pattern.matcher(descStr).find();
		default -> pattern.matcher(valueStr).find() || pattern.matcher(descStr).find();
		};
	}

	private static boolean matchesString(String cmpQuery, String valueStr, String descStr, int column,
			boolean caseSensitive) {
		String cmpValue = caseSensitive ? valueStr : valueStr.toLowerCase(Locale.ROOT);
		String cmpDesc = caseSensitive ? descStr : descStr.toLowerCase(Locale.ROOT);

		return switch (column) {
		case 0 -> cmpValue.contains(cmpQuery);
		case 1 -> cmpDesc.contains(cmpQuery);
		default -> cmpValue.contains(cmpQuery) || cmpDesc.contains(cmpQuery);
		};
	}

	private void updateUI() {
		tableModel = setupTableModel(theValues, theDescriptions);
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
		tableModel.fireTableStructureChanged();

		jList.setListData(theValues.toArray(new String[0]));
		if (!theValues.isEmpty()) {
			jList.setSelectedIndex(0);
		} else {
			jList.clearSelection();
		}
	}
}
