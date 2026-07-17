/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.gui.features.table.GenericTableModel;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.share.PopupMouseListener;

@SuppressWarnings("java:S1200")
public class GenericTable extends JTable {
	private GenericTableViewModel model;
	private boolean isUpdatingProgrammatically;
	private Function<Integer, Boolean> isCellEditable;
	private Consumer<GenericTableViewMsg> dispatch;

	private RowSorterListener rowSorterListener = (RowSorterEvent e) -> {
		if (isUpdatingProgrammatically) {
			return;
		}

		notifyRowSorterChange();
	};

	private Timer columnResizeNotifier = new Timer(500, (ActionEvent e) -> {
		TableColumnModel columnModel = getColumnModel();
		Map<String, Integer> columnWidths = new HashMap<>();

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			columnWidths.put(col.getIdentifier().toString(), col.getWidth());
		}

		dispatch.accept(new GenericTableViewMsg.ResizeColumns(columnWidths));
	});
	private TableColumnModelListener columnModelListener = new TableColumnModelListener() {

		@Override
		public void columnAdded(TableColumnModelEvent e) {
			// Nothing to do.
		}

		@Override
		public void columnMarginChanged(ChangeEvent e) {
			columnResizeNotifier.restart();
		}

		@Override
		public void columnMoved(TableColumnModelEvent e) {
			// Nothing to do.
		}

		@Override
		public void columnRemoved(TableColumnModelEvent e) {
			// Nothing to do.
		}

		@Override
		public void columnSelectionChanged(ListSelectionEvent e) {
			// Nothing to do.
		}
	};

	public GenericTable(GenericTableViewModel model, Consumer<GenericTableViewMsg> dispatch,
			Function<Integer, Boolean> isCellEditable) {
		this.model = model;
		this.dispatch = dispatch;
		this.isCellEditable = isCellEditable;

		super(new GenericTableModel(model, dispatch::accept, isCellEditable), null);

	}

	private void notifyRowSorterChange() {
		List<? extends RowSorter.SortKey> sortKeys = getRowSorter().getSortKeys();

		Map<String, SortOrder> rowSortKeys = new HashMap<>();
		if (sortKeys.isEmpty()) {
			rowSortKeys.put(null, SortOrder.UNSORTED);
		} else {
			for (RowSorter.SortKey key : sortKeys) {
				String columnKey = (String) getColumnModel().getColumn(key.getColumn()).getIdentifier();
				rowSortKeys.put(columnKey, key.getSortOrder());
			}
		}

		dispatch.accept(new GenericTableViewMsg.ChangeSortOrder(rowSortKeys));
	}

	public void initialize() {
		setFillsViewportHeight(model.getTableConfig().isFillViewportHeight());
		setAutoCreateRowSorter(model.getTableConfig().isAutoCreateRowSorter());

		if (model.getTableConfig().getDefauTableCellRenderer() != null) {
			setDefaultRenderer(Object.class, model.getTableConfig().getDefauTableCellRenderer());
		}
		setSelectionMode(model.getTableConfig().getSelectionMode());

		buildColumnModel();

		setTableHeader(model.getTableConfig().isShowTableHeader() ? new JTableHeader(getColumnModel()) : null);
		if (model.getTableConfig().isShowTableHeader()) {
			getTableHeader().setReorderingAllowed(model.getTableConfig().isReorderingAllowed());
			if (model.getTableConfig().isEnableHeaderContextMenu()) {
				getTableHeader().addMouseListener(new PopupMouseListener(getPopupMenu()));
			}
		}
		setColumnSelectionAllowed(model.getTableConfig().isColumnSelectionAllowed());
		getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (e.getValueIsAdjusting() || isUpdatingProgrammatically) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			Set<String> selectedRows = retrieveSelectedRows(lsm);

			dispatch.accept(new GenericTableViewMsg.ChangeSelection(selectedRows));
		});

		setDragEnabled(model.getTableConfig().isDragEnabled());

		columnResizeNotifier.setRepeats(false);
	}

	public void updateTable(GenericTableViewModel model) {
		this.model = model;

		isUpdatingProgrammatically = true;

		RowSorter<? extends TableModel> sorter = getRowSorter();
		if (sorter != null) {
			sorter.removeRowSorterListener(rowSorterListener);
			sorter.addRowSorterListener(rowSorterListener);
		}

		getColumnModel().removeColumnModelListener(columnModelListener);

		if (model.isRebuildTableModel()) {
			rebuildTableModel();
		}

		getColumnModel().addColumnModelListener(columnModelListener);

		restoreSelection();

		isUpdatingProgrammatically = false;
	}

	private void rebuildTableModel() {
		buildColumnModel();

		setModel(new GenericTableModel(model, msg -> dispatch.accept(msg), isCellEditable));

		restoreSortState();

		rebuildColumns();
	}

	private void restoreSortState() {
		Map<String, SortOrder> rowSortKeys = model.getTableConfig().getSortKeys();

		if (rowSortKeys == null) {
			setRowSorter(new TableRowSorter<>(getModel()));
			return;
		}

		List<String> visibleColumnKeys = model.getColumns().stream().filter(TableColumnConfig::isVisible)
				.map(TableColumnConfig::getHeader).toList();

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		for (Map.Entry<String, SortOrder> entry : rowSortKeys.entrySet()) {
			if (entry.getKey() == null || !visibleColumnKeys.contains(entry.getKey())) {
				continue;
			}

			TableColumn col = getColumn(entry.getKey());
			if (col != null) {
				sortKeys.add(new RowSorter.SortKey(col.getModelIndex(), entry.getValue()));
			}
		}

		RowSorter<? extends TableModel> sorter = getRowSorter();
		if (sorter instanceof TableRowSorter<? extends TableModel> tableRowSorter) {
			tableRowSorter.setSortKeys(sortKeys);
		}
	}

	private void rebuildColumns() {
		for (TableColumnConfig config : model.getVisibleColumns()) {
			TableColumn col = getColumn(config.getHeader());
			if (col == null) {
				col = getColumn(config.getKey());
			}

			if (col != null && config.getPrefferedWidth() > 0) {
				col.setPreferredWidth(config.getPrefferedWidth());
				col.setWidth(config.getPrefferedWidth());
			}

			if (col != null && config.getEditor() != null) {
				col.setCellEditor(config.getEditor());
			}

			if (col != null && config.getRenderer() != null) {
				col.setCellRenderer(config.getRenderer());
			}

			if (config.getComparator() != null) {
				TableRowSorter<?> rowSorter = (TableRowSorter<?>) getRowSorter();
				rowSorter.setComparator(getColumn(config.getHeader()).getModelIndex(), config.getComparator());
			}
		}
	}

	private void buildColumnModel() {
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();

		for (TableColumnConfig columnConfig : model.getVisibleColumns()) {
			TableColumn col = new TableColumn();
			col.setHeaderValue(columnConfig.getHeader());
			col.setIdentifier(columnConfig.getKey());

			if (columnConfig.getMaxWidth() > 0) {
				col.setMaxWidth(columnConfig.getMaxWidth());
			}

			if (columnConfig.getRenderer() != null) {
				col.setCellRenderer(columnConfig.getRenderer());
			}

			if (columnConfig.getEditor() != null) {
				col.setCellEditor(columnConfig.getEditor());
			}

			newColumnModel.addColumn(col);
		}

		setColumnModel(newColumnModel);
	}

	private void restoreSelection() {
		Set<String> selectedRows = model.getSelectedRows();
		ListSelectionModel lsm = getSelectionModel();

		lsm.clearSelection();

		if (selectedRows != null) {
			for (String id : selectedRows) {
				int index = findRowIndexById(id);
				if (index >= 0 && index < getRowCount()) {
					lsm.addSelectionInterval(index, index);
				}
			}
		}
	}

	/**
	 * Finds the row index in the CURRENT VIEW (sorted/filtered) for a given ID.
	 * Returns -1 if not found or filtered out.
	 */
	public int findRowIndexById(String id) {
		for (int i = 0; i < model.getRows().size(); i++) {
			if (model.getRows().get(i).getId().equals(id)) {
				return convertRowIndexToView(i);
			}
		}
		return -1;
	}

	@Override
	public int convertColumnIndexToView(int modelColumnIndex) {
		return convertIndex(modelColumnIndex);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		Component c = super.prepareRenderer(renderer, row, col);
		dispatch.accept(new GenericTableViewMsg.PrepareRenderer((JComponent) c, row, col));
		return c;
	}

	private int convertIndex(int modelColumnIndex) {
		List<TableColumnConfig> visibleColumns = model.getColumns().stream().filter(TableColumnConfig::isVisible)
				.toList();

		if (modelColumnIndex >= 0 && modelColumnIndex < visibleColumns.size()) {
			TableColumnConfig config = visibleColumns.get(modelColumnIndex);
			return model.getColumns().indexOf(config);
		}
		return -1;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableCellRenderer renderer = getTableCellRenderer(column);
		return renderer != null ? renderer : super.getCellRenderer(row, column);
	}

	private JPopupMenu getPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		List<TableColumnConfig> columns = model.getColumns();

		for (TableColumnConfig column : columns) {
			if (column.isToggleable()) {
				popupMenu.add(createShowColumnCheckBoxMenuItem(column));
			}
		}

		return popupMenu;
	}

	private JCheckBoxMenuItem createShowColumnCheckBoxMenuItem(TableColumnConfig column) {
		String key = column.getKey();
		String headerText = column.getHeader();
		boolean isVisible = column.isVisible();

		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(headerText, isVisible);
		menuItem.addActionListener(event -> dispatch.accept(new GenericTableViewMsg.ToggleColumn(key)));

		return menuItem;
	}

	private Set<String> retrieveSelectedRows(ListSelectionModel lsm) {
		Set<String> selectedRows = new HashSet<>();

		int[] viewIndices = lsm.getSelectedIndices();

		for (int viewIndex : viewIndices) {
			int modelIndex = convertRowIndexToModel(viewIndex);
			if (modelIndex >= 0 && modelIndex < model.getRows().size()) {
				selectedRows.add(model.getRows().get(modelIndex).getId());
			}
		}

		return selectedRows;
	}

	private TableCellRenderer getTableCellRenderer(int column) {
		TableColumnConfig config = model.getColumnByModelIndex(column);

		if (config != null && config.getRenderer() != null) {
			return config.getRenderer();
		}

		return null;
	}
}
