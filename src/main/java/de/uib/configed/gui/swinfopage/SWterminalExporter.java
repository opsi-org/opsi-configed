package de.uib.configed.gui.swinfopage;

import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
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
		// logging.info(this, "------------- create console report swaudit for " +
		

		// logging.debug( "------------- create console report swaudit for " +
		

		modelSWInfo.setSorting(0, true);
		// logging.debug(" theHost " + clientName);
		// logging.debug(" export file " + exportFilename);
		// logging.debug(" model columns " + modelSWInfo.getColumnNames() );

		theTable.setModel(modelSWInfo);

		logging.debug("");
		logging.debug("SWaudit report for " + clientName);
		logging.debug("");

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
