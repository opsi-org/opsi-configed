/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.swinfopage;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.ExporterToPDF;

public class SwPdfExporter extends AbstractSWExporter {
	private JTable theTable;
	private ExporterToPDF exportTable;

	public SwPdfExporter() {
		theTable = new JTable();
		exportTable = new ExporterToPDF(theTable);
	}

	@Override
	public void export() {
		String clientName = hostId;
		Logging.info(this, "create pdf report swaudit for ", clientName);

		Map<String, String> metaData = new HashMap<>();

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
