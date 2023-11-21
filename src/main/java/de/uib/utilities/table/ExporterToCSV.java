/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;

public class ExporterToCSV extends AbstractExportTable {
	private static final String CSV_SEPARATOR = ";";
	private static final Character STRING_DELIMITER = '"';
	private static final String THIS_EXTENSION = ".csv";

	public ExporterToCSV(JTable table) {
		super(table, null);

		extensionFilter = new FileNameExtensionFilter("CSV", "csv");

		defaultExportFilename = "export.csv";
		extension = THIS_EXTENSION;
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {
		Logging.info(this, "toCSV fileName, onlySelectedRows, csvSep " + "\"" + fileName + "\", " + onlySelectedRows
				+ "\", " + "\"" + CSV_SEPARATOR + "\"");

		Boolean selectedOnly = checkSelection(onlySelectedRows);
		if (selectedOnly == null) {
			return;
		}

		if (theTable.getModel() instanceof GenTableModel) {
			setClassNames(((GenTableModel) theTable.getModel()).getClassNames());
		}

		if ((fileName = checkFile(fileName, extensionFilter)) != null) {
			writeToCSVFile(fileName, onlySelectedRows);
		}
	}

	private void writeToCSVFile(String fileName, boolean selectedOnly) {
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setQuote(STRING_DELIMITER).setQuoteMode(QuoteMode.ALL)
				.setDelimiter(CSV_SEPARATOR).build();
		try (BufferedWriter writer = Files.newBufferedWriter(new File(fileName).toPath(), StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
			writeHeader(printer);
			writeRows(printer, selectedOnly);
		} catch (IOException ex) {
			Logging.error(Configed.getResourceValue("ExportTable.error"), ex);
		}
	}

	private void writeHeader(CSVPrinter printer) throws IOException {
		List<String> headers = new ArrayList<>();
		for (int colI = 0; colI < theTable.getColumnCount(); colI++) {
			headers.add(theTable.getColumnName(colI));
		}
		printer.printRecord(headers);
	}

	private void writeRows(CSVPrinter printer, boolean selectedOnly) throws IOException {
		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
			Logging.debug(this, "toCsv, handle row " + rowI + " selected " + theTable.isRowSelected(rowI)
					+ " selectedOnly " + selectedOnly);
			List<String> row = new ArrayList<>();
			if (selectedOnly && !theTable.isRowSelected(rowI)) {
				continue;
			}
			for (int colI = 0; colI < theTable.getColumnCount(); colI++) {
				if (theTable.getValueAt(rowI, colI) != null) {
					String val = "" + theTable.getValueAt(rowI, colI);
					val = removeStringDelimiter(val);
					val = removeSeparatorChar(val);
					row.add(val);
				}
			}
			if (!row.isEmpty()) {
				printer.printRecord(row);
			}
		}
	}

	private static String removeStringDelimiter(Object value) {
		if (value == null) {
			return "";
		}

		return ((String) value).replace(STRING_DELIMITER, '\'');
	}

	private static String removeSeparatorChar(Object value) {
		if (value == null) {
			return "";
		}

		return ((String) value).replace(CSV_SEPARATOR, "\\" + CSV_SEPARATOR);
	}
}
