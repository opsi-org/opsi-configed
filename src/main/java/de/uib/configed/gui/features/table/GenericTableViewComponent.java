/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;

public class GenericTableViewComponent
		extends AbstractTeaComponent<GenericTableViewModel, GenericTableViewMsg, GenericTableViewEffect> {
	private JTable table;
	private boolean isUpdatingProgrammatically;
	private boolean addedPopupMouseListener;
	private TableSideEffectStrategy sideEffectStrategy;
	private Supplier<PopupMouseListener> popupMouseListenerSupplier;
	private Function<Integer, Boolean> isCellEditable;
	private PopupMouseListener popupMouseListener;
	private RowSorterListener rowSorterListener = (RowSorterEvent e) -> {
		if (isUpdatingProgrammatically) {
			return;
		}

		notifyRowSorterChange();
	};
	private Timer columnResizeNotifier = new Timer(500, e -> notifyColumnResize());
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

	public interface TableSideEffectStrategy {
		/**
		 * Given an effect, return the side effect's action to execute. Return
		 * null if no side effect's action is needed.
		 */
		Runnable getActionFor(GenericTableViewEffect effect);
	}

	public GenericTableViewComponent() {
		super();
	}

	public GenericTableViewComponent(GenericTableViewModel model) {
		this(model, null, null);
	}

	public GenericTableViewComponent(GenericTableViewModel model, TableSideEffectStrategy sideEffectStrategy,
			Supplier<PopupMouseListener> popupMouseListenerSupplier) {
		super(model);
		this.sideEffectStrategy = sideEffectStrategy;
		this.popupMouseListenerSupplier = popupMouseListenerSupplier;
	}

	@Override
	protected GenericTableViewModel initModel() {
		return GenericTableViewModel.builder().build();
	}

	@Override
	protected UpdateResult<GenericTableViewModel, GenericTableViewEffect> updateModel(GenericTableViewMsg msg,
			GenericTableViewModel model) {
		return GenericTableViewUpdate.update(msg, model);
	}

	@Override
	protected JComponent renderView(GenericTableViewModel model, Consumer<GenericTableViewMsg> dispatch) {
		table = new JTable(new GenericTableModel(model, msg -> dispatch(msg), isCellEditable), null) {
			@Override
			public int convertColumnIndexToView(int modelColumnIndex) {
				return convertIndex(modelColumnIndex);
			}

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component c = super.prepareRenderer(renderer, row, col);
				dispatch(new GenericTableViewMsg.PrepareRenderer((JComponent) c, row, col));
				return c;
			}

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				TableCellRenderer renderer = getTableCellRenderer(row, column);
				return renderer != null ? renderer : super.getCellRenderer(row, column);
			}
		};
		table.setFillsViewportHeight(model.getTableConfig().isFillViewportHeight());
		table.setAutoCreateRowSorter(model.getTableConfig().isAutoCreateRowSorter());

		if (model.getTableConfig().getDefauTableCellRenderer() != null) {
			table.setDefaultRenderer(Object.class, model.getTableConfig().getDefauTableCellRenderer());
		}
		table.setSelectionMode(model.getTableConfig().getSelectionMode());
		buildColumnModel();
		table.setTableHeader(
				model.getTableConfig().isShowTableHeader() ? new JTableHeader(table.getColumnModel()) : null);
		if (model.getTableConfig().isShowTableHeader()) {
			table.getTableHeader().setReorderingAllowed(model.getTableConfig().isReorderingAllowed());
			if (model.getTableConfig().isEnableHeaderContextMenu()) {
				table.getTableHeader().addMouseListener(new PopupMouseListener(getPopupMenu()));
			}
		}
		table.setColumnSelectionAllowed(model.getTableConfig().isColumnSelectionAllowed());
		table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (e.getValueIsAdjusting() || isUpdatingProgrammatically) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			Set<String> selectedRows = retrieveSelectedRows(lsm);

			dispatch(new GenericTableViewMsg.ChangeSelection(selectedRows));
		});

		table.setDragEnabled(model.getTableConfig().isDragEnabled());

		columnResizeNotifier.setRepeats(false);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		return scrollPane;
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

	private TableCellRenderer getTableCellRenderer(int row, int column) {
		TableColumnConfig config = model.getColumnByModelIndex(column);

		if (config != null && config.getRenderer() != null) {
			return config.getRenderer();
		}

		return null;
	}

	private void notifyRowSorterChange() {
		List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();

		Map<String, SortOrder> rowSortKeys = new HashMap<>();
		if (sortKeys.isEmpty()) {
			rowSortKeys.put(null, SortOrder.UNSORTED);
		} else {
			for (RowSorter.SortKey key : sortKeys) {
				String columnKey = (String) table.getColumnModel().getColumn(key.getColumn()).getIdentifier();
				rowSortKeys.put(columnKey, key.getSortOrder());
			}
		}

		dispatch(new GenericTableViewMsg.ChangeSortOrder(rowSortKeys));
	}

	private void notifyColumnResize() {
		TableColumnModel columnModel = table.getColumnModel();
		Map<String, Integer> columnWidths = new HashMap<>();

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			columnWidths.put(col.getIdentifier().toString(), col.getWidth());
		}

		dispatch(new GenericTableViewMsg.ResizeColumns(columnWidths));
	}

	@Override
	protected void handleEffect(GenericTableViewEffect effect) {
		super.handleEffect(effect);

		if (sideEffectStrategy == null) {
			return;
		}

		Runnable action = sideEffectStrategy.getActionFor(effect);
		if (action != null) {
			action.run();
		}
	}

	/**
	 * Finds the row index in the CURRENT VIEW (sorted/filtered) for a given ID.
	 * Returns -1 if not found or filtered out.
	 */
	public int findRowIndexById(String id) {
		for (int i = 0; i < model.getRows().size(); i++) {
			if (model.getRows().get(i).getId().equals(id)) {
				return getTable().convertRowIndexToView(i);
			}
		}
		return -1;
	}

	/**
	 * Gets the RowData object directly by ID.
	 */
	public RowData getRowById(String id) {
		for (RowData row : model.getRows()) {
			if (row.getId().equals(id)) {
				return row;
			}
		}
		return null;
	}

	public RowData getRowByModelIndex(int modelIndex) {
		if (modelIndex < 0 || modelIndex >= model.getRows().size()) {
			throw new IndexOutOfBoundsException("Index " + modelIndex + " out of bounds");
		}
		return model.getRows().get(modelIndex);
	}

	public Object getValueAt(int row, int col) {
		if (row < 0 || row >= model.getRows().size()) {
			return null;
		}

		TableColumnConfig columnConfig = getColumnByModelIndex(col);
		return getRowByModelIndex(row).getValue(columnConfig.getKey(), Object.class);
	}

	/**
	 * Gets a ColumnConfig directly by its MODEL INDEX (position in the list).
	 * This is O(1) access without needing to look up by key first.
	 * 
	 * @param modelIndex The index in the underlying columns list (0-based)
	 * @return The TableColumnConfig, or null if index is out of bounds
	 */
	public TableColumnConfig getColumnByModelIndex(int modelIndex) {
		return model.getColumnByModelIndex(modelIndex);
	}

	public int getColumnIndexByKey(String key) {
		Logging.devel(this, "getColumnIndexByKey key ", key, "columns", model.getColumns());
		List<TableColumnConfig> visibleColumns = model.getVisibleColumns();
		for (int i = 0; i < visibleColumns.size(); i++) {
			if (visibleColumns.get(i).getKey().equals(key)) {
				return i;
			}
		}
		return -1;
	}

	public List<TableColumnConfig> getVisibleColumns() {
		return model.getVisibleColumns();
	}

	// TODO: remove later (exists only for compatibility with old code)
	public JTable getTable() {
		return table;
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
		menuItem.addActionListener(event -> dispatch(new GenericTableViewMsg.ToggleColumn(key)));

		return menuItem;
	}

	private Set<String> retrieveSelectedRows(ListSelectionModel lsm) {
		Set<String> selectedRows = new HashSet<>();

		int[] viewIndices = lsm.getSelectedIndices();

		for (int viewIndex : viewIndices) {
			int modelIndex = table.convertRowIndexToModel(viewIndex);
			if (modelIndex >= 0 && modelIndex < model.getRows().size()) {
				selectedRows.add(model.getRows().get(modelIndex).getId());
			}
		}

		return selectedRows;
	}

	@Override
	protected void refreshView() {
		isUpdatingProgrammatically = true;

		RowSorter<? extends TableModel> sorter = table.getRowSorter();
		if (sorter != null) {
			sorter.removeRowSorterListener(rowSorterListener);
			sorter.addRowSorterListener(rowSorterListener);
		}

		table.getColumnModel().removeColumnModelListener(columnModelListener);

		if (model.isRebuildTableModel()) {
			buildColumnModel();

			table.setModel(new GenericTableModel(model, msg -> dispatch(msg), isCellEditable));

			restoreSortState();

			rebuildColumns();
		}

		table.getColumnModel().addColumnModelListener(columnModelListener);

		restoreSelection();

		if (popupMouseListenerSupplier != null && (popupMouseListener == null || !popupMouseListener.initialized())) {
			popupMouseListener = popupMouseListenerSupplier.get();
		}

		if ((popupMouseListener != null && popupMouseListener.initialized()) && !addedPopupMouseListener) {
			table.addMouseListener(popupMouseListener);
			addedPopupMouseListener = true;
		}

		isUpdatingProgrammatically = false;
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

		table.setColumnModel(newColumnModel);
	}

	private void restoreSortState() {
		Map<String, SortOrder> rowSortKeys = model.getTableConfig().getSortKeys();

		if (rowSortKeys == null) {
			table.setRowSorter(new TableRowSorter<>(table.getModel()));
			return;
		}

		List<String> visibleColumnKeys = model.getColumns().stream().filter(TableColumnConfig::isVisible)
				.map(TableColumnConfig::getHeader).toList();

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		for (Map.Entry<String, SortOrder> entry : rowSortKeys.entrySet()) {
			if (entry.getKey() == null || !visibleColumnKeys.contains(entry.getKey())) {
				continue;
			}

			TableColumn col = table.getColumn(entry.getKey());
			if (col != null) {
				sortKeys.add(new RowSorter.SortKey(col.getModelIndex(), entry.getValue()));
			}
		}

		RowSorter<? extends TableModel> sorter = table.getRowSorter();
		if (sorter instanceof TableRowSorter<? extends TableModel> tableRowSorter) {
			tableRowSorter.setSortKeys(sortKeys);
		}
	}

	private void rebuildColumns() {
		for (TableColumnConfig config : model.getVisibleColumns()) {
			TableColumn col = table.getColumn(config.getHeader());
			if (col == null) {
				col = table.getColumn(config.getKey());
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
				TableRowSorter<?> rowSorter = (TableRowSorter<?>) table.getRowSorter();
				rowSorter.setComparator(table.getColumn(config.getHeader()).getModelIndex(), config.getComparator());
			}
		}
	}

	private void restoreSelection() {
		Set<String> selectedRows = model.getSelectedRows();
		ListSelectionModel lsm = table.getSelectionModel();

		lsm.clearSelection();

		if (selectedRows != null) {
			for (String id : selectedRows) {
				int index = findRowIndexById(id);
				if (index >= 0 && index < table.getRowCount()) {
					lsm.addSelectionInterval(index, index);
				}
			}
		}
	}

	public Set<String> getSelectedRows() {
		return model.getSelectedRows();
	}

	public int getSelectedRowCount() {
		return model.getSelectedRows().size();
	}

	public int getRowCount() {
		return model.getRows().size();
	}

	public List<RowData> getRows() {
		return model.getRows();
	}

	public void setIsCellEditable(Function<Integer, Boolean> isCellEditable) {
		this.isCellEditable = isCellEditable;
	}

	@SuppressWarnings("java:S2972")
	public static class GenericTableModel extends AbstractTableModel {
		private final GenericTableViewModel tableModel;
		private final Consumer<GenericTableViewMsg> dispatcher;
		private final Function<Integer, Boolean> isCellEditable;

		public GenericTableModel(GenericTableViewModel model, Consumer<GenericTableViewMsg> dispatcher,
				Function<Integer, Boolean> isCellEditable) {
			this.tableModel = model;
			this.dispatcher = dispatcher;
			this.isCellEditable = isCellEditable;
		}

		@Override
		public int getColumnCount() {
			return (int) tableModel.getColumns().stream().filter(TableColumnConfig::isVisible).count();
		}

		@Override
		public String getColumnName(int column) {
			TableColumnConfig config = tableModel.getColumnByModelIndex(column);
			return config != null ? config.getHeader() : null;
		}

		@Override
		public int getRowCount() {
			return tableModel.getRows().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < 0 || rowIndex >= tableModel.getRows().size()) {
				return null;
			}

			TableColumnConfig config = tableModel.getColumnByModelIndex(columnIndex);
			String logicalKey = config.getKey();

			RowData rowData = tableModel.getRows().get(rowIndex);

			return rowData.getValue(logicalKey, Object.class);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return tableModel.getColumnByModelIndex(col).isEditable()
					&& (isCellEditable == null || isCellEditable.apply(row));
		}

		@Override
		public void setValueAt(Object newValue, int row, int col) {
			dispatcher.accept(new GenericTableViewMsg.CellEdited(row, col, newValue));
		}
	}
}
