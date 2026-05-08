/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.table;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.table.RowData.RowState;

class GenericTableViewUpdateTest {
	private static GenericTableViewModel baseModel() {
		List<Map<String, Object>> originalSnapshot = new ArrayList<>();
		originalSnapshot.add(row("1", "test", "test"));
		originalSnapshot.add(row("2", "test2", "test2"));
		originalSnapshot.add(row("3", "test3", "test3"));
		originalSnapshot.add(row("4", "test4", "test4"));
		originalSnapshot.add(row("5", "test5", "test5"));

		List<RowData> rows = RowData.fromOriginalSnapshot(originalSnapshot);

		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "data0", false, 0, null, null));
		columns.add(new TableColumnConfig("data1", "data1", false, 0, null, null));
		columns.add(new TableColumnConfig("data2", "data2", false, 0, null, null));

		return GenericTableViewModel.builder().originalSnapshot(originalSnapshot).rows(rows).columns(columns)
				.diffStrategy((String rowId, String colKey, Object currentValue, Object originalValue) -> {
					if (!currentValue.equals(originalValue)) {
						return RowState.MODIFIED;
					}
					return RowState.NORMAL;
				}).build();
	}

	private static Map<String, Object> row(String data0, String data1, String data2) {
		Map<String, Object> row = new HashMap<>();
		row.put("data0", data0);
		row.put("data1", data1);
		row.put("data2", data2);
		return row;
	}

	@Test
	void shouldUpdateRow_whenCellEdited() {
		GenericTableViewModel model = baseModel();
		int rowIdx = 1;
		int colIdx = 2;
		GenericTableViewMsg msg = new GenericTableViewMsg.CellEdited(1, 2, "test");

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model, result.model());
		assertTrue(result.model().isDirty());

		String resultColumnKey = model.getColumns().get(colIdx).getKey();
		assertEquals("data2", resultColumnKey);

		String resultValue = result.model().getRows().get(rowIdx).getValue(resultColumnKey, String.class);
		assertEquals("test", resultValue);
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldAddRow_whenRowAdded() {
		GenericTableViewModel model = baseModel();
		Map<String, Object> row = row("6", "test6", "test6");
		GenericTableViewMsg msg = new GenericTableViewMsg.AddRow(row);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model, result.model());
		assertTrue(result.model().isDirty());

		RowData resultLastRow = result.model().getRows().get(result.model().getRows().size() - 1);
		RowData lastRow = model.getRows().get(model.getRows().size() - 1);
		assertNotSame(lastRow, resultLastRow);
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerSaveChanges_whenCommitChanges() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.CommitChanges();

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withDirty(true));

		assertNotNull(result.model());
		assertNotSame(model, result.model());
		assertFalse(result.model().isDirty());

		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.SaveChanges.class, result.effect().get()));
	}

	@Test
	void shouldRevertChangesWhenDataEdited_whenCancelChanges() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.CancelChanges();

		List<Map<String, Object>> modifiedRows = new ArrayList<>();
		modifiedRows.add(row("1", "test", "test"));
		modifiedRows.add(row("2", "test2", "test2"));
		modifiedRows.add(row("3", "test3", "test2"));
		modifiedRows.add(row("4", "test6", "test4"));
		modifiedRows.add(row("5", "test5", "test4"));

		List<RowData> rows = RowData.fromOriginalSnapshot(modifiedRows);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withRows(rows).withDirty(true));

		assertNotNull(result.model());
		assertEquals(model.getRows().size(), result.model().getRows().size());
		assertNotEquals("test6", result.model().getRows().get(3).getValue("data1", String.class));
		assertNotEquals("test2", result.model().getRows().get(2).getValue("data2", String.class));
		assertFalse(result.model().isDirty());
	}

	@Test
	void shouldRevertChangesWhenRowDeleted_whenCancelChanges() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.CancelChanges();

		List<Map<String, Object>> modifiedRows = new ArrayList<>();
		modifiedRows.add(row("1", "test", "test"));
		modifiedRows.add(row("2", "test2", "test2"));
		modifiedRows.add(row("3", "test3", "test3"));
		modifiedRows.add(row("5", "test5", "test4"));

		List<RowData> rows = RowData.fromOriginalSnapshot(modifiedRows);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withRows(rows).withDirty(true));

		assertNotNull(result.model());
		assertSame(model.getRows().size(), result.model().getRows().size());
		assertEquals("5", result.model().getRows().get(4).getValue("data0", String.class));
		assertFalse(result.model().isDirty());
	}

	@Test
	void shouldDoNothingIfNoChanges_whenCancelChanges() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.CancelChanges();

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withDirty(false));

		assertNotNull(result.model());
		assertSame(model.getRows(), result.model().getRows());
		assertFalse(result.model().isDirty());
	}

	@Test
	void shouldDeleteRowFromRows_whenRowDeleted() {
		GenericTableViewModel model = baseModel();
		int rowIdx = 2;
		GenericTableViewMsg msg = new GenericTableViewMsg.DeleteRow(rowIdx);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model.getRows().size(), result.model().getRows().size());
		assertNotEquals("3", result.model().getRows().get(rowIdx).getValue("data0", String.class));
		assertTrue(result.model().isDirty());
	}

	@Test
	void shouldUpdateOriginalSnapshot_whenUpdateOriginal() {
		GenericTableViewModel model = baseModel();

		List<Map<String, Object>> originalSnapshot = new ArrayList<>();
		originalSnapshot.add(row("1", "test", "test"));
		originalSnapshot.add(row("2", "test2", "test2"));
		originalSnapshot.add(row("3", "test3", "test3"));

		GenericTableViewMsg msg = new GenericTableViewMsg.ChangeOriginalSnapshot(originalSnapshot);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model.getOriginalSnapshot().size(), result.model().getOriginalSnapshot().size());
		assertNotSame(model.getRows().size(), result.model().getRows().size());
	}

	@Test
	void shouldUpdateSelectedIndex_whenChangeSelection() {
		GenericTableViewModel model = baseModel();
		Set<Integer> selectedRows = Set.of(1, 2);
		GenericTableViewMsg msg = new GenericTableViewMsg.ChangeSelection(selectedRows);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(selectedRows.size(), result.model().getSelectedRows().size());
		assertEquals(selectedRows, result.model().getSelectedRows());
	}
}
