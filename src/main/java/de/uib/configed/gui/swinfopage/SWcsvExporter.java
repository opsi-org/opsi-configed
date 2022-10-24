package de.uib.configed.gui.swinfopage;


import java.io.*;
import java.util.*;
import java.text.MessageFormat;

import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.provider.*;
import de.uib.utilities.table.gui.*;

import de.uib.opsicommand.ConnectionState;
import de.uib.messages.Messages;


/**
*  A class to implement pdf export of SWAudit data
*/
public class SWcsvExporter extends SWExporter
{
	javax.swing.JTable theTable;
	ExporterToCSV exportTable;
	
	
	public SWcsvExporter()
	{
		theTable = new javax.swing.JTable();
		
		exportTable = new ExporterToCSV(theTable);
	}
	
	
	@Override
	public void export(  )
	{
		String clientName = theHost;
		logging.info(this, "------------- create csv report swaudit for " + clientName );
		
		System.out.println( "------------- create csv report swaudit for " + clientName );
		
		
		modelSWInfo.setSorting(0, true);
		//System.out.println(" theHost " + clientName);
		//System.out.println(" export file  " + exportFilename); 
		//System.out.println(" model columns " + modelSWInfo.getColumnNames() );
		
		theTable.setModel( modelSWInfo );
		exportTable.execute( exportFilename, false );
		
		
	}

	
	
	@Override
	protected String getExtension()
	{
		return ".csv";
	}

}