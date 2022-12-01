/*
 * SearchTargetModelFromJList.java
 *
 * By uib, www.uib.de, 2017
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.uib.utilities.logging.logging;

public class SearchTargetModelFromJList extends SearchTargetModelFromTable {

	protected JList<String> jList;

	protected AbstractTableModel tableModel;
	protected Vector<String> theValues;
	protected Vector<String> theDescriptions;
	private Vector<String> unfilteredV;
	private Vector<String> unfilteredD;
	protected int[] unfilteredSelection;

	public SearchTargetModelFromJList(JList<String> jList, final Vector<String> values,
			final Vector<String> descriptions) {

		this.jList = jList;
		unfilteredV = values;
		unfilteredD = descriptions;
		unfilteredSelection = null;

		if (values == null || descriptions == null || values.size() != descriptions.size()) {
			logging.error("missing data for List");
			theValues = new Vector<String>();
			theDescriptions = new Vector<String>();
			unfilteredV = new Vector<String>();
			unfilteredD = new Vector<String>();
		} else {
			theValues = new Vector<String>(values);
			theDescriptions = new Vector<String>(descriptions);
		}

		tableModel = setupTableModel(theValues, theDescriptions);

		super.setTable(new JTable(tableModel));
	}

	protected AbstractTableModel setupTableModel(Vector<String> values, Vector<String> descriptions) {

		AbstractTableModel tableModel = new AbstractTableModel() {
			public int getRowCount() {
				return values.size();
			}

			public int getColumnCount() {
				return 2;
			}

			public Object getValueAt(int row, int col) {
				if (col == 0)
					return values.get(row);
				else
					return "" + descriptions.get(row);
			}
		};

		return tableModel;
	}

	public int getColForVisualCol(int visualCol) {
		return visualCol;
	}

	public int getRowForVisualRow(int visualRow) {
		return visualRow;
	}

	public void clearSelection() {
		logging.info(this, "clearSelection");
		jList.clearSelection();
	}

	public int getSelectedRow() {
		if (getSelectedRows().length == 0)
			return -1;
		return getSelectedRows()[0];
	}

	public int[] getSelectedRows() {
		TreeSet<Integer> selection = new TreeSet<Integer>();
		for (int j = 0; j < theValues.size(); j++) {
			if (jList.isSelectedIndex(j))
				selection.add(j);
		}

		int[] result = new int[selection.size()];
		int i = 0;
		for (Integer j : selection) {
			result[i] = j;
			i++;
		}
		return result;
	}

	public void ensureRowIsVisible(int row) {
		// jList.locationToIndex
		// table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}

	public void setCursorRow(int row) {
	}

	public void setSelectedRow(int row) {
		if (row == -1) {
			clearSelection();
			return;
		}

		jList.setSelectedIndex(row);
		ensureRowIsVisible(row);
	}

	public void addSelectedRow(int row) {
		logging.info(this, "addSelectedRow " + row);

		jList.addSelectionInterval(row, row);
		// jList.addSelectionInterval(row +2 , row + 2); //test
		// logging.debug(" --- view row selected " + row);
		ensureRowIsVisible(row);
	}

	public int[] getUnfilteredSelection() {
		if (unfilteredV == null || unfilteredSelection == null)
			return null;

		return unfilteredSelection;
	}

	public void setSelection(int[] selection) {
		setValueIsAdjusting(true);
		jList.clearSelection();

		for (int i : selection) {
			if (i > tableModel.getRowCount() - 1) {
				logging.warning(this, "tableModel has index (as should be set from selection) " + i);
			} else
				jList.addSelectionInterval(i, i);

		}

		setValueIsAdjusting(false);
		if (selection.length > 0)
			jList.ensureIndexIsVisible(selection[0]);
	}

	public void setValueIsAdjusting(boolean b) {
		jList.getSelectionModel().setValueIsAdjusting(b);
	}

	boolean filtered = false;

	@Override
	public void setFiltered(boolean b) {
		logging.info(this, "setFiltered " + b + " it was filtered " + filtered);

		if (b == filtered)
			return;

		if (b) // && filtering)
		{
			unfilteredSelection = jList.getSelectedIndices();
			theValues = new Vector<String>();
			theDescriptions = new Vector<String>();
			for (Integer i : jList.getSelectedIndices()) {
				theValues.add(unfilteredV.get(i));
				theDescriptions.add(unfilteredD.get(i));
			}
			filtered = true;
		} else {
			theValues = unfilteredV;
			theDescriptions = unfilteredD;
			filtered = false;
		}

		tableModel = setupTableModel(theValues, theDescriptions);
		tableModel.fireTableChanged(new javax.swing.event.TableModelEvent(tableModel));
		tableModel.fireTableStructureChanged();

		jList.setListData(theValues);
		try {
			if (filtered)
			// we mark all since we just filtered the marked ones
			{
				// selectAll : (since it is assumed that we filter the selected)
				setValueIsAdjusting(true);
				jList.setSelectionInterval(0, jList.getModel().getSize() - 1);
				setValueIsAdjusting(false);
			} else
				jList.setSelectionInterval(0, 0);
		} catch (Exception ex) {
			logging.warning(this, "selection error " + ex);
		}

		logging.info(this, "setFilter " + theValues);
	}

	@Override
	public boolean isFiltered() {
		return filtered;
	}

}
