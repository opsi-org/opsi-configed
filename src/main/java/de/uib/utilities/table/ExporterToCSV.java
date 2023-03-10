package de.uib.utilities.table;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;

public class ExporterToCSV extends AbstractExportTable {

	protected static final String CSV_SEPARATOR = ";";
	public static final Character STRING_DELIMITER = '"';
	protected static final String THIS_EXTENSION = ".csv";

	public ExporterToCSV(JTable table, List<String> classNames) {
		super(table, classNames);
		extensionFilter = new FileNameExtensionFilter("CSV", "csv");

		defaultExportFilename = "export.csv";
		extension = THIS_EXTENSION;

	}

	public ExporterToCSV(JTable table) {
		this(table, null);

	}

	private String removeStringDelimiter(Object value) {
		if (value == null)
			return "";

		return ((String) value).replace(STRING_DELIMITER, '\'');
	}

	private String removeSeparatorChar(Object value) {
		if (value == null)
			return "";

		return ((String) value).replace(CSV_SEPARATOR, "\\" + CSV_SEPARATOR);
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {

		Logging.info(this, "toCSV fileName, onlySelectedRows, csvSep " + "\"" + fileName + "\", " + onlySelectedRows
				+ "\", " + "\"" + CSV_SEPARATOR + "\"");

		Boolean selectedOnly = checkSelection(onlySelectedRows);
		if (selectedOnly == null)
			return;

		if (theTable.getModel() instanceof GenTableModel)
			setClassNames(((GenTableModel) theTable.getModel()).getClassNames());

		Date date1 = null;
		fileName = checkFile(fileName, extensionFilter);

		if (fileName != null) {

			try (OutputStream os = new FileOutputStream(fileName);
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw)) {

				// write header
				StringBuilder line = new StringBuilder();
				for (int colI = 0; colI < theTable.getColumnCount(); colI++) { // i column
					line.append(STRING_DELIMITER);
					line.append(theTable.getColumnName(colI));
					line.append(STRING_DELIMITER);
					if (colI < theTable.getColumnCount() - 1) {
						line.append(CSV_SEPARATOR);
					}
				}
				line.append("\n");
				bw.write(line.toString());
				bw.flush();
				// write rows
				for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
					Logging.debug(this, "toCsv, handle row " + rowI + " selected " + theTable.isRowSelected(rowI)
							+ " selectedOnly " + selectedOnly);

					if (Boolean.TRUE.equals(!selectedOnly) || theTable.isRowSelected(rowI)) {
						line = new StringBuilder();
						for (int colI = 0; colI < theTable.getColumnCount(); colI++) { // i column
							date1 = null; // reset

							if (theTable.getValueAt(rowI, colI) != null) {
								if (classNames == null || classNames.isEmpty()) {
									if (theTable.getValueAt(rowI, colI) instanceof String) {
										String val = "" + theTable.getValueAt(rowI, colI);
										val = removeStringDelimiter(val);

										line.append(STRING_DELIMITER);
										line.append(val);
										line.append(STRING_DELIMITER);
									} else {
										String val = "" + theTable.getValueAt(rowI, colI);
										val = removeStringDelimiter(val);
										val = removeSeparatorChar(val);

										line.append(val);
									}

								} else {

									if (classNames.get(colI).equals("java.lang.String")) {

										String inString = removeStringDelimiter(theTable.getValueAt(rowI, colI));

										line.append(STRING_DELIMITER);
										line.append(inString);
										line.append(STRING_DELIMITER);

									}

									else if (classNames.get(colI).equals("java.lang.Integer")) {
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.lang.Double")) {
										Logging.debug(this,
												"decimal place --- double: " + theTable.getValueAt(rowI, colI));
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.lang.Float")) {
										Logging.debug(this,
												"decimal place --- float: " + theTable.getValueAt(rowI, colI));
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.math.BigDecimal")) {
										Logging.debug(this,
												"decimal place --- bigdecimal: " + theTable.getValueAt(rowI, colI));
										line.append(f.format(
												Double.parseDouble(theTable.getValueAt(rowI, colI).toString())));
									} else if (classNames.get(colI).equals("java.lang.Boolean")) {
										boolean booleanValue = (Boolean) theTable.getValueAt(rowI, colI);
										line.append(booleanValue);
									}

									else if (classNames.get(colI).equals("java.sql.Timestamp")) {
										if ((theTable.getValueAt(rowI, colI) != null)
												&& (!theTable.getValueAt(rowI, colI).equals(""))) {
											try {
												date1 = java.sql.Timestamp
														.valueOf((String) theTable.getValueAt(rowI, colI));

											} catch (Exception ex2) {
												Logging.error("Error in date format:" + ex2);
											}
											if (date1 != null) {
												line.append("" + date1);
											}
										}
									}

									else // append other values
										line.append(theTable.getValueAt(rowI, colI));
								}
							}

							if (colI < theTable.getColumnCount() - 1) {
								line.append(CSV_SEPARATOR);
							}
						}

						if (rowI < theTable.getRowCount() - 1) {
							line.append("\n");
						}
						bw.write(line.toString());
						bw.flush();
					}
				}

			} catch (Exception ex) {
				Logging.error(Configed.getResourceValue("ExportTable.error") + " " + ex.toString());
			}
		}

	}

}