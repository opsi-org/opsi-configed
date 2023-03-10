package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.List;

public class ExporterToTerminal extends AbstractExportTable {
	public ExporterToTerminal(javax.swing.JTable table, List<String> classNames) {
		super(table, classNames);
	}

	public ExporterToTerminal(javax.swing.JTable table) {
		this(table, null);

	}

	@Override
	public void execute(String fileName, boolean onlySelectedRows) {

		Boolean selectedOnly = checkSelection(onlySelectedRows);
		if (selectedOnly == null)
			return;

		for (int rowI = 0; rowI < theTable.getRowCount(); rowI++) {

			if (Boolean.TRUE.equals(!selectedOnly) || theTable.isRowSelected(rowI)) {
				List<String> rowV = new ArrayList<>();
				for (int colI = 0; colI < theTable.getColumnCount(); colI++) {
					if (theTable.getValueAt(rowI, colI) != null && ((classNames == null || classNames.isEmpty())
							&& theTable.getValueAt(rowI, colI) instanceof String)) {

						rowV.add((String) theTable.getValueAt(rowI, colI));
					}
				}
				for (int i = 0; i < rowV.size(); i++) {
					if (!rowV.get(i).equals(" ")) {
						System.out.print(rowV.get(i));
						if (i != rowV.size() - 1)
							System.out.print(", ");
					}
				}
			}

		}

	}

}