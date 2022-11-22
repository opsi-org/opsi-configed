/*
 * SearchTargetModelFromTable.java
 *
 * By uib, www.uib.de, 2017
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;

public class SearchTargetModelFromTable implements SearchTargetModel {
	protected JTable table;
	protected PanelGenEditTable thePanel; // in case that we are working in our standard context

	public static final String FILTER_BY_SELECTION = "filterBySelection";

	protected int[] viewRowfilter = new int[0];

	public SearchTargetModelFromTable() {
		this((JTable) null);
	}

	public SearchTargetModelFromTable(JTable table) {
		setTable(table);
	}

	public SearchTargetModelFromTable(PanelGenEditTable thePanel) {
		setTable(thePanel.getTheTable());
		this.thePanel = thePanel;
	}

	public void setTable(JTable table) {
		this.table = table;
		logging.info(this, "setTable null? " + (table == null));

	}

	protected AbstractTableModel getTableModel() {
		return (AbstractTableModel) table.getModel();
	}

	public String getColumnName(int col) {
		return getTableModel().getColumnName(col);
	}

	public int findColumn(String name) {
		return getTableModel().findColumn(name);
	}

	public int getColumnCount() {
		// logging.info(this, "getColumnCount retrieves model " + getTableModel());
		// if (getTableModel() == null)
		// return 0;

		return getTableModel().getColumnCount();
	}

	public int getRowCount() {
		return getTableModel().getRowCount();
	}

	public Object getValueAt(int row, int col) {
		return getTableModel().getValueAt(row, col);
	}

	public int getColForVisualCol(int visualCol) {
		return table.convertColumnIndexToModel(visualCol);
	}

	public int getRowForVisualRow(int visualRow) {
		return table.convertRowIndexToModel(visualRow);
	}

	public void setRenderAsCurrentRow(int row) {
		/*
		 * logging.info(this, "setRenderAsCurrentRow " + row);
		 * 
		 * for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
		 * {
		 * logging.info(this, "setRenderAsCurrentRow looking for col " + i);
		 * TableColumn col = table.getColumnModel().getColumn(i);
		 * logging.info(this, "setRenderAsCurrentRow col cell renderer is " +
		 * col.getCellRenderer() );
		 * 
		 * if (
		 * col.getCellRenderer() instanceof StandardTableCellRenderer
		 * //||
		 * //col.getCellRenderer() instanceof TableCellRendererConfigured
		 * )
		 * {
		 * ((StandardTableCellRenderer)(col.getCellRenderer())).setCurrentRow ( row );
		 * logging.info(this, "setRenderAsCurrentRow setting for col " + i);
		 * }
		 * }
		 */
	}

	public void clearSelection() {
		table.clearSelection();
	}

	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public int[] getSelectedRows() {
		return table.getSelectedRows();
	}

	public void ensureRowIsVisible(int row) {
		// int viewrow = table.convertRowIndexToView(row);
		// int modelrow = table.convertRowIndexToModel(row);
		table.scrollRectToVisible(table.getCellRect(row, 0, false));

		// setCursorRow( row );
	}

	public void setCursorRow(int row) {
		// int viewrow = table.convertRowIndexToView(row);
		if (table.getRowCount() <= 0) {
			return;
		}

		int modelrow = table.convertRowIndexToModel(row);

		logging.info(this, "setCursorRow row, produced modelrow " + modelrow);

		if (table.getModel() instanceof de.uib.utilities.table.GenTableModel) {
			// int row = table.convertRowIndexToModel( viewrow );
			((de.uib.utilities.table.GenTableModel) table.getModel()).setCursorRow(modelrow);
		}

	}

	public void setSelectedRow(int row) {
		if (table.getRowCount() == 0)
			return;

		if (row == -1) {
			table.clearSelection();
			return;
		}

		table.setRowSelectionInterval(row, row);
		// System.out.println(" --- view row selected " + row);
		ensureRowIsVisible(row);
	}

	public void addSelectedRow(int row) {
		logging.debug(this, "addSelectedRow " + row);

		if (table.getRowCount() == 0)
			return;

		table.addRowSelectionInterval(row, row);
		// System.out.println(" --- view row selected " + row);
		ensureRowIsVisible(row);
	}

	public int[] getUnfilteredSelection() {
		return viewRowfilter;
	}

	public void setValueIsAdjusting(boolean b) {
		table.getSelectionModel().setValueIsAdjusting(b);
	}

	public void setSelection(int[] selection) {
		logging.info(this, "setSelection --- " + java.util.Arrays.toString(selection));
		table.getSelectionModel().clearSelection();
		for (int i = 0; i < selection.length; i++) {
			table.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}

	private void returnToNotChanged(boolean wasChanged) {
		// we are not interested in changes of model induced by selection
		if (thePanel != null
				&& !wasChanged
				&& thePanel.isDataChanged()) {
			logging.info(this, "returnToNotChanged active ");
			thePanel.setDataChanged(false);
		}
	}

	protected boolean filtered = false;

	@Override
	public void setFiltered(boolean b) {
		// logging.info(this, "setFiltered " + b + " it was filtered " + filtered);

		// if (b == filtered)
		// return;

		boolean wasChanged = false;

		if (thePanel != null)
			wasChanged = thePanel.isDataChanged();

		GenTableModel model = (GenTableModel) (table.getModel());

		if (!filtered)
			viewRowfilter = table.getSelectedRows();

		// logging.info(this, "setFiltered " + b + " viewRowfilter "
		// + java.util.Arrays.toString( viewRowfilter ));

		if (b && viewRowfilter.length > 0) {
			int[] modelRowFilter = new int[viewRowfilter.length];
			for (int i = 0; i < viewRowfilter.length; i++) {
				modelRowFilter[i] = table.convertRowIndexToModel(viewRowfilter[i]);
			}

			logging.info(this, "setFiltered modelRowFilter " + java.util.Arrays.toString(modelRowFilter));

			((de.uib.utilities.table.RowNoTableModelFilterCondition) (model.getFilter(FILTER_BY_SELECTION)
					.getCondition()))
					.setFilter(modelRowFilter, model.getRows());

			model.setUsingFilter(FILTER_BY_SELECTION, true);
			model.reset();

			// setSelection( viewRowfilter );
			table.getSelectionModel().setSelectionInterval(0, model.getRowCount());

		} else {
			model.setUsingFilter(FILTER_BY_SELECTION, false);
			// ((AbstractTableModel) table.getModel()).fireTableDataChanged();
			setSelection(viewRowfilter); // restore the original selection
		}
		filtered = b;

		returnToNotChanged(wasChanged);
	}

	@Override
	public boolean isFiltered() {
		return filtered;

		// ((de.uib.utilities.table.GenTableModel) table.getModel()).isFiltered(); does
		// not work since we don't always have got a GenTableModel
	}

	@Override
	public int getListSelectionMode() {
		return table.getSelectionModel().getSelectionMode();
	}

}
