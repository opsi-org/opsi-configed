/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.infopage;

import java.io.File;
import java.util.Map;

import javax.swing.JTable;

import de.uib.configed.gui.features.swinfopage.PanelSWSingleClientInfo.KindOfExport;
import de.uib.configed.gui.share.table.AbstractExportTable.OverwriteDecision;
import de.uib.configed.gui.share.table.ExporterToCSV;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.share.logging.Logging;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
public class SingleClientExporter {
	@NonNull
	private final JTable table;
	private final String filename;
	private final boolean askForOverwrite;
	private final boolean onlySelectedRows;

	@NonNull
	private final KindOfExport kindOfExport;
	private final Map<String, String> metaData;

	@Default
	private final OverwriteDecision overwriteDecision = OverwriteDecision.CONTINUE;
	private final String defaultPrefix;
	private final String exportClientId;

	/**
	 * Exports the data according to kindOfExport
	 */
	public boolean export() {
		if (overwriteDecision == OverwriteDecision.SKIP_ALL && new File(filename).exists()) {
			Logging.info(this, "Skipping file (user chose Skip All): ", filename);
			return true;
		}

		if (!hasData(onlySelectedRows)) {
			Logging.info(SingleClientExporter.class, "No data to export for file: ", filename, ", skipping export");
			return false;
		}

		return switch (kindOfExport) {
		case CSV -> exportToCSV();
		case PDF -> exportToPDF();
		default -> {
			Logging.warning(SingleClientExporter.class, "unexpected kindOfExport ", kindOfExport);
			yield false;
		}
		};
	}

	/**
	 * Returns true if the table contains any data to export. If
	 * onlySelectedRows is true, only considers selected rows.
	 */
	private boolean hasData(boolean onlySelected) {
		if (table == null || table.getRowCount() == 0 || table.getColumnCount() == 0) {
			return false;
		}

		for (int r = 0; r < table.getRowCount(); r++) {
			if (onlySelected && !table.isRowSelected(r)) {
				continue;
			}

			for (int c = 0; c < table.getColumnCount(); c++) {
				Object val = table.getValueAt(r, c);
				if (val != null && !val.toString().trim().isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean exportToCSV() {
		ExporterToCSV exporter = new ExporterToCSV(table);
		exporter.setAskForOverwrite(askForOverwrite && overwriteDecision != OverwriteDecision.OVERWRITE_ALL);
		exporter.setClient(exportClientId);
		return exporter.execute(filename, onlySelectedRows);
	}

	private boolean exportToPDF() {
		ExporterToPDF pdfExporter = new ExporterToPDF(table);
		if (metaData != null) {
			pdfExporter.setMetaData(metaData);
		}
		pdfExporter.setPageSizeA4Landscape();
		pdfExporter.setAskForOverwrite(askForOverwrite && overwriteDecision != OverwriteDecision.OVERWRITE_ALL);
		pdfExporter.setClient(exportClientId);
		return pdfExporter.execute(filename, onlySelectedRows);
	}
}
