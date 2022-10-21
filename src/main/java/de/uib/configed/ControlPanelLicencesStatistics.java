package de.uib.configed;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.licences.*;
import de.uib.opsidatamodel.PersistenceController;


public class ControlPanelLicencesStatistics extends ControlMultiTablePanel
{
	
	PanelLicencesStatistics thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelStatistics;
	
	PersistenceController persist;
	
	boolean initialized = false;
	
	public ControlPanelLicencesStatistics(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelLicencesStatistics(this) ;
		this.persist = persist;
		init();
	}
	
	public TabClientAdapter getTabClient()
	{
		return thePanel;
	}
	
	public void init()
	{
		updateCollection = new TableUpdateCollection();
		
		Vector<String> columnNames;
		Vector<String> classNames;
		
	
		//--- panelStatistics
		columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("licence_options");
		columnNames.add("used_by_opsi"); columnNames.add("remaining_opsi");
		columnNames.add("SWinventory_used"); columnNames.add("SWinventory_remaining");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryStatistics = new MapTableUpdateItemFactory(modelStatistics, columnNames, classNames, 0);
		modelStatistics =
			new GenTableModel(
				updateItemFactoryStatistics,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								logging.info(this, "retrieveMap() for modelStatistics");
								if (initialized) persist.reconciliationInfoRequestRefresh();
								else initialized = true;
								return persist.getLicenceStatistics();
							}
						})
					),
			
				0, 
				(TableModelListener) thePanel.panelStatistics,  
				updateCollection);
		updateItemFactoryStatistics.setSource(modelStatistics);
		
		tableModels.add(modelStatistics);
		tablePanes.add(thePanel.panelStatistics);
				
		modelStatistics.reset();		
		thePanel.panelStatistics.setTableModel(modelStatistics);
		modelStatistics.setEditableColumns(new int[]{});
		thePanel.panelStatistics.setEmphasizedColumns(new int[]{});
		
	}
}
		
