/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedUtilityMethods;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.table.CursorrowObserver;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.RowNoTableModelFilterCondition;
import de.uib.configed.gui.share.table.TableModelFilter;
import de.uib.configed.gui.share.table.updates.UpdateController;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelGenEdit extends JPanel implements TableModelListener, ListSelectionListener, CursorrowObserver {
	private JScrollPane jScrollPane;
	protected GenEditTable genEditTable;

	private JButton buttonCommit;
	private JButton buttonCancel;
	private JLabel jLabelTitle;

	private boolean dataChanged;

	private UpdateController updateController;

	private boolean editing = true;

	private boolean awareOfSelectionListener;
	private boolean awareOfTableChangedListener = true;

	private boolean withTablesearchPane;

	protected TableSearchPane tableSearchPane;

	private String title = "";

	private PanelGenEditPopupManager popupManager;

	private int oldrowcount = -1;

	private FilterKey filterKey;

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition, int[] popupsWanted,
			boolean withTablesearchPane) {
		this.withTablesearchPane = withTablesearchPane;

		popupManager = new PanelGenEditPopupManager(this, generalPopupPosition, popupsWanted);

		if (title != null) {
			this.title = title;
		}

		this.editing = editing;

		if (!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
				.hasServerFullPermissionPD()) {
			this.editing = false;
		}

		initComponents();
		popupManager.addPopupmenuStandardpart();
	}

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition, int[] popupsWanted) {
		this(title, editing, generalPopupPosition, popupsWanted, false);
	}

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition) {
		this(title, editing, generalPopupPosition, null);
	}

	public void setFilterKey(FilterKey filterKey) {
		this.filterKey = filterKey;
		tableSearchPane.setFilterKey(filterKey);
	}

	@Override
	public void requestFocus() {
		genEditTable.requestFocus();

		if (withTablesearchPane) {
			tableSearchPane.requestFocus();
		}
	}

	public void setUpdateController(UpdateController updateController) {
		this.updateController = updateController;
	}

	public void addListSelectionListener(ListSelectionListener l) {
		genEditTable.getSelectionModel().addListSelectionListener(l);
	}

	private void initComponents() {
		jLabelTitle = new JLabel(title);

		if (title == null || title.isEmpty()) {
			jLabelTitle.setVisible(false);
		}

		genEditTable = new GenEditTable();

		tableSearchPane = new TableSearchPane(this);
		tableSearchPane.setVisible(withTablesearchPane);

		// we prefer the simple behaviour:

		genEditTable.getSelectionModel().addListSelectionListener(this);

		jScrollPane = new JScrollPane();
		jScrollPane.setViewportView(genEditTable);

		JPanel controlPanel = initControlPanel();

		setLayout(new MigLayout("insets 0, wrap 1", "[grow,fill]", "[]0"));

		add(jLabelTitle, "gapbottom " + Globals.MIN_GAP_SIZE);
		add(tableSearchPane, "hidemode 3, gapbottom " + Globals.MIN_GAP_SIZE);
		add(jScrollPane, "grow, push");
		add(controlPanel, "hidemode 3, gaptop " + Globals.MIN_GAP_SIZE);
	}

	private JPanel initControlPanel() {
		JPanel controlPanel = new JPanel();

		if (!editing) {
			controlPanel.setVisible(false);
			return controlPanel;
		}

		buttonCommit = new JButton(Icons.getIntellijIcon("checkmark"));
		buttonCommit.setToolTipText(Configed.getResourceValue("save"));
		buttonCommit.addActionListener(action -> commit());

		buttonCancel = new JButton(Icons.getIntellijIcon("close"));
		buttonCancel.setToolTipText(Configed.getResourceValue("buttonClose"));
		buttonCancel.addActionListener(action -> cancel());

		controlPanel.setLayout(new MigLayout("insets 0", "[][" + Globals.GAP_SIZE + "][]", "[]0"));

		controlPanel.add(buttonCancel);
		controlPanel.add(buttonCommit);

		setDataChanged(false);

		return controlPanel;
	}

	public void setDeleteAllowed(boolean deleteAllowed) {
		genEditTable.setDeleteAllowed(deleteAllowed);
	}

	public void reload() {
		getParent().setCursor(Globals.WAIT_CURSOR);

		Logging.info(this, "in PanelGenEditTable reload()");
		int[] columnWidths = ConfigedUtilityMethods.getTableColumnWidths(genEditTable);
		genEditTable.getGenTableModel().requestReload();
		genEditTable.getGenTableModel().reset();
		setDataChanged(false);
		ConfigedUtilityMethods.setTableColumnWidths(genEditTable, columnWidths);

		getParent().setCursor(null);
	}

	public void setTitle(String title) {
		Logging.info(this, "setTitle ", title);
		this.title = title;
		jLabelTitle.setText(title);
	}

	public JScrollPane getTheScrollpane() {
		return jScrollPane;
	}

	public void setSortOrder(Map<Integer, SortOrder> sortDescriptor) {
		genEditTable.setSortDescriptor(sortDescriptor);
	}

	public void setTableModel(GenTableModel m) {
		genEditTable.setRowSorter(null);
		// just in case there was one

		genEditTable.setModel(m);
		genEditTable.getGenTableModel().addCursorrowObserver(this);

		genEditTable.setSorter();

		setDataChanged(false);

		setModelFilteringBySelection();
	}

	/**
	 * set columns for which the searchpane shall work
	 */
	public void setSearchColumns(Integer[] cols) {
		if (!withTablesearchPane) {
			Logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		tableSearchPane.setSearchFields(cols);
	}

	/**
	 * set all columns for column selection in search pane; requires the correct
	 * model is initialized
	 */
	public void setSearchColumnsAll() {
		if (!withTablesearchPane) {
			Logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		tableSearchPane.setSearchFieldsAll();
	}

	private void setModelFilteringBySelection() {
		if (tableSearchPane.isFiltering() && genEditTable.getModel() != null
				&& genEditTable.getGenTableModel().getFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION) == null) {
			RowNoTableModelFilterCondition filterBySelectionCondition = new RowNoTableModelFilterCondition();
			TableModelFilter filterBySelection = new TableModelFilter(filterBySelectionCondition, false, false);

			genEditTable.getGenTableModel().chainFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION,
					filterBySelection);
		}
	}

	public void setDataChanged(boolean dataChanged) {
		if (!editing) {
			return;
		}

		Logging.info(this, "setDataChanged ", dataChanged);
		this.dataChanged = dataChanged;
		buttonCommit.setEnabled(dataChanged);
		buttonCancel.setEnabled(dataChanged);

		popupManager.setDataChanged(dataChanged);
	}

	public boolean isDataChanged() {
		Logging.info(this, "isDataChanged ", dataChanged);
		return dataChanged;
	}

	private void stopCellEditing() {
		if (genEditTable.getCellEditor() != null) {
			// we are editing
			Logging.info(this, "we are editing a cell");
			genEditTable.getCellEditor().stopCellEditing();
		} else {
			Logging.info(this, "no cell editing");
		}
	}

	public void commit() {
		stopCellEditing();

		if (updateController == null) {
			return;
		}

		if (updateController.saveChanges()) {
			setDataChanged(false);
		}
	}

	public void cancel() {
		if (updateController == null) {
			return;
		}

		if (updateController.cancelChanges()) {
			setDataChanged(false);
		}
	}

	public GenEditTable getGenEditTable() {
		return genEditTable;
	}

	public GenTableModel getTableModel() {
		return genEditTable.getGenTableModel();
	}

	public TableSearchPane getTableSearchPane() {
		return tableSearchPane;
	}

	public void setSelectedRow(int row) {
		genEditTable.setSelectedRow(row);
	}

	public void setSelection(int[] selection) {
		Logging.info(this, "setSelection --- ", Arrays.toString(selection));
		genEditTable.getSelectionModel().clearSelection();
		for (int i = 0; i < selection.length; i++) {
			genEditTable.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}

	public int getSelectedRowInModelTerms() {
		return genEditTable.getSelectedRowInModelTerms();
	}

	public String getTitle() {
		return title;
	}

	public void addPopupItem(JMenuItem item) {
		Logging.info(this, "addPopupMenuItem ", item);
		popupManager.addPopupItem(item);
	}

	public void setValueAt(Object value, int row, int col) {
		genEditTable.setValueAt(value, genEditTable.convertRowIndexToModel(row),
				genEditTable.convertColumnIndexToModel(col));
	}

	public Object getValueAt(int row, int col) {
		return genEditTable.getValueAt(row, col);
	}

	public void setAwareOfSelectionListener(boolean awareOfSelectionListener) {
		Logging.debug(this, "setAwareOfSelectionListener  ", awareOfSelectionListener);

		this.awareOfSelectionListener = awareOfSelectionListener;
	}

	public boolean isAwareOfSelectionListener() {
		return awareOfSelectionListener;
	}

	public void setAwareOfTableChangedListener(boolean b) {
		Logging.debug(this, "setAwareOfTableChangedListener  ", b);

		awareOfTableChangedListener = b;
	}

	public boolean isAwareOfTableChangedListener() {
		return awareOfTableChangedListener;
	}

	public List<String> getSelectedKeys() {
		List<String> result = new ArrayList<>();

		if (genEditTable.getGenTableModel().getKeyCol() < 0) {
			return result;
		}

		if (genEditTable.getGenTableModel().isUsingFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION)) {
			for (int i = 0; i < genEditTable.getRowCount(); i++) {
				result.add(genEditTable
						.getValueAt(genEditTable.convertRowIndexToModel(i), genEditTable.getGenTableModel().getKeyCol())
						.toString());
			}
		} else {
			for (int i = 0; i < genEditTable.getSelectedRowCount(); i++) {
				result.add(
						genEditTable.getValueAt(genEditTable.convertRowIndexToModel(genEditTable.getSelectedRows()[i]),
								genEditTable.getGenTableModel().getKeyCol()).toString());
			}
		}

		return result;
	}

	public void setSelectedValues(List<String> values, int col) {
		genEditTable.clearSelection();

		if (values == null || values.isEmpty()) {
			return;
		}

		genEditTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		Iterator<String> iter = values.iterator();

		while (iter.hasNext()) {
			int viewRow = genEditTable.findViewRowFromValue(iter.next(), col);

			genEditTable.getSelectionModel().addSelectionInterval(viewRow, viewRow);
		}
	}

	public int findViewRowFromValue(Object value, int col) {
		return genEditTable.findViewRowFromValue(value, col);
	}

	public boolean moveToValue(String value, int col) {
		return moveToValue(value, col, true);
	}

	public boolean moveToValue(String value, int col, boolean selecting) {
		Logging.info(this, "moveToValue ", value, " col ", col, " selecting ", selecting);
		int viewrow = genEditTable.findViewRowFromValue(value, col);
		if (viewrow > -1) {
			genEditTable.getGenTableModel().setCursorRow(genEditTable.convertRowIndexToModel(viewrow));
		}

		genEditTable.scrollRectToVisible(genEditTable.getCellRect(viewrow, col, false));

		if (viewrow == -1) {
			return false;
		}

		if (selecting) {
			setSelectedRow(viewrow);
		}

		return true;
	}

	public void moveToKeyValue(String keyValue) {
		if (keyValue == null) {
			return;
		}

		if (genEditTable.getGenTableModel().getKeyCol() > -1) {
			moveToValue(keyValue, genEditTable.getGenTableModel().getKeyCol());
		} else {
			boolean found = false;

			// try to use pseudokey
			int viewrow = 0;

			while (viewrow < genEditTable.getRowCount()) {
				String[] partialkeys = new String[genEditTable.getGenTableModel().getFinalCols().size()];

				int j = 0;
				for (Integer col : genEditTable.getGenTableModel().getFinalCols()) {
					partialkeys[j] = genEditTable.getValueAt(genEditTable.convertRowIndexToModel(viewrow), col)
							.toString();
					j++;
				}

				if (keyValue.equals(Utils.pseudokey(partialkeys))) {
					found = true;
					break;
				} else {
					viewrow++;
				}
			}

			if (found) {
				setSelectedRow(viewrow);
			} else {
				// try value for col 0 as target for search
				moveToValue(keyValue, 0);
			}
		}
	}

	public boolean setCursorToFirstRow() {
		if (genEditTable.getRowCount() > 0) {
			genEditTable.getGenTableModel().setCursorRow(genEditTable.convertRowIndexToModel(0));
			genEditTable.scrollRectToVisible(genEditTable.getCellRect(0, 0, true));
		}

		return true;
	}

	public boolean setCursorToLastRow() {
		if (genEditTable.getRowCount() > 0) {
			genEditTable.getGenTableModel()
					.setCursorRow(genEditTable.convertRowIndexToModel(genEditTable.getRowCount() - 1));
			genEditTable.scrollRectToVisible(genEditTable.getCellRect(genEditTable.getRowCount() - 1, 0, true));
		}
		return true;
	}

	public boolean advanceCursor(int d) {
		int viewCursorRow = -1;
		if (genEditTable.getGenTableModel().getCursorRow() > -1) {
			viewCursorRow = genEditTable.convertRowIndexToView(genEditTable.getGenTableModel().getCursorRow());
		}

		Logging.info(this, "advanceCursor from ", viewCursorRow);
		int nextViewCursorRow = viewCursorRow + d;
		Logging.info(this, "advanceCursor to ", nextViewCursorRow);
		if (nextViewCursorRow < genEditTable.getRowCount() && nextViewCursorRow >= 0) {
			genEditTable.getGenTableModel().setCursorRow(genEditTable.convertRowIndexToModel(nextViewCursorRow));
		}

		genEditTable.scrollRectToVisible(genEditTable.getCellRect(nextViewCursorRow, 0, true));

		return true;
	}

	public void moveToLastRow() {
		genEditTable.moveToRow(genEditTable.getRowCount() - 1);
	}

	public void restoreFilter() {
		if (filterKey == null) {
			Logging.warning(this, "Filter key is null");
			return;
		}
		tableSearchPane.restoreFilter();
	}

	// TableModelListener
	@Override
	public void tableChanged(TableModelEvent e) {
		Logging.debug(this, " tableChanged ", "source ", e.getSource(), " col ", e.getColumn());
		if (genEditTable.getGenTableModel() != null) {
			Logging.debug(this, "tableChanged,  whereas tableModel.getColMarkCursorRow() is ",
					genEditTable.getGenTableModel().getColMarkCursorRow());
		}

		if (awareOfTableChangedListener && genEditTable.getGenTableModel() != null
				&& !(genEditTable.getGenTableModel().getColMarkCursorRow() > -1
						&& e.getColumn() == genEditTable.getGenTableModel().getColMarkCursorRow())) {
			Logging.info(this, " tableChanged, datachanged set to true");
			setDataChanged(true);
			if (genEditTable.getGenTableModel() != null && oldrowcount != genEditTable.getRowCount()) {
				oldrowcount = genEditTable.getRowCount();
			}
		}
	}

	//
	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.debug(this, "ListSelectionEvent ", e);
		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		Logging.debug(this, "ListSelectionEvent not more adjusting");

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (awareOfSelectionListener) {
			setDataChanged(true);
		}

		Logging.info(this, "rows selected: ", lsm.getSelectedItemsCount());

		popupManager.enableMenuItemDeleteRelation(!lsm.isSelectionEmpty());
	}

	@Override
	public void rowUpdated(int modelrow) {
		Logging.info(this, " in PanelGenEditTable rowUpdated to modelrow ", modelrow);
	}
}
