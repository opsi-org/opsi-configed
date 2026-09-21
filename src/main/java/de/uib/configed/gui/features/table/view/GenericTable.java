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
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.gui.features.table.GenericTableModel;
import de.uib.configed.gui.features.table.GenericTableViewComponent.RendererPreparator;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.share.logging.Logging;

@SuppressWarnings("java:S1200")
public class GenericTable extends JTable {
	private GenericTableViewModel model;
	private boolean isUpdatingProgrammatically;
	private Function<Integer, Boolean> isCellEditable;
	private Consumer<GenericTableViewMsg> dispatch;
	private RendererPreparator rendererPreparator;

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
			Function<Integer, Boolean> isCellEditable, RendererPreparator rendererPreparator) {
		this.model = model;
		this.dispatch = dispatch;
		this.isCellEditable = isCellEditable;
		this.rendererPreparator = rendererPreparator;

		super(new GenericTableModel(model, dispatch::accept, isCellEditable), null);

	}

	private void notifyRowSorterChange() {
		RowSorter<? extends TableModel> sorter = getRowSorter();
		if (sorter == null) {
			return;
		}

		List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();

		Map<String, SortOrder> rowSortKeys = new HashMap<>();
		if (sortKeys.isEmpty()) {
			rowSortKeys.put(null, SortOrder.UNSORTED);
		} else {
			for (RowSorter.SortKey key : sortKeys) {
				TableColumnConfig column = model.getColumnByModelIndex(key.getColumn());
				if (column != null) {
					String columnKey = column.getKey();
					rowSortKeys.put(columnKey, key.getSortOrder());
				}
			}
		}

		dispatch.accept(new GenericTableViewMsg.ChangeSortOrder(rowSortKeys));
	}

	public void initialize() {
		setFillsViewportHeight(model.getTableConfig().isFillViewportHeight());
		setAutoCreateRowSorter(model.getTableConfig().isAutoCreateRowSorter());
		setAutoCreateColumnsFromModel(false);

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

			if (!selectedRows.equals(model.getSelectedRows())) {
				dispatch.accept(new GenericTableViewMsg.ChangeSelection(selectedRows));
			}
		});

		setDragEnabled(model.getTableConfig().isDragEnabled());

		columnResizeNotifier.setRepeats(false);
	}

	public void runWithoutSelectionEvents(Runnable runnable) {
		boolean originalValue = isUpdatingProgrammatically;
		isUpdatingProgrammatically = true;
		try {
			runnable.run();
		} finally {
			isUpdatingProgrammatically = originalValue;
		}
	}

	public void updateTable(GenericTableViewModel model) {
		this.model = model;

		isUpdatingProgrammatically = true;
		if (model.isRebuildTableModel() && isEditing()) {
			removeEditor();
		}

		RowSorter<? extends TableModel> sorter = getRowSorter();
		if (sorter != null) {
			sorter.removeRowSorterListener(rowSorterListener);
		}

		getColumnModel().removeColumnModelListener(columnModelListener);

		if (model.isRebuildTableModel()) {
			rebuildTableModel();
		}

		sorter = getRowSorter();
		if (sorter != null) {
			sorter.removeRowSorterListener(rowSorterListener);
			sorter.addRowSorterListener(rowSorterListener);
		}

		getColumnModel().addColumnModelListener(columnModelListener);

		restoreSelection();

		isUpdatingProgrammatically = false;
	}

	private void rebuildTableModel() {
		setModel(new GenericTableModel(model, msg -> dispatch.accept(msg), isCellEditable));
		buildColumnModel();

		restoreSortState();

		rebuildColumns();
	}

	private void restoreSortState() {
		TableRowSorter<TableModel> tableRowSorter = new TableRowSorter<>(getModel());
		setRowSorter(tableRowSorter);

		List<TableColumnConfig> columns = model.getColumns();
		for (int i = 0; i < columns.size(); i++) {
			TableColumnConfig config = columns.get(i);
			if (config.getComparator() != null) {
				tableRowSorter.setComparator(i, config.getComparator());
			}
		}

		Map<String, SortOrder> rowSortKeys = model.getTableConfig().getSortKeys();
		if (rowSortKeys == null || rowSortKeys.isEmpty()) {
			return;
		}

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		for (Map.Entry<String, SortOrder> entry : rowSortKeys.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == SortOrder.UNSORTED) {
				continue;
			}

			TableColumn col = findTableColumn(entry.getKey());
			if (col != null) {
				sortKeys.add(new RowSorter.SortKey(col.getModelIndex(), entry.getValue()));
			}
		}

		if (!sortKeys.isEmpty()) {
			tableRowSorter.setSortKeys(sortKeys);
		}
	}

	private TableColumn findTableColumn(String keyOrHeader) {
		TableColumnModel colModel = getColumnModel();
		for (int i = 0; i < colModel.getColumnCount(); i++) {
			TableColumn c = colModel.getColumn(i);
			if (keyOrHeader.equals(c.getIdentifier()) || keyOrHeader.equals(c.getHeaderValue())) {
				return c;
			}
		}
		return null;
	}

	private void rebuildColumns() {
		for (TableColumnConfig config : model.getVisibleColumns()) {
			TableColumn col = findTableColumn(config.getKey());
			if (col != null) {
				applyColumnConfig(col, config);
			}
		}
	}

	private void applyColumnConfig(TableColumn col, TableColumnConfig config) {
		if (config.getPrefferedWidth() > 0) {
			col.setPreferredWidth(config.getPrefferedWidth());
			col.setWidth(config.getPrefferedWidth());
		}

		if (config.getEditor() != null) {
			col.setCellEditor(config.getEditor());
		}

		if (config.getRenderer() != null) {
			col.setCellRenderer(config.getRenderer());
		}

		if (config.getComparator() != null && getRowSorter() instanceof TableRowSorter<?> tableRowSorter) {
			tableRowSorter.setComparator(col.getModelIndex(), config.getComparator());
		}
	}

	private void buildColumnModel() {
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();
		List<TableColumnConfig> columns = model.getColumns();

		for (int i = 0; i < columns.size(); i++) {
			TableColumnConfig columnConfig = columns.get(i);
			if (!columnConfig.isVisible()) {
				continue;
			}

			TableColumn col = new TableColumn(i);
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
	public int convertRowIndexToModel(int viewRowIndex) {
		if (viewRowIndex < 0) {
			return -1;
		}

		int viewRowCount = getRowCount();
		if (viewRowIndex >= viewRowCount) {
			return -1;
		}

		int modelIndex;

		// Delegate to the RowSorter for the actual conversion
		// This handles sorted/filter conversions within the filtered model space
		RowSorter<? extends TableModel> sorter = getRowSorter();
		if (sorter == null || sorter.getModel() == null) {
			// No sorting → view and model indices are identical
			modelIndex = viewRowIndex;
		} else {
			try {
				modelIndex = sorter.convertRowIndexToModel(viewRowIndex);
			} catch (ArrayIndexOutOfBoundsException e) {
				Logging.error(this, "failed to convert view index to model index using default", viewRowIndex, e);
				// Fallback: If sorter state is inconsistent (e.g., during rebuild),
				// fall back to direct model index lookup
				modelIndex = viewRowIndex;
			}
		}

		return modelIndex;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
		Component c = super.prepareRenderer(renderer, row, col);
		if (rendererPreparator != null) {
			rendererPreparator.prepare(c, row, col);
		}
		return c;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableCellRenderer renderer = getTableCellRenderer(column);
		return renderer != null ? renderer : super.getCellRenderer(row, column);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		TableCellEditor editor = getTableCellEditor(column);
		return editor != null ? editor : super.getCellEditor(row, column);
	}

	private JPopupMenu getPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		List<TableColumnConfig> columns = model.getColumns();

		for (TableColumnConfig column : columns) {
			if (column.isToggleable()) {
				popupMenu.add(createShowColumnCheckBoxMenuItem(column));
			}
		}

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				for (Component c : popupMenu.getComponents()) {
					if (c instanceof JCheckBoxMenuItem item) {
						String key = item.getActionCommand();
						TableColumnConfig column = model.getColumnByKey(key);
						if (column != null) {
							item.setState(column.isVisible());
						}
					}
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// No action needed when the popup menu becomes invisible
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// No action needed when the popup menu is canceled
			}
		});

		return popupMenu;
	}

	private JCheckBoxMenuItem createShowColumnCheckBoxMenuItem(TableColumnConfig column) {
		String key = column.getKey();
		String headerText = column.getHeader();
		boolean isVisible = column.isVisible();

		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(headerText, isVisible);
		menuItem.setActionCommand(column.getKey());
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
		TableColumnConfig config = model.getColumnByModelIndex(convertColumnIndexToModel(column));

		if (config != null && config.getRenderer() != null) {
			return config.getRenderer();
		}

		return null;
	}

	private TableCellEditor getTableCellEditor(int column) {
		TableColumnConfig config = model.getColumnByModelIndex(convertColumnIndexToModel(column));

		if (config != null && config.getEditor() != null) {
			return config.getEditor();
		}

		return null;
	}
}
