/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromTable;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import net.miginfocom.swing.MigLayout;

public class GenericTableViewComponent
		extends AbstractTeaComponent<GenericTableViewModel, GenericTableViewMsg, GenericTableViewEffect> {
	private JTable table;
	private TableSearchPane searchPane;
	private boolean isUpdatingProgrammatically;

	public GenericTableViewComponent() {
		super();
	}

	public GenericTableViewComponent(GenericTableViewModel model) {
		super(model);
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
		table = new JTable(new GenericTableModel(model, msg -> dispatch(msg)), null) {
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				List<TableColumnConfig> visibleColumns = getVisibleColumns();

				if (column < 0 || column >= visibleColumns.size()) {
					return super.getCellRenderer(row, column);
				}

				TableColumnConfig config = visibleColumns.get(column);

				if (config.getRenderer() != null) {
					return config.getRenderer();
				}

				return super.getCellRenderer(row, column);
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

		JScrollPane jScrollPaneInfo = new JScrollPane(table);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel panel = new JPanel();

		panel.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fillx, wrap 1", "[grow, fill]",
				"[]" + Globals.GAP_SIZE + "[grow, fill]"));

		if (model.isShowSearchPane()) {
			searchPane = new TableSearchPane(
					model.getSearchTargetModelFromTable() == null ? new SearchTargetModelFromTable(table)
							: model.getSearchTargetModelFromTable());
			searchPane.setFilterKey(model.getFilterKey());
			searchPane.setFiltering();
			panel.add(searchPane);
		}

		panel.add(jScrollPaneInfo, "grow, push");

		return panel;
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

	private List<TableColumnConfig> getVisibleColumns() {
		return model.getColumns().stream().filter(TableColumnConfig::isVisible).toList();
	}

	// TODO: remove later (exists only for compatibility with old code)
	public JTable getTable() {
		return table;
	}

	// TODO: remove later (exists only for compatibility with old code)
	public TableSearchPane getSearchPane() {
		return searchPane;
	}

	private JPopupMenu getPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		List<TableColumnConfig> columns = model.getColumns();

		for (TableColumnConfig column : columns) {
			popupMenu.add(createShowColumnCheckBoxMenuItem(column));
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

		if (model.isRebuildTableModel()) {
			buildColumnModel();

			table.setModel(new GenericTableModel(model, msg -> dispatch(msg)));
		}

		restoreSelection();

		isUpdatingProgrammatically = false;
	}

	private void buildColumnModel() {
		DefaultTableColumnModel newColumnModel = new DefaultTableColumnModel();

		List<TableColumnConfig> visibleColumnConfigs = model.getColumns().stream().filter(TableColumnConfig::isVisible)
				.toList();

		for (TableColumnConfig columnConfig : visibleColumnConfigs) {
			TableColumn col = new TableColumn();
			col.setHeaderValue(columnConfig.getHeader());
			col.setIdentifier(columnConfig.getKey());

			if (columnConfig.getPrefferedWidth() > 0) {
				col.setPreferredWidth(columnConfig.getPrefferedWidth());
			}

			if (columnConfig.getMaxWidth() > 0) {
				col.setMaxWidth(columnConfig.getMaxWidth());
			}

			if (columnConfig.getRenderer() != null) {
				col.setCellRenderer(columnConfig.getRenderer());
			}

			newColumnModel.addColumn(col);
		}

		table.setColumnModel(newColumnModel);
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

	@SuppressWarnings("java:S2972")
	public static class GenericTableModel extends AbstractTableModel {
		private final GenericTableViewModel tableModel;
		private final Consumer<GenericTableViewMsg> dispatcher;

		public GenericTableModel(GenericTableViewModel model, Consumer<GenericTableViewMsg> dispatcher) {
			this.tableModel = model;
			this.dispatcher = dispatcher;
		}

		@Override
		public int getColumnCount() {
			return (int) tableModel.getColumns().stream().filter(TableColumnConfig::isVisible).count();
		}

		@Override
		public String getColumnName(int column) {
			List<TableColumnConfig> visibleColumns = tableModel.getColumns().stream()
					.filter(TableColumnConfig::isVisible).toList();

			if (column >= 0 && column < visibleColumns.size()) {
				return visibleColumns.get(column).getHeader();
			}
			return "";
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

			List<TableColumnConfig> visibleColumns = tableModel.getColumns().stream()
					.filter(TableColumnConfig::isVisible).toList();

			if (columnIndex < 0 || columnIndex >= visibleColumns.size()) {
				return null;
			}

			TableColumnConfig config = visibleColumns.get(columnIndex);
			String logicalKey = config.getKey();

			RowData rowData = tableModel.getRows().get(rowIndex);

			return rowData.getValue(logicalKey, Object.class);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return tableModel.getColumns().get(col).isEditable();
		}

		@Override
		public void setValueAt(Object newValue, int row, int col) {
			dispatcher.accept(new GenericTableViewMsg.CellEdited(row, col, newValue));
		}
	}
}
