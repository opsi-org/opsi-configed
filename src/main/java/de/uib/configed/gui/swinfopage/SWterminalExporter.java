package de.uib.configed.gui.swinfopage;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToTerminal;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class SWterminalExporter extends SWExporter {

	javax.swing.JTable theTable;
	ExporterToTerminal exportTable;
	Boolean onlySelectedRows = false;

	public SWterminalExporter(PersistenceController controller) {
		super(controller);
		theTable = new javax.swing.JTable();
		exportTable = new ExporterToTerminal(theTable);

	}

	@Override
	public void export() {
		String clientName = theHost;

		modelSWInfo.setSorting(0, true);

		theTable.setModel(modelSWInfo);

		Logging.debug("");
		Logging.debug("SWaudit report for " + clientName);
		Logging.debug("");

		exportTable.execute(null, onlySelectedRows);
	}

	@Override
	protected String getExtension() {
		return "";
	}

	public void setOnlySelectedRows() {
		onlySelectedRows = true;
	}

	public void setPanelTableForExportTable(PanelGenEditTable panelTable) {

		exportTable.setPanelTable(panelTable);
	}
}
