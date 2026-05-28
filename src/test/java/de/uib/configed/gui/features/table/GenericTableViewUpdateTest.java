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

import javax.swing.SortOrder;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.table.GenericTableViewComponent.GenericTableModel;
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
		columns.add(new TableColumnConfig("data0", "data0", false, true, 0, 0, null, null));
		columns.add(new TableColumnConfig("data1", "data1", false, true, 0, 0, null, null));
		columns.add(new TableColumnConfig("data2", "data2", false, true, 0, 0, null, null));

		return GenericTableViewModel.builder().originalSnapshot(originalSnapshot).rows(rows).columns(columns)
				.tableConfig(TableConfig.builder().build())
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
		assertTrue(result.model().isRebuildTableModel());

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
		assertTrue(result.model().isRebuildTableModel());

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
		assertFalse(result.model().isRebuildTableModel());

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
		assertTrue(result.model().isRebuildTableModel());
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
		assertTrue(result.model().isRebuildTableModel());
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
		assertTrue(result.model().isRebuildTableModel());
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
		assertTrue(result.model().isRebuildTableModel());
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
		assertFalse(result.effect().isPresent());
		assertTrue(result.model().isRebuildTableModel());

		GenericTableViewMsg msg2 = new GenericTableViewMsg.ToggleColumn("data1");

		UpdateResult<GenericTableViewModel, GenericTableViewEffect> result2 = GenericTableViewUpdate.update(msg2,
				result.model());

		assertNotNull(result2.model());
		assertTrue(result2.model().getColumns().get(0).isVisible());
		assertTrue(result2.model().getColumns().get(1).isVisible());
		assertTrue(result2.model().getColumns().get(2).isVisible());
		assertFalse(result2.effect().isPresent());
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
	void shouldReportCorrectColumnCount_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "Col 0", false, true, 100, 100, null, null));
		columns.add(new TableColumnConfig("data1", "Col 1", false, false, 100, 100, null, null));
		columns.add(new TableColumnConfig("data2", "Col 2", false, true, 100, 100, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("val0", "val1", "val2"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		});

		assertEquals(2, tableModel.getColumnCount(), "Should only count visible columns");
	}

	@Test
	void shouldReturnCorrectColumnNames_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, 100, 100, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, 100, 100, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, 100, 100, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("val0", "val1", "val2"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		});

		assertEquals("A", tableModel.getColumnName(0), "First visible column name");
		assertEquals("C", tableModel.getColumnName(1), "Second visible column name (skipped hidden one)");
	}

	@Test
	void shouldReturnCorrectDataValues_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, 100, 100, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, 100, 100, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, 100, 100, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("A", "B", "C"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		});

		assertEquals("A", tableModel.getValueAt(0, 0), "Value for first visible column");
		assertEquals("C", tableModel.getValueAt(0, 1), "Value for second visible column (should skip hidden data1)");
	}

	@Test
	void shouldHandleMultipleHiddenColumns() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, 100, 100, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, 100, 100, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, 100, 100, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("1", "2", "3"));

		Map<String, Object> rowData = new HashMap<>();
		rowData.put("data0", "1");
		rowData.put("data1", "2");
		rowData.put("data2", "3");
		snapshot.add(rowData);

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		});

		assertEquals(2, tableModel.getColumnCount());
		assertEquals("A", tableModel.getColumnName(0));
		assertEquals("C", tableModel.getColumnName(1));
		assertEquals("1", tableModel.getValueAt(0, 0));
		assertEquals("3", tableModel.getValueAt(0, 1));
	}
}
