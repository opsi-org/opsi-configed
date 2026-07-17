package de.uib.configed.gui.features.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class GenericTableModelTest {
	private static Map<String, Object> row(String data0, String data1, String data2) {
		Map<String, Object> row = new HashMap<>();
		row.put("data0", data0);
		row.put("data1", data1);
		row.put("data2", data2);
		return row;
	}

	@Test
	void shouldReportCorrectColumnCount_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "Col 0", false, true, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data1", "Col 1", false, false, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data2", "Col 2", false, true, false, 100, 100, null, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("val0", "val1", "val2"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot, null);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		}, row -> true);

		assertEquals(2, tableModel.getColumnCount(), "Should only count visible columns");
	}

	@Test
	void shouldReturnCorrectColumnNames_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, false, 100, 100, null, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("val0", "val1", "val2"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot, null);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		}, row -> true);

		assertEquals("A", tableModel.getColumnName(0), "First visible column name");
		assertEquals("C", tableModel.getColumnName(1), "Second visible column name (skipped hidden one)");
	}

	@Test
	void shouldReturnCorrectDataValues_whenSomeColumnsAreHidden() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, false, 100, 100, null, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("A", "B", "C"));

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot, null);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		}, row -> true);

		assertEquals("A", tableModel.getValueAt(0, 0), "Value for first visible column");
		assertEquals("C", tableModel.getValueAt(0, 1), "Value for second visible column (should skip hidden data1)");
	}

	@Test
	void shouldHandleMultipleHiddenColumns() {
		List<TableColumnConfig> columns = new ArrayList<>();
		columns.add(new TableColumnConfig("data0", "A", false, true, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data1", "B", false, false, false, 100, 100, null, null, null));
		columns.add(new TableColumnConfig("data2", "C", false, true, false, 100, 100, null, null, null));

		List<Map<String, Object>> snapshot = new ArrayList<>();
		snapshot.add(row("1", "2", "3"));

		Map<String, Object> rowData = new HashMap<>();
		rowData.put("data0", "1");
		rowData.put("data1", "2");
		rowData.put("data2", "3");
		snapshot.add(rowData);

		List<RowData> rows = RowData.fromOriginalSnapshot(snapshot, null);

		GenericTableViewModel model = GenericTableViewModel.builder().columns(columns).rows(rows)
				.originalSnapshot(snapshot).build();

		GenericTableModel tableModel = new GenericTableModel(model, msg -> {
		}, row -> true);

		assertEquals(2, tableModel.getColumnCount());
		assertEquals("A", tableModel.getColumnName(0));
		assertEquals("C", tableModel.getColumnName(1));
		assertEquals("1", tableModel.getValueAt(0, 0));
		assertEquals("3", tableModel.getValueAt(0, 1));
	}
}
