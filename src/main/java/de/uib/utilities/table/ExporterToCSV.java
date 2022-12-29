package de.uib.utilities.table;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class ExporterToCSV extends ExportTable {
	protected String CSVencoding = "UTF8";

	public static final Character CSVseparator = ';';
	protected static final String csvSep = "" + CSVseparator;
	public static final Character stringDelimiter = '"';
	protected static final String thisExtension = ".csv";

	public ExporterToCSV(javax.swing.JTable table, List<String> classNames) {
		super(table, classNames);
		extensionFilter = new javax.swing.filechooser.FileNameExtensionFilter("CSV", "csv");

		defaultExportFilename = "export.csv";
		extension = thisExtension;

	}

	public ExporterToCSV(javax.swing.JTable table) {
		this(table, null);

	}

	private String removeStringDelimiter(Object value) {
		if (value == null)
			return "";

		return ((String) value).replace(stringDelimiter, '\'');
	}

	private String removeSeparatorChar(Object value) {
		if (value == null)
			return "";

		return ((String) value).replace(csvSep, "\\" + csvSep);
	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {

		logging.info(this, "toCSV fileName, onlySelectedRows, csvSep " + "\"" + fileName + "\", " + onlySelectedRows
				+ "\", " + "\"" + csvSep + "\"");

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
					line.append(stringDelimiter);
					line.append(theTable.getColumnName(colI));
					line.append(stringDelimiter);
					if (colI < theTable.getColumnCount() - 1) {
						line.append(csvSep);
					}
				}
				line.append("\n");
				bw.write(line.toString());
				bw.flush();
				// write rows
				for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
					logging.debug(this, "toCsv, handle row " + rowI + " selected " + theTable.isRowSelected(rowI)
							+ " selectedOnly " + selectedOnly);

					if (!selectedOnly || theTable.isRowSelected(rowI)) {
						line = new StringBuilder();
						for (int colI = 0; colI < theTable.getColumnCount(); colI++) { // i column
							date1 = null; // reset

							if (theTable.getValueAt(rowI, colI) != null) {
								if (classNames == null || classNames.isEmpty()) {
									if (theTable.getValueAt(rowI, colI) instanceof String) {
										String val = "" + theTable.getValueAt(rowI, colI);
										val = removeStringDelimiter(val);

										line.append(stringDelimiter);
										line.append(val);
										line.append(stringDelimiter);
									} else {
										String val = "" + theTable.getValueAt(rowI, colI);
										val = removeStringDelimiter(val);
										val = removeSeparatorChar(val);

										line.append(val);
									}

								} else {

									if (classNames.get(colI).equals("java.lang.String")) {

										String inString = removeStringDelimiter(theTable.getValueAt(rowI, colI));

										{
											line.append(stringDelimiter);
											line.append(inString);
											line.append(stringDelimiter);
										}
									}

									else if (classNames.get(colI).equals("java.lang.Integer")) {
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.lang.Double")) {
										logging.debug(this,
												"decimal place --- double: " + theTable.getValueAt(rowI, colI));
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.lang.Float")) {
										logging.debug(this,
												"decimal place --- float: " + theTable.getValueAt(rowI, colI));
										line.append(theTable.getValueAt(rowI, colI));
									} else if (classNames.get(colI).equals("java.math.BigDecimal")) {
										logging.debug(this,
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
												logging.error("Error in date format:" + ex2);
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
								line.append(csvSep);
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
				logging.error(configed.getResourceValue("ExportTable.error") + " " + ex.toString());
			}
		}

	}

}