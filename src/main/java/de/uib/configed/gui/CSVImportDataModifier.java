/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.csv.CSVParser;
import de.uib.configed.csv.CSVReader;
import de.uib.configed.csv.exceptions.CSVException;
import de.uib.configed.csv.exceptions.CSVFieldCountException;
import de.uib.configed.csv.exceptions.CSVParserException;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapSource;
import de.uib.utilities.table.provider.TableSource;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;

public class CSVImportDataModifier {
	private GenTableModel model;
	private String csvFile;
	private List<String> columnNames;
	private List<String> hiddenColumns;

	public CSVImportDataModifier(String csvFile, List<String> columnNames) {
		this.csvFile = csvFile;
		this.columnNames = columnNames;
		this.hiddenColumns = new ArrayList<>();
	}

	public boolean updateTable(CSVParser parser, int startLine, PanelGenEditTable thePanel) {
		model = updateModel(thePanel, parser, startLine);

		if (model == null) {
			return false;
		}

		thePanel.setTableModel(model);

		hideEmptyColumns(thePanel);
		makeColumnsEditable(model, columnNames);
		disableRowSorting(thePanel);

		return true;
	}

	private GenTableModel updateModel(PanelGenEditTable thePanel, CSVParser parser, int startLine) {
		try {
			CSVReader reader = null;

			if (parser.getFormat().hasHeader()) {
				reader = new CSVReader(new FileReader(csvFile, StandardCharsets.UTF_8), parser, startLine);
			} else {
				reader = new CSVReader(new FileReader(csvFile, StandardCharsets.UTF_8), parser, startLine, columnNames);
			}

			List<Map<String, Object>> csvData = reader.readAll();
			reader.close();

			model = createModel(thePanel, csvData, columnNames, parser);

			if (csvData.isEmpty()) {
				model.deleteRows(new int[model.getRows().size()]);
				return model;
			}

			for (int i = 0; i < csvData.size(); i++) {
				if (model.getRowCount() != 0) {
					model.updateRowValues(i, csvData.get(i));
				}
			}
		} catch (IOException ex) {
			Logging.error(this, "Failed to read CSV file", ex);
		} catch (CSVException ex) {
			String title = "";
			StringBuilder message = new StringBuilder("");

			if (ex instanceof CSVParserException) {
				title = Configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.title");
				message.append(Configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.message"));
			} else if (ex instanceof CSVFieldCountException) {
				title = Configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.title");
				message.append(Configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.message"));
			} else {
				Logging.error(this, "unexpected CSVException of type " + ex.getClass());
			}

			FTextArea fInfo = new FTextArea(ConfigedMain.getMainFrame(), title + " (" + Globals.APPNAME + ") ", false,
					new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);

			fInfo.setMessage(message.toString());
			fInfo.setAlwaysOnTop(true);
			fInfo.setVisible(true);

			return null;
		}

		return model;
	}

	private GenTableModel createModel(PanelGenEditTable thePanel, List<Map<String, Object>> csvData,
			List<String> columnNames, CSVParser parser) {
		List<String> classNames = new ArrayList<>();
		populateClassNames(classNames, columnNames);

		Map<String, Map<String, Object>> theSourceMap = new HashMap<>();
		populateSourceMap(theSourceMap, csvData);

		ArrayList<TableEditItem> updateCollection = new ArrayList<>();
		TableSource source = new MapSource(columnNames, classNames, theSourceMap, false);
		MapTableUpdateItemFactory updateItemFactory = new MapTableUpdateItemFactory(columnNames, 0);

		GenTableModel createdModel = new GenTableModel(updateItemFactory, // updateItemFactory,
				new DefaultTableProvider(source), // tableProvider
				0,
				// final columns int array
				new int[] {},
				// table model listener
				thePanel,
				// ArrayList<TableEditItem> updates
				updateCollection);

		updateItemFactory.setSource(createdModel);

		CSVFileDataUpdater updater = new CSVFileDataUpdater(createdModel, csvFile, parser.getFormat(), hiddenColumns);
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

	private static void populateClassNames(List<String> classNames, List<String> columnNames) {
		if (!classNames.isEmpty()) {
			classNames.clear();
		}

		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}
	}

	private void hideEmptyColumns(PanelGenEditTable thePanel) {
		hiddenColumns.clear();

		for (int i = 0; i < thePanel.getTheTable().getColumnCount(); i++) {
			if (isColumnEmpty(i, thePanel)) {
				TableColumn column = thePanel.getTheTable().getColumnModel().getColumn(i);
				column.setMinWidth(0);
				column.setMaxWidth(0);
				column.setResizable(false);
				hiddenColumns.add((String) column.getHeaderValue());
			}
		}
	}

	private boolean isColumnEmpty(int column, PanelGenEditTable thePanel) {
		int emptyRows = 0;
		List<List<Object>> rows = model.getRows();

		for (int row = 0; row < rows.size(); row++) {
			String value = thePanel.getTheTable().getValueAt(row, column).toString();

			if (value.isEmpty()) {
				emptyRows++;
			}
		}

		return emptyRows == rows.size();
	}

	private static void disableRowSorting(PanelGenEditTable thePanel) {
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(thePanel.getTheTable().getModel());

		int columnCount = thePanel.getTheTable().getColumnCount();

		for (int i = 0; i < columnCount; i++) {
			rowSorter.setSortable(i, false);
		}

		thePanel.getTheTable().setRowSorter(rowSorter);
	}

	private static void makeColumnsEditable(GenTableModel model, List<String> columnNames) {
		int[] editableColumns = new int[columnNames.size()];

		for (int i = 0; i < columnNames.size(); i++) {
			editableColumns[i] = i;
		}

		model.setEditableColumns(editableColumns);
	}

	public List<List<Object>> getRows() {
		return model.getRows();
	}
}
