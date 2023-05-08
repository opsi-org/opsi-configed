package de.uib.configed.gui.swinfopage;

import java.util.HashMap;

import javax.swing.JTable;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class SwPdfExporter extends AbstractSWExporter {

	JTable theTable;
	ExporterToPDF exportTable;
	private boolean onlySelectedRows;

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
		exportTable.execute(exportFilename, onlySelectedRows);

	}

	@Override
	protected String getExtension() {
		return ".pdf";
	}

	public void setPanelTableForExportTable(PanelGenEditTable panelTable) {
		exportTable.setPanelTable(panelTable);
	}

	public void setOnlySelectedRows() {
		onlySelectedRows = true;
	}
}
