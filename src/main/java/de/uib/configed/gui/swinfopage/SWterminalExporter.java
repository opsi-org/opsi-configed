package de.uib.configed.gui.swinfopage;

import de.uib.utilities.table.ExporterToTerminal;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.*;

public class SWterminalExporter extends SWExporter{
    
    javax.swing.JTable theTable;
    ExporterToTerminal exportTable;
    Boolean onlySelectedRows = false;

    public SWterminalExporter(PersistenceController controller)
    {
        super(controller);
        theTable = new javax.swing.JTable();
        exportTable = new ExporterToTerminal(theTable);

    }

    @Override
    public void export(  )
    {
        String clientName = theHost;
		//logging.info(this, "------------- create console report swaudit for " + clientName );
		
		//System.out.println( "------------- create console report swaudit for " + clientName );
		
		
		modelSWInfo.setSorting(0, true);
		//System.out.println(" theHost " + clientName);
		//System.out.println(" export file  " + exportFilename); 
		//System.out.println(" model columns " + modelSWInfo.getColumnNames() );
		
		theTable.setModel( modelSWInfo );

        System.out.println("");
        System.out.println("SWaudit report for " + clientName);
        System.out.println("");

		exportTable.execute(null, onlySelectedRows);
    }

    @Override
	protected String getExtension()
	{
		return "";
	}

    public void setOnlySelectedRows(){
        onlySelectedRows = true;
    }

    public void setPanelTableForExportTable(PanelGenEditTable panelTable){

        exportTable.setPanelTable(panelTable);
    }
}
