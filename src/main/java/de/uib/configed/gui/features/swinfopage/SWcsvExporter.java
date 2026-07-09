/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.swinfopage;

import javax.swing.JTable;

import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.share.logging.Logging;

/**
 * A class to implement csv export of SWAudit data
 */
public class SWcsvExporter extends AbstractSWExporter {
	public static final String EXPORT_FILE_PREFIX = "software_report_";
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

		Logging.debug("create csv report swaudit for ", clientName);

		modelSWInfo.setSorting(0, true);

		theTable.setModel(modelSWInfo);
		exportTable.setClient(clientName);
		exportTable.execute(EXPORT_FILE_PREFIX, exportFilename, false);
	}

	@Override
	protected String getExtension() {
		return ".csv";
	}
}
