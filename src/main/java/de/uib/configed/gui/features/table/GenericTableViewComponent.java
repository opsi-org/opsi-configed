/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromTable;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import net.miginfocom.swing.MigLayout;

public class GenericTableViewComponent
		extends AbstractTeaComponent<GenericTableViewModel, GenericTableViewMsg, GenericTableViewEffect> {
	private JTable table;
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
		table = new JTable(new GenericTableModel(), null);
		table.setFillsViewportHeight(model.getTableConfig().isFillViewportHeight());
		table.setAutoCreateRowSorter(model.getTableConfig().isAutoCreateRowSorter());
		table.setDefaultRenderer(Object.class, model.getTableConfig().getDefauTableCellRenderer());
		table.setSelectionMode(model.getTableConfig().getSelectionMode());
		table.setTableHeader(
				model.getTableConfig().isShowTableHeader() ? new JTableHeader(table.getColumnModel()) : null);
		if (model.getTableConfig().isShowTableHeader()) {
			table.getTableHeader().setReorderingAllowed(model.getTableConfig().isReorderingAllowed());
		}
		table.setColumnSelectionAllowed(model.getTableConfig().isColumnSelectionAllowed());
		table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (e.getValueIsAdjusting() || isUpdatingProgrammatically) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			Set<Integer> selectedRows = retrieveSelectedRows(lsm);

			dispatch(new GenericTableViewMsg.ChangeSelection(selectedRows));
		});

		for (int i = 0; i < model.getColumns().size(); i++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(i);
			TableColumnConfig config = model.getColumns().get(i);

			tableColumn.setPreferredWidth(config.getPrefferedWidth());

			if (config.getRenderer() != null) {
				tableColumn.setCellRenderer(config.getRenderer());
			}
		}

		table.setDragEnabled(model.getTableConfig().isDragEnabled());

		JScrollPane jScrollPaneInfo = new JScrollPane(table);
		jScrollPaneInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel panel = new JPanel();

		panel.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fillx, wrap 1", "[grow, fill]",
				"[]" + Globals.GAP_SIZE + "[grow, fill]"));

		if (model.isShowSearchPane()) {
			TableSearchPane searchPane = new TableSearchPane(
					model.getSearchTargetModelFromTable() == null ? new SearchTargetModelFromTable(table)
							: model.getSearchTargetModelFromTable());
			searchPane.setFilterKey(model.getFilterKey());
			searchPane.setFiltering();
			panel.add(searchPane);
		}

		panel.add(jScrollPaneInfo, "grow, push");

		return panel;
	}

	private static Set<Integer> retrieveSelectedRows(ListSelectionModel lsm) {
		Set<Integer> selectedRows = new HashSet<>();

		if (!lsm.isSelectionEmpty()) {
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();

			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					selectedRows.add(i);
				}
			}
		}

		return selectedRows;
	}

	@Override
	protected void refreshView() {
		isUpdatingProgrammatically = true;

		table.setModel(new GenericTableModel());

		restoreSelection();

		isUpdatingProgrammatically = false;
	}

	private void restoreSelection() {
		Set<Integer> selectedRows = model.getSelectedRows();
		ListSelectionModel lsm = table.getSelectionModel();

		lsm.clearSelection();

		if (selectedRows != null) {
			for (Integer index : selectedRows) {
				if (index >= 0 && index < table.getRowCount()) {
					lsm.addSelectionInterval(index, index);
				}
			}
		}
	}

	@SuppressWarnings("java:S2972")
	private class GenericTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return model.getColumns().size();
		}

		@Override
		public String getColumnName(int col) {
			return model.getColumns().get(col).getHeader();
		}

		@Override
		public int getRowCount() {
			return model.getRows().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return model.getRows().get(rowIndex).getValue(model.getColumns().get(columnIndex).getKey(), Object.class);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return model.getColumns().get(col).isEditable();
		}

		@Override
		public void setValueAt(Object newValue, int row, int col) {
			dispatch(new GenericTableViewMsg.CellEdited(row, col, newValue));
		}
	}
}
