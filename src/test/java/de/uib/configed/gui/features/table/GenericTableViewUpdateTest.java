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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.SortOrder;

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

		List<RowData> rows = RowData.fromOriginalSnapshot(originalSnapshot, null);

		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "data0", false, true, false, 0, 0, null, null, null));
		columns.add(new TableColumnConfig("data1", "data1", false, true, false, 0, 0, null, null, null));
		columns.add(new TableColumnConfig("data2", "data2", false, true, false, 0, 0, null, null, null));

		return GenericTableViewModel.builder().originalSnapshot(originalSnapshot).rows(rows).columns(columns)
				.tableConfig(TableConfig.builder().build())
				.diffStrategy((RowData rowData, String colKey, Object currentValue, Object originalValue) -> {
					if (!Objects.equals(currentValue, originalValue)) {
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
		assertTrue(result.model().isRebuildTableModel());

		String resultColumnKey = model.getColumns().get(colIdx).getKey();
		assertEquals("data2", resultColumnKey);

		String resultValue = result.model().getRows().get(rowIdx).getValue(resultColumnKey, String.class);
		assertEquals("test", resultValue);
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.CellEdited.class, result.effect().get()));
	}

	@Test
	void shouldUpdateMultipleRows_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 1, "updated1"),
				new GenericTableViewMsg.CellEdited(2, 1, "updated3"),
				new GenericTableViewMsg.CellEdited(4, 1, "updated5"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model, result.model());
		assertTrue(result.model().isDirty());
		assertTrue(result.model().isRebuildTableModel());
		assertFalse(result.effect().isPresent(), "No effect should be generated for batch updates");

		assertEquals("updated1", result.model().getRows().get(0).getValue("data1", String.class));
		assertEquals("updated3", result.model().getRows().get(2).getValue("data1", String.class));
		assertEquals("updated5", result.model().getRows().get(4).getValue("data1", String.class));

		// Unedited values should remain unchanged
		assertEquals("test2", result.model().getRows().get(1).getValue("data1", String.class));
		assertEquals("test4", result.model().getRows().get(3).getValue("data1", String.class));
	}

	@Test
	void shouldUpdateMultipleColumns_onSameRow_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(1, 0, "new_data0"),
				new GenericTableViewMsg.CellEdited(1, 1, "new_data1"),
				new GenericTableViewMsg.CellEdited(1, 2, "new_data2"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertTrue(result.model().isDirty());

		RowData updatedRow = result.model().getRows().get(1);
		assertEquals("new_data0", updatedRow.getValue("data0", String.class));
		assertEquals("new_data1", updatedRow.getValue("data1", String.class));
		assertEquals("new_data2", updatedRow.getValue("data2", String.class));
	}

	@Test
	void shouldSkipOutOfBoundsRows_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 1, "valid_update"),
				new GenericTableViewMsg.CellEdited(99, 1, "invalid_row"),
				new GenericTableViewMsg.CellEdited(-1, 1, "negative_index"),
				new GenericTableViewMsg.CellEdited(5, 1, "exactly_at_bound"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals("valid_update", result.model().getRows().get(0).getValue("data1", String.class));
		assertEquals("test2", result.model().getRows().get(1).getValue("data1", String.class));
	}

	@Test
	void shouldSkipNoOpEdits_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();
		String currentValue = model.getRows().get(1).getValue("data1", String.class);

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(1, 1, currentValue),
				new GenericTableViewMsg.CellEdited(2, 2, "actually_updated"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Row 1 should NOT be marked as modified since value didn't change
		assertEquals("test2", result.model().getRows().get(1).getValue("data1", String.class));
		// But row 2 should be updated
		assertEquals("actually_updated", result.model().getRows().get(2).getValue("data2", String.class));

		// At least one row was actually modified, so dirty should be true
		assertTrue(result.model().isDirty());
	}

	@Test
	void shouldCalculateCorrectRowState_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 1, "modified_value"),
				new GenericTableViewMsg.CellEdited(1, 1, "test2"), // Same as original - should be NORMAL
				new GenericTableViewMsg.CellEdited(2, 1, "another_modified"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());

		// Row 0: Modified (different from original)
		assertEquals(RowState.MODIFIED, result.model().getRows().get(0).getState());

		// Row 1: Normal (value matches original snapshot)
		assertEquals(RowState.NORMAL, result.model().getRows().get(1).getState());

		// Row 2: Modified (different from original)
		assertEquals(RowState.MODIFIED, result.model().getRows().get(2).getState());

		// Row 3 & 4: Should remain NORMAL (untouched)
		assertEquals(RowState.NORMAL, result.model().getRows().get(3).getState());
		assertEquals(RowState.NORMAL, result.model().getRows().get(4).getState());
	}

	@Test
	void shouldPreserveFilters_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel().toBuilder().filterColumnKey("data0").filterValues(Set.of("1", "3"))
				.build();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 1, "updated1"),
				new GenericTableViewMsg.CellEdited(1, 1, "should_be_filtered"),
				new GenericTableViewMsg.CellEdited(2, 1, "updated3"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Visible rows should still only contain filtered data0 values
		assertEquals(2, result.model().getRows().size());

		// All rows should be in allRows regardless of filter
		assertEquals(5, result.model().getAllRows().size());

		// Filter settings preserved
		assertEquals("data0", result.model().getFilterColumnKey());
		assertEquals(Set.of("1", "3"), result.model().getFilterValues());
	}

	@Test
	void shouldMaintainAllRowsInAllRows_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 0, "updated1"),
				new GenericTableViewMsg.CellEdited(1, 1, "updated2"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Both rows and allRows should have same count (no filter applied)
		assertEquals(5, result.model().getRows().size());
		assertEquals(5, result.model().getAllRows().size());

		// Changes reflected in both collections
		assertEquals("updated1", result.model().getRows().get(0).getValue("data0", String.class));
		assertEquals("updated1", result.model().getAllRows().get(0).getValue("data0", String.class));
	}

	@Test
	void shouldMergeMultipleEditsOnSameRow_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		// Three edits targeting the same row with different columns
		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(1, 0, "first_edit"),
				new GenericTableViewMsg.CellEdited(1, 0, "second_edit"),
				new GenericTableViewMsg.CellEdited(1, 0, "final_edit"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Final value should be from the last edit
		assertEquals("final_edit", result.model().getRows().get(1).getValue("data0", String.class));
		// Other columns unchanged
		assertEquals("test2", result.model().getRows().get(1).getValue("data1", String.class));
		assertEquals("test2", result.model().getRows().get(1).getValue("data2", String.class));
	}

	@Test
	void shouldPreserveOriginalValues_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();
		List<Map<String, Object>> originalSnapshot = model.getOriginalSnapshot();

		List<GenericTableViewMsg.CellEdited> edits = List
				.of(new GenericTableViewMsg.CellEdited(2, 1, "only_column1_changed"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());

		// Original snapshot should remain unchanged
		assertEquals(originalSnapshot, result.model().getOriginalSnapshot());

		// Unedited cells in edited row should retain original values
		assertEquals("3", result.model().getRows().get(2).getValue("data0", String.class));
		assertEquals("only_column1_changed", result.model().getRows().get(2).getValue("data1", String.class));
		assertEquals("test3", result.model().getRows().get(2).getValue("data2", String.class));
	}

	@Test
	void shouldTrackDirtyStateAcrossMixedEdits_whenMultipleCellEdits() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(
				new GenericTableViewMsg.CellEdited(0, 0, "different_from_original"),
				new GenericTableViewMsg.CellEdited(1, 1, "test2"), // Same as original
				new GenericTableViewMsg.CellEdited(2, 2, "different_from_original"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Should be dirty because at least one row was actually modified
		assertTrue(result.model().isDirty());
	}

	@Test
	void shouldHandleNullValues_whenMultipleCellsEdited() {
		GenericTableViewModel model = baseModel();

		List<GenericTableViewMsg.CellEdited> edits = List.of(new GenericTableViewMsg.CellEdited(0, 1, null),
				new GenericTableViewMsg.CellEdited(1, 1, "valid_value"));
		GenericTableViewMsg msg = new GenericTableViewMsg.MultipleCellsEdited(edits);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		// Null value should be accepted
		assertNull(result.model().getRows().get(0).getValue("data1", String.class));
		assertEquals("valid_value", result.model().getRows().get(1).getValue("data1", String.class));
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
		assertTrue(result.model().isRebuildTableModel());

		RowData resultLastRow = result.model().getRows().get(result.model().getRows().size() - 1);
		RowData lastRow = model.getRows().get(model.getRows().size() - 1);
		assertNotSame(lastRow, resultLastRow);
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.AddRow.class, result.effect().get()));
	}

	@Test
	void shouldDeleteRowFromRows_whenRowDeleted() {
		GenericTableViewModel model = baseModel();
		int rowIdx = 2;
		GenericTableViewMsg msg = new GenericTableViewMsg.DeleteRows(List.of(model.getRows().get(2).getId()));

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertNotSame(model.getRows().size(), result.model().getRows().size());
		assertNotEquals("3", result.model().getRows().get(rowIdx).getValue("data0", String.class));
		assertTrue(result.model().isDirty());
		assertTrue(result.model().isRebuildTableModel());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.DeleteRows.class, result.effect().get()));
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
		assertTrue(result.model().isRebuildTableModel());
	}

	@Test
	void shouldUpdateSelectedIndex_whenChangeSelection() {
		GenericTableViewModel model = baseModel();
		Set<String> selectedRows = Set.of(model.getRows().get(1).getId(), model.getRows().get(2).getId());
		GenericTableViewMsg msg = new GenericTableViewMsg.ChangeSelection(selectedRows);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(selectedRows.size(), result.model().getSelectedRows().size());
		assertEquals(selectedRows, result.model().getSelectedRows());
		assertFalse(result.model().isRebuildTableModel());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.Selection.class, result.effect().get()));
	}

	@Test
	void shouldToggleBetweenBooleanValues_whenToggleColumn() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.ToggleColumn("data1");

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertTrue(result.model().getColumns().get(0).isVisible());
		assertFalse(result.model().getColumns().get(1).isVisible());
		assertTrue(result.model().getColumns().get(2).isVisible());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.StoreVisibleColulmns.class, result.effect().get()),
				() -> {
					GenericTableViewEffect.StoreVisibleColulmns effect = (GenericTableViewEffect.StoreVisibleColulmns) result
							.effect().get();
					assertTrue(effect.visibleColumns().containsAll(List.of("data0", "data2")));
				});
		assertTrue(result.model().isRebuildTableModel());

		GenericTableViewMsg msg2 = new GenericTableViewMsg.ToggleColumn("data1");

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result2 = GenericTableViewUpdate.update(msg2,
				result.model());

		assertNotNull(result2.model());
		assertTrue(result2.model().getColumns().get(0).isVisible());
		assertTrue(result2.model().getColumns().get(1).isVisible());
		assertTrue(result2.model().getColumns().get(2).isVisible());
		assertAll(() -> assertTrue(result2.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.StoreVisibleColulmns.class, result2.effect().get()),
				() -> {
					GenericTableViewEffect.StoreVisibleColulmns effect = (GenericTableViewEffect.StoreVisibleColulmns) result2
							.effect().get();
					assertTrue(effect.visibleColumns().containsAll(List.of("data0", "data1", "data2")));
				});
		assertTrue(result2.model().isRebuildTableModel());
	}

	@Test
	void shouldUpdateSorterValues_whenChangeSortOrder() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.ChangeSortOrder(Map.of("data1", SortOrder.ASCENDING));

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(SortOrder.ASCENDING, result.model().getTableConfig().getSortKeys().get("data1"));
		assertFalse(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldFilterRows_whenApplyRowFilter() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.ApplyRowFilter("data0", Set.of("2", "4"), false);

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(2, result.model().getRows().size());
		assertTrue(result.model().getRows().stream()
				.allMatch(row -> Set.of("2", "4").contains(row.getValue("data0", String.class))));
		assertEquals(5, result.model().getAllRows().size());
		assertEquals(0, result.model().getSelectedRows().size());
		assertTrue(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldFilterRowsAndSelect_whenApplyRowFilterWithSelection() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.ApplyRowFilter("data0", Set.of("2", "4"), true);
		Set<String> expectedSelectedRows = Set.of(model.getRows().get(1).getId(), model.getRows().get(3).getId());

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(2, result.model().getRows().size());
		assertTrue(result.model().getRows().stream()
				.allMatch(row -> Set.of("2", "4").contains(row.getValue("data0", String.class))));
		assertEquals(5, result.model().getAllRows().size());
		assertEquals(2, result.model().getSelectedRows().size());
		assertEquals(expectedSelectedRows, result.model().getSelectedRows());
		assertTrue(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateColumnsWidths_whenResizeColumns() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.ResizeColumns(Map.of("data0", 25, "data1", 23, "data2", 25));

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg, model);

		assertNotNull(result.model());
		assertEquals(25, result.model().getColumns().get(0).getPrefferedWidth());
		assertEquals(23, result.model().getColumns().get(1).getPrefferedWidth());
		assertEquals(25, result.model().getColumns().get(2).getPrefferedWidth());
		assertFalse(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldInvertSelection_whenInvertSelection() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.InvertSelection();

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withSelectedRows(Set.of(model.getRows().get(0).getId(), model.getRows().get(2).getId())));

		assertNotNull(result.model());
		assertEquals(3, result.model().getSelectedRows().size());
		assertTrue(result.model().getSelectedRows().containsAll(Set.of(model.getRows().get(1).getId(),
				model.getRows().get(3).getId(), model.getRows().get(4).getId())));
		assertFalse(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.Selection.class, result.effect().get()));
	}

	@Test
	void shouldSelectionBeEmpty_whenInvertSelectionWithAllSelected() {
		GenericTableViewModel model = baseModel();
		GenericTableViewMsg msg = new GenericTableViewMsg.InvertSelection();

		Set<String> selectedRows = new HashSet<>();
		for (RowData data : model.getRows()) {
			selectedRows.add(data.getId());
		}

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result = GenericTableViewUpdate.update(msg,
				model.withSelectedRows(selectedRows));

		assertNotNull(result.model());
		assertEquals(0, result.model().getSelectedRows().size());
		assertFalse(result.model().isRebuildTableModel());
		assertFalse(result.model().isDirty());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(GenericTableViewEffect.Selection.class, result.effect().get()));
	}
}
