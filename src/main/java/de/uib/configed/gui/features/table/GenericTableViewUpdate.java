/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.table.GenericTableViewMsg.AddRow;
import de.uib.configed.gui.features.table.GenericTableViewMsg.CancelChanges;
import de.uib.configed.gui.features.table.GenericTableViewMsg.CellEdited;
import de.uib.configed.gui.features.table.GenericTableViewMsg.ChangeOriginalSnapshot;
import de.uib.configed.gui.features.table.GenericTableViewMsg.ChangeSelection;
import de.uib.configed.gui.features.table.GenericTableViewMsg.CommitChanges;
import de.uib.configed.gui.features.table.GenericTableViewMsg.DeleteRow;
import de.uib.configed.gui.features.table.RowData.RowState;

/**
 * The Pure Logic Layer. Handles messages and updates the Model.
 */
public final class GenericTableViewUpdate {
	private GenericTableViewUpdate() {
	}

	public static UpdateResult<GenericTableViewModel, GenericTableViewEffect> update(GenericTableViewMsg msg,
			GenericTableViewModel model) {
		return switch (msg) {
		case CellEdited(int rowIdx, int colIdx, Object newValue) -> handleCellEdit(rowIdx, colIdx, newValue, model);
		case CommitChanges() -> handleCommit(model);
		case CancelChanges() -> handleCancel(model);
		case ChangeSelection(Set<Integer> selectedRows) -> UpdateResult.noEffect(model.withSelectedRows(selectedRows));
		case AddRow(Map<String, Object> data) -> handleRowAdd(data, model);
		case DeleteRow(int rowIdx) -> handleRowDelete(rowIdx, model);
		case ChangeOriginalSnapshot(List<Map<String, Object>> originalSnapshot) -> UpdateResult.noEffect(
				model.withOriginalSnapshot(originalSnapshot).withRows(RowData.fromOriginalSnapshot(originalSnapshot)));
		default -> UpdateResult.noEffect(model);
		};
	}

	private static UpdateResult<GenericTableViewModel, GenericTableViewEffect> handleCellEdit(int rowIdx, int colIdx,
			Object newValue, GenericTableViewModel model) {
		if (rowIdx < 0 || rowIdx >= model.getRows().size()) {
			return UpdateResult.noEffect(model);
		}

		RowDiffStrategy strategy = model.getDiffStrategy();

		RowData oldRow = model.getRows().get(rowIdx);
		String colKey = model.getColumns().get(colIdx).getKey();

		RowState newRowStyle = strategy.getRowStyle(oldRow.getId(), colKey, newValue,
				oldRow.getValue(colKey, Object.class));

		Map<String, Object> newValues = new HashMap<>(oldRow.getValues());
		newValues.put(colKey, newValue);

		RowData newRow = oldRow.toBuilder().values(newValues).state(newRowStyle).build();

		List<RowData> newRows = new ArrayList<>(model.getRows());
		newRows.set(rowIdx, newRow);

		boolean isDirty = newRows.stream().anyMatch(r -> r.getState() != RowState.NORMAL);

		return UpdateResult.noEffect(model.withRows(newRows).withDirty(isDirty));
	}

	private static UpdateResult<GenericTableViewModel, GenericTableViewEffect> handleCommit(
			GenericTableViewModel model) {
		if (!model.isDirty()) {
			return UpdateResult.noEffect(model);
		}

		List<RowData> newRows = model.getRows().stream().filter((RowData row) -> row.getState() != RowState.DELETED)
				.toList();

		return UpdateResult.withEffect(model.withRows(newRows).withDirty(false),
				new GenericTableViewEffect.SaveChanges(newRows));
	}

	private static UpdateResult<GenericTableViewModel, GenericTableViewEffect> handleCancel(
			GenericTableViewModel model) {
		if (!model.isDirty()) {
			return UpdateResult.noEffect(model);
		}

		List<RowData> restoredRows = RowData.fromOriginalSnapshot(model.getOriginalSnapshot());

		return UpdateResult.noEffect(model.withRows(restoredRows).withDirty(false));
	}

	private static UpdateResult<GenericTableViewModel, GenericTableViewEffect> handleRowAdd(Map<String, Object> data,
			GenericTableViewModel model) {
		String id = UUID.randomUUID().toString();
		RowData newRow = RowData.builder().id(id).values(data).state(RowState.NEW).build();

		List<RowData> newRows = new ArrayList<>(model.getRows());
		newRows.add(newRow);

		return UpdateResult.noEffect(model.withRows(newRows).withDirty(true));
	}

	private static UpdateResult<GenericTableViewModel, GenericTableViewEffect> handleRowDelete(int rowIdx,
			GenericTableViewModel model) {
		if (rowIdx < 0 || rowIdx >= model.getRows().size()) {
			return UpdateResult.noEffect(model);
		}

		List<RowData> rows = new ArrayList<>(model.getRows());
		rows.remove(rowIdx);

		return UpdateResult.noEffect(model.withRows(rows).withDirty(true));
	}
}
