/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.util.HashMap;

import javax.swing.JTable;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToPDF;

public class SwPdfExporter extends AbstractSWExporter {

	private JTable theTable;
	private ExporterToPDF exportTable;

	public SwPdfExporter() {

		theTable = new JTable();
		exportTable = new ExporterToPDF(theTable);
	}

	@Override
	public void export() {
		String clientName = theHost;
		Logging.info(this, "create pdf report swaudit for " + clientName);

		Logging.debug(this, "create pdf report swaudit for " + clientName);

		HashMap<String, String> metaData = new HashMap<>();

		metaData.put("title", "Client " + clientName);
		metaData.put("subtitle", scanInfo);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "software inventory");

		modelSWInfo.setSorting(0, true);

		theTable.setModel(modelSWInfo);

		exportTable.setClient(clientName);
		exportTable.setMetaData(metaData);
		exportTable.execute(exportFilename, false);
	}

	@Override
	protected String getExtension() {
		return ".pdf";
	}
}
