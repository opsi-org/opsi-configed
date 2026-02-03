/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import java.util.Map;

import javax.swing.JTable;

import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.share.logging.Logging;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
public class SingleClientExporter {
	@NonNull
	private final JTable table;

	@NonNull
	private final String filename;

	private final boolean askForOverwrite;

	private final boolean onlySelectedRows;

	@NonNull
	private final KindOfExport kindOfExport;

	private final Map<String, String> metaData;

	/**
	 * Exports the data according to kindOfExport
	 */
	public void export() {
		switch (kindOfExport) {
		case CSV -> exportToCSV();
		case PDF -> exportToPDF();
		default -> Logging.warning(SingleClientExporter.class, "unexpected kindOfExport ", kindOfExport);
		}
	}

	private void exportToCSV() {
		ExporterToCSV exporter = new ExporterToCSV(table);
		exporter.setAskForOverwrite(askForOverwrite);
		exporter.execute(filename, onlySelectedRows);
	}

	private void exportToPDF() {
		ExporterToPDF pdfExporter = new ExporterToPDF(table);
		if (metaData != null) {
			pdfExporter.setMetaData(metaData);
		}
		pdfExporter.setPageSizeA4Landscape();
		pdfExporter.execute(filename, onlySelectedRows);
	}
}
