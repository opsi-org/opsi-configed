/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import javax.swing.JTable;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.ExporterToCSV;

/**
 * A class to implement pdf export of SWAudit data
 */
public class SWcsvExporter extends AbstractSWExporter {
	private JTable theTable;
	private ExporterToCSV exportTable;

	public SWcsvExporter() {
		theTable = new JTable();

		exportTable = new ExporterToCSV(theTable);
	}

	@Override
	public void export() {
		String clientName = hostId;
		Logging.info(this, "create csv report swaudit for ", clientName);

		Logging.debug("create csv report swaudit for " + clientName);

		modelSWInfo.setSorting(0, true);

		theTable.setModel(modelSWInfo);
		exportTable.execute(exportFilename, false);
	}

	@Override
	protected String getExtension() {
		return ".csv";
	}
}
