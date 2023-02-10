package de.uib.configed.gui.swinfopage;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToCSV;

/**
 * A class to implement pdf export of SWAudit data
 */
public class SWcsvExporter extends AbstractSWExporter {
	javax.swing.JTable theTable;
	ExporterToCSV exportTable;

	public SWcsvExporter() {
		theTable = new javax.swing.JTable();

		exportTable = new ExporterToCSV(theTable);
	}

	@Override
	public void export() {
		String clientName = theHost;
		Logging.info(this, "------------- create csv report swaudit for " + clientName);

		Logging.debug("------------- create csv report swaudit for " + clientName);

		modelSWInfo.setSorting(0, true);

		theTable.setModel(modelSWInfo);
		exportTable.execute(exportFilename, false);

	}

	@Override
	protected String getExtension() {
		return ".csv";
	}

}