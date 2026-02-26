/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

public class CSVImportDataModifier {
	private static final List<String> IMPORTANT_HEADER_NAMES = List.of(HostInfo.HOSTNAME_KEY, HostInfo.CSV_DOMAIN_KEY,
			HostInfo.DEPOT_OF_CLIENT_KEY, HostInfo.CLIENT_MAC_ADDRESS_KEY);

	private GenTableModel model;
	private String csvFile;
	private Set<String> columnNames;
	private List<String> hiddenColumns;

	public CSVImportDataModifier(String csvFile, Set<String> columnNames) {
		this.csvFile = csvFile;
		this.columnNames = columnNames;
		this.hiddenColumns = new ArrayList<>();
	}

	public boolean updateTable(CSVFormat format, int startLine, PanelGenEdit thePanel) {
		model = updateModel(format, startLine, thePanel);
		if (model == null) {
			Logging.warning(this, "Failed to update table model");
			return false;
		}
		thePanel.setTableModel(model);

		hideEmptyColumns(thePanel);
		makeColumnsEditable(model, columnNames);
		disableRowSorting(thePanel);

		return true;
	}

	private GenTableModel updateModel(CSVFormat format, int startLine, PanelGenEdit thePanel) {
		List<Map<String, Object>> csvData = extractDataFromCSV(format, startLine);
		if (csvData == null) {
			return null;
		}
		model = createModel(thePanel, csvData, new ArrayList<>(columnNames), format);

		if (csvData.isEmpty()) {
			model.deleteRows(new int[model.getRows().size()]);
			return model;
		}

		for (int i = 0; i < csvData.size(); i++) {
			if (model.getRowCount() != 0) {
				model.updateRowValues(i, csvData.get(i));
			}
		}

		return model;
	}

	@SuppressWarnings({ "java:S135", "java:S1168" })
	private List<Map<String, Object>> extractDataFromCSV(CSVFormat format, int startLine) {
		format = format.builder().setCommentMarker('#').setHeader().get();
		List<Map<String, Object>> csvData = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(new File(csvFile).toPath(), StandardCharsets.UTF_8);
				CSVParser parser = CSVParser.parse(reader, format)) {
			List<String> headerNames = parser.getHeaderNames();

			if (!headerNames.containsAll(IMPORTANT_HEADER_NAMES)) {
				StringBuilder message = new StringBuilder();
				message.append(Configed.getResourceValue("CSVImportDataDialog.missingRequiredHeaderNames.message"));
				message.append("\n");
				message.append(" " + IMPORTANT_HEADER_NAMES.toString().replace("[", "").replace("]", ""));
				JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), message,
						Configed.getResourceValue("CSVImportDataDialog.missingRequiredHeaderNames.title"),
						JOptionPane.ERROR_MESSAGE);

				return null;
			}

			for (CSVRecord csvRecord : parser.getRecords()) {
				if (!csvRecord.isConsistent()) {
					JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.message"),
							Configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.title"),
							JOptionPane.ERROR_MESSAGE);

					csvData = null;
					break;
				}
				if (csvRecord.getRecordNumber() < startLine) {
					continue;
				}
				csvData.add(new HashMap<>(csvRecord.toMap()));
			}
		} catch (IOException | UncheckedIOException ex) {
			Logging.warning(this, ex, "Failed to read CSV file");
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.message"),
					Configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.title"),
					JOptionPane.ERROR_MESSAGE);

			csvData = null;
		}
		return csvData;
	}

	private GenTableModel createModel(PanelGenEdit thePanel, List<Map<String, Object>> csvData,
			List<String> columnNames, CSVFormat format) {
		Logging.info(this, "createModel, csvData: ", csvData);
		Map<String, Map<String, Object>> theSourceMap = new HashMap<>();
		populateSourceMap(theSourceMap, csvData);

		List<MapBasedTableEditItem> updateCollection = new ArrayList<>();
		MapTableUpdateItemFactory updateItemFactory = new MapTableUpdateItemFactory(columnNames);

		GenTableModel createdModel = new GenTableModel(updateItemFactory,
				DefaultTableProvider.createWithMapSource(columnNames, theSourceMap), 0, new int[] {}, thePanel,
				updateCollection);

		updateItemFactory.setSource(createdModel);

		CSVFileDataUpdater updater = new CSVFileDataUpdater(createdModel, csvFile, format, hiddenColumns);
		MapItemsUpdateController updateController = new MapItemsUpdateController(thePanel, createdModel, updater,
				updateCollection);
		thePanel.setUpdateController(updateController);

		return createdModel;
	}

	private static void populateSourceMap(Map<String, Map<String, Object>> theSourceMap,
			List<Map<String, Object>> data) {
		int id = 0;

		for (Map<String, Object> line : data) {
			theSourceMap.put(String.valueOf(id), line);
			id++;
		}
	}

	private void hideEmptyColumns(PanelGenEdit thePanel) {
		hiddenColumns.clear();

		for (int i = 0; i < thePanel.getGenEditTable().getColumnCount(); i++) {
			if (isColumnEmpty(i, thePanel)) {
				TableColumn column = thePanel.getGenEditTable().getColumnModel().getColumn(i);
				column.setMinWidth(0);
				column.setMaxWidth(0);
				column.setResizable(false);
				hiddenColumns.add((String) column.getHeaderValue());
			}
		}
	}

	private boolean isColumnEmpty(int column, PanelGenEdit thePanel) {
		int emptyRows = 0;
		List<List<Object>> rows = model.getRows();

		for (int row = 0; row < rows.size(); row++) {
			String value = thePanel.getGenEditTable().getValueAt(row, column).toString();

			if (value.isEmpty()) {
				emptyRows++;
			}
		}

		return emptyRows == rows.size();
	}

	private static void disableRowSorting(PanelGenEdit thePanel) {
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(thePanel.getGenEditTable().getModel());

		int columnCount = thePanel.getGenEditTable().getColumnCount();

		for (int i = 0; i < columnCount; i++) {
			rowSorter.setSortable(i, false);
		}

		thePanel.getGenEditTable().setRowSorter(rowSorter);
	}

	private static void makeColumnsEditable(GenTableModel model, Set<String> columnNames) {
		int[] editableColumns = new int[columnNames.size()];

		for (int i = 0; i < columnNames.size(); i++) {
			editableColumns[i] = i;
		}

		model.setEditableColumns(editableColumns);
	}

	public List<Map<String, Object>> getRowsAsListOfMaps() {
		return model.getRows().parallelStream().map(this::buildMapFromRow).toList();
	}

	private Map<String, Object> buildMapFromRow(List<Object> row) {
		Map<String, Object> map = new HashMap<>(model.getColumnNames().size());
		for (int i = 0; i < model.getColumnNames().size(); i++) {
			if (model.getColumnNames().get(i).equals(HostInfo.CSV_GROUPS_KEY)) {
				map.put(model.getColumnNames().get(i), getGroupsFromObject(row.get(i)));
			} else {
				map.put(model.getColumnNames().get(i), row.get(i));
			}
		}
		return map;
	}

	private static List<String> getGroupsFromObject(Object groups) {
		if (groups == null || ((String) groups).isEmpty()) {
			return List.of();
		} else if (!((String) groups).contains(",")) {
			return List.of((String) groups);
		} else {
			return Arrays.asList(((String) groups).split(","));
		}
	}

	public static List<String> getImportantHeaders() {
		return IMPORTANT_HEADER_NAMES;
	}
}
