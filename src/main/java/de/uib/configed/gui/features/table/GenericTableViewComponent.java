/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.awt.Component;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.features.table.view.GenericTable;
import de.uib.configed.gui.share.PopupMouseListener;

public class GenericTableViewComponent
		extends AbstractTeaComponent<GenericTableViewModel, GenericTableViewMsg, GenericTableViewEffect> {
	private GenericTable table;
	private boolean addedPopupMouseListener;
	private TableSideEffectStrategy sideEffectStrategy;
	private Supplier<PopupMouseListener> popupMouseListenerSupplier;
	private Function<Integer, Boolean> isCellEditable;
	private PopupMouseListener popupMouseListener;
	private RendererPreparator rendererPreparator;

	public interface RendererPreparator {
		void prepare(Component component, int row, int col);
	}

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
		this(model, null, null, null);
	}

	public GenericTableViewComponent(GenericTableViewModel model, TableSideEffectStrategy sideEffectStrategy,
			Supplier<PopupMouseListener> popupMouseListenerSupplier) {
		this(model, sideEffectStrategy, popupMouseListenerSupplier, null);
	}

	public GenericTableViewComponent(GenericTableViewModel model, TableSideEffectStrategy sideEffectStrategy,
			Supplier<PopupMouseListener> popupMouseListenerSupplier, RendererPreparator rendererPreparator) {
		super(model);
		this.sideEffectStrategy = sideEffectStrategy;
		this.popupMouseListenerSupplier = popupMouseListenerSupplier;
		this.rendererPreparator = rendererPreparator;
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
		table = new GenericTable(model, dispatch, isCellEditable, rendererPreparator);
		table.initialize();

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		return scrollPane;
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
		return table.findRowIndexById(id);
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

	public RowData getRowByViewIndex(int viewIndex) {
		return getRowByModelIndex(table.convertRowIndexToModel(viewIndex));
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

	// Remove later (exists only for compatibility with old code)
	public JTable getTable() {
		return table;
	}

	@Override
	protected void refreshView() {
		table.updateTable(model);

		if (popupMouseListenerSupplier != null && (popupMouseListener == null || !popupMouseListener.initialized())) {
			popupMouseListener = popupMouseListenerSupplier.get();
		}

		if ((popupMouseListener != null && popupMouseListener.initialized()) && !addedPopupMouseListener) {
			table.addMouseListener(popupMouseListener);
			addedPopupMouseListener = true;
		}
	}

	// Remove later (exists only for compatibility with old code)
	public int getSelectedRow() {
		return table.getSelectedRow();
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
}
