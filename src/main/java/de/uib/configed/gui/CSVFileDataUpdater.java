/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uib.configed.csv.CSVFormat;
import de.uib.configed.csv.CSVWriter;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.updates.MapBasedUpdater;

public class CSVFileDataUpdater implements MapBasedUpdater {
	private String csvFile;
	private GenTableModel model;
	private CSVFormat format;
	private List<String> hiddenColumns;

	public CSVFileDataUpdater(GenTableModel model, String csvFile, CSVFormat format, List<String> hiddenColumns) {
		this.model = model;
		this.csvFile = csvFile;
		this.format = format;
		this.hiddenColumns = hiddenColumns;
	}

	@Override
	public String sendUpdate(Map<String, Object> rowmap) {
		CSVWriter writer = null;

		try {
			writer = new CSVWriter(new FileWriter(csvFile, StandardCharsets.UTF_8), format);

			// Create a copy of columnNames List to avoid global modification
			// of columnNames List, that exists in GenTableModel class.
			List<String> columns = new ArrayList<>(model.getColumnNames());
			columns.removeAll(hiddenColumns);
			writer.write(columns);

			List<List<Object>> rows = model.getRows();

			for (List<Object> originalRow : rows) {
				List<Object> modifiedRow = modifyRowAccordingToHeaders(originalRow);
				writer.write(modifiedRow);
			}

			writer.close();
		} catch (IOException e) {
			Logging.error("Unable to write to the CSV file", e);
			return null;
		}

		return writer.toString();
	}

	private List<Object> modifyRowAccordingToHeaders(List<Object> row) {
		List<Object> result = new ArrayList<>();
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
