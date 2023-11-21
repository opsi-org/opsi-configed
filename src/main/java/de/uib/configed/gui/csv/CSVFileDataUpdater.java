/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.updates.MapBasedUpdater;

public class CSVFileDataUpdater implements MapBasedUpdater {
	private String csvFile;
	private GenTableModel model;
	private org.apache.commons.csv.CSVFormat format;
	private List<String> hiddenColumns;

	public CSVFileDataUpdater(GenTableModel model, String csvFile, org.apache.commons.csv.CSVFormat format,
			List<String> hiddenColumns) {
		this.model = model;
		this.csvFile = csvFile;
		this.format = format;
		this.hiddenColumns = hiddenColumns;
	}

	@Override
	public String sendUpdate(Map<String, Object> rowmap) {
		try (BufferedWriter writer = Files.newBufferedWriter(new File(csvFile).toPath(), StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(writer, format)) {
			// Create a copy of columnNames List to avoid global modification
			// of columnNames List, that exists in GenTableModel class.
			List<String> columns = new ArrayList<>(model.getColumnNames());
			columns.removeAll(hiddenColumns);
			printer.printRecord(columns);

			for (List<Object> originalRow : model.getRows()) {
				List<Object> modifiedRow = modifyRowAccordingToHeaders(originalRow);
				printer.printRecord(modifiedRow);
			}
		} catch (IOException e) {
			Logging.error("Unable to write to the CSV file", e);
			return null;
		}

		return "";
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
