package de.uib.utilities.table;

import java.util.Vector;

public class ExporterToTerminal extends ExportTable {
	public ExporterToTerminal(javax.swing.JTable table, Vector<String> classNames) {
		super(table, classNames);
	}

	public ExporterToTerminal(javax.swing.JTable table) {
		this(table, null);

	}

	public void execute(String fileName, boolean onlySelectedRows) {
		// System.out.println("onlySelectedRows: "+ onlySelectedRows);
		Boolean selectedOnly = checkSelection(onlySelectedRows);
		if (selectedOnly == null)
			return;

		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {
			// System.out.println("selected? " + theTable.isRowSelected(rowI));
			if (!selectedOnly || theTable.isRowSelected(rowI)) {
				Vector<String> rowV = new Vector<String>();
				for (int colI = 0; colI < theTable.getColumnCount(); colI++) {
					if (theTable.getValueAt(rowI, colI) != null) {
						if (classNames == null || classNames.size() == 0) {
							if (theTable.getValueAt(rowI, colI) instanceof String) {
								// System.out.println(theTable.getValueAt(rowI, colI));
								rowV.add((String) theTable.getValueAt(rowI, colI));

							}
						}
					}
				}
				for (int i = 0; i < rowV.size(); i++) {
					if (rowV.get(i) != " ") {
						System.out.print(rowV.get(i));
						if (i != rowV.size() - 1)
							System.out.print(", ");
					}
				}
				System.out.println("");

			}

		}

	}

}