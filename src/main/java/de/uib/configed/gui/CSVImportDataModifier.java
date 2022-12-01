package de.uib.configed.gui;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.RowSorter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.csv.CSVFormat;
import de.uib.configed.csv.CSVParser;
import de.uib.configed.csv.CSVReader;
import de.uib.configed.csv.CSVWriter;
import de.uib.configed.csv.exceptions.CSVException;
import de.uib.configed.csv.exceptions.CSVFieldCountException;
import de.uib.configed.csv.exceptions.CSVParserException;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.MapSource;
import de.uib.utilities.table.provider.TableSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class CSVImportDataModifier {
	private GenTableModel model;
	private String csvFile;
	private ArrayList<String> columnNames;
	private ArrayList<String> hiddenColumns;

	public CSVImportDataModifier(String csvFile, ArrayList<String> columnNames) {
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

	public GenTableModel updateModel(PanelGenEditTable thePanel, CSVParser parser, int startLine) {
		try {
			CSVReader reader = null;

			if (parser.getFormat().hasHeader()) {
				reader = new CSVReader(new FileReader(csvFile), parser, startLine);
			} else {
				reader = new CSVReader(new FileReader(csvFile), parser, startLine, columnNames);
			}

			java.util.List<Map<String, Object>> csvData = reader.readAll();
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
			logging.error(this, "Failed to read CSV file");
		} catch (CSVException ex) {
			String title = "";
			StringBuffer message = new StringBuffer("");

			if (ex instanceof CSVParserException) {
				title = configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.title");
				message.append(configed.getResourceValue("CSVImportDataDialog.infoSyntaxErrorsOccurred.message"));
			} else if (ex instanceof CSVFieldCountException) {
				title = configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.title");
				message.append(configed.getResourceValue("CSVImportDataDialog.infoUnequalLineLength.message"));
			}

			FTextArea fInfo = new FTextArea(Globals.mainFrame, title + " (" + Globals.APPNAME + ") ", false,
					new String[] { configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);

			fInfo.setMessage(message.toString());
			fInfo.setAlwaysOnTop(true);
			fInfo.setVisible(true);

			return null;
		}

		return model;
	}

	private GenTableModel createModel(PanelGenEditTable thePanel, java.util.List<Map<String, Object>> csvData,
			ArrayList<String> columnNames, CSVParser parser) {
		ArrayList<String> classNames = new ArrayList<>();
		populateClassNames(classNames, columnNames);

		Map<String, Map> theSourceMap = new HashMap<>();
		populateSourceMap(theSourceMap, csvData, columnNames);

		TableUpdateCollection updateCollection = new TableUpdateCollection();
		TableSource source = new MapSource(columnNames, classNames, theSourceMap, false);
		MapTableUpdateItemFactory updateItemFactory = new MapTableUpdateItemFactory(columnNames, classNames, 0);

		GenTableModel model = new GenTableModel(updateItemFactory, // updateItemFactory,
				new de.uib.utilities.table.provider.DefaultTableProvider(source), // tableProvider
				0, // keycol
				new int[] {}, // final columns int array
				thePanel, // table model listener
				updateCollection // TableUpdateCollection updates
		);

		updateItemFactory.setSource(model);

		CSVFileDataUpdater updater = new CSVFileDataUpdater(model, csvFile, parser.getFormat(), hiddenColumns);
		MapItemsUpdateController updateController = new MapItemsUpdateController(thePanel, model, updater,
				updateCollection);
		thePanel.setUpdateController(updateController);

		return model;
	}

	private class CSVFileDataUpdater extends MapBasedUpdater {
		private String csvFile;
		private GenTableModel model;
		private CSVFormat format;
		private ArrayList<String> hiddenColumns;

		public CSVFileDataUpdater(GenTableModel model, String csvFile, CSVFormat format,
				ArrayList<String> hiddenColumns) {
			this.model = model;
			this.csvFile = csvFile;
			this.format = format;
			this.hiddenColumns = hiddenColumns;
		}

		@Override
		public String sendUpdate(Map<String, Object> rowmap) {
			CSVWriter writer = null;

			try {
				writer = new CSVWriter(new FileWriter(csvFile), format);

				// Create a copy of columnNames ArrayList to avoid global modification
				// of columnNames ArrayList, that exists in GenTableModel class.
				ArrayList<String> columns = new ArrayList<>(model.getColumnNames());
				columns.removeAll(hiddenColumns);
				writer.write(columns);

				ArrayList<ArrayList<Object>> rows = model.getRows();

				for (ArrayList<Object> originalRow : rows) {
					ArrayList<Object> modifiedRow = modifyRowAccordingToHeaders(originalRow);
					writer.write(modifiedRow);
				}

				writer.close();
			} catch (IOException e) {
				logging.error("Unable to write to the CSV file", e);
			}

			return writer.toString();
		}

		private ArrayList<Object> modifyRowAccordingToHeaders(ArrayList<Object> row) {
			ArrayList<Object> result = new ArrayList<>();
			Iterator<String> columnNameIter = model.getColumnNames().iterator();
			Iterator<Object> rowIter = row.iterator();

			while (columnNameIter.hasNext() && rowIter.hasNext()) {
				String columnName = columnNameIter.next();
				String rowValue = rowIter.next().toString();

				if (!hiddenColumns.contains(columnName)) {
					result.add(rowValue);
				}
			}

			return result;
		}

		@Override
		public boolean sendDelete(Map<String, Object> rowmap) {
			return false;
		}
	}

	private void populateSourceMap(Map<String, Map> theSourceMap, java.util.List<Map<String, Object>> data,
			ArrayList<String> columnNames) {
		int id = 0;

		for (Map<String, Object> line : data) {
			theSourceMap.put(String.valueOf(id++), line);
		}
	}

	private void populateClassNames(ArrayList<String> classNames, ArrayList<String> columnNames) {
		if (classNames.size() != 0) {
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
		ArrayList<ArrayList<Object>> rows = model.getRows();

		for (int row = 0; row < rows.size(); row++) {
			String value = thePanel.getTheTable().getValueAt(row, column).toString();

			if (value.isEmpty()) {
				emptyRows++;
			}
		}

		return emptyRows == rows.size();
	}

	private void disableRowSorting(PanelGenEditTable thePanel) {
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(thePanel.getTheTable().getModel());
		java.util.List<RowSorter.SortKey> sortKeys = thePanel.getRowSorter().getSortKeys();

		int columnCount = thePanel.getTheTable().getColumnCount();

		for (int i = 0; i < columnCount; i++) {
			rowSorter.setSortable(i, false);
		}

		thePanel.getTheTable().setRowSorter(rowSorter);
	}

	private void makeColumnsEditable(GenTableModel model, ArrayList<String> columnNames) {
		int[] editableColumns = new int[columnNames.size()];

		for (int i = 0; i < columnNames.size(); i++) {
			editableColumns[i] = i;
		}

		model.setEditableColumns(editableColumns);
	}

	public ArrayList<ArrayList<Object>> getRows() {
		return model.getRows();
	}
}
