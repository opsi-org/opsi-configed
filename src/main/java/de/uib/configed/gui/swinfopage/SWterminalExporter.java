package de.uib.configed.gui.swinfopage;

import javax.swing.JTable;

import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ExporterToTerminal;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class SWterminalExporter extends AbstractSWExporter {

	private JTable theTable;
	private ExporterToTerminal exportTable;
	private boolean onlySelectedRows;

	public SWterminalExporter(OpsiserviceNOMPersistenceController controller) {
		super(controller);
		theTable = new JTable();
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
