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


public class ControlPanelLicencesReconciliation extends ControlMultiTablePanel
{
	
	PanelLicencesReconciliation thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelLicencesReconciliation;
	
	PersistenceController persist;
	ConfigedMain mainController;
	
	boolean initialized = false;
	
	public ControlPanelLicencesReconciliation(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelLicencesReconciliation(this) ;
		this.persist = persist;
		this.mainController = mainController; 
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
		
		java.util.List<String> extraHostFields 
			= persist.getServerConfigStrings( persist.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation );
	
		//--- panelLicencesReconciliation
		columnNames = new Vector<String>();
		classNames = new Vector<String>();
		
		columnNames.add("hostId");
		
		for (String fieldName : extraHostFields)
		{
			columnNames.add(fieldName);
			classNames.add("java.lang.String");
		}
		
		
		columnNames.add("licensePoolId");
		columnNames.add("used_by_opsi"); 
		final int index_used_by_opsi = columnNames.size()-1; 
		columnNames.add("SWinventory_used"); 
		final int index_SWinventory_used = columnNames.size()-1;
		logging.debug(this, "columnNames: " + columnNames);
		logging.debug(this, "cols index_used_by_opsi  " + index_used_by_opsi + " , " + index_SWinventory_used);
		//System.exit(0);
		classNames.add("java.lang.String"); 
		
		classNames.add("java.lang.String"); 
		classNames.add("java.lang.Boolean");
		classNames.add("java.lang.Boolean");
		MapTableUpdateItemFactory updateItemFactoryLicencesReconciliation = new MapTableUpdateItemFactory(modelLicencesReconciliation, columnNames, classNames, 0);
		modelLicencesReconciliation =
			new GenTableModel(
				updateItemFactoryLicencesReconciliation,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								logging.debug(this,"retrieveMap");
								if (initialized) persist.reconciliationInfoRequestRefresh();
								initialized = true;
								return persist.getLicencesReconciliation();
							}
						})
					),
			
				-1, 
				new int[]{0,1},
				
				(TableModelListener) thePanel.panelReconciliation,  
				updateCollection)
			;
		
		//filter which guarantees that clients are only shown when they have entries 
		modelLicencesReconciliation.setFilterCondition(new TableModelFilterCondition()
			{
				public void setFilter(TreeSet<Object> filterParam)
				{
				}
				
				public boolean test( Vector row)
				{
					return 
						((Boolean) row.get(index_used_by_opsi)) 
						|| 
						((Boolean) row.get(index_SWinventory_used))
						;
				}
			}
		);
			
	
		updateItemFactoryLicencesReconciliation.setSource(modelLicencesReconciliation);
		
		tableModels.add(modelLicencesReconciliation);
		tablePanes.add(thePanel.panelReconciliation);
				
		modelLicencesReconciliation.reset();		
		thePanel.panelReconciliation.setTableModel(modelLicencesReconciliation);
		//thePanel.panelReconciliation.setListSelectionModel(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelLicencesReconciliation.setEditableColumns(new int[]{});
		thePanel.panelReconciliation.setEmphasizedColumns(new int[]{});
		
		// --- PopupMenu
		/*
		JMenuItemFormatted menuItemAddReconciliation = new JMenuItemFormatted("add Reconciliation");//configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddReconciliation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		thePanel.panelReconciliation.addPopupItem(menuItemAddReconciliation);
		
		*/
		
		
		// special treatment of columns
		javax.swing.table.TableColumn col;
	
		
		
		col=thePanel.panelReconciliation.getColumnModel().getColumn(index_used_by_opsi); 
		col.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer()
		);
		col.setPreferredWidth(130);
		col.setMaxWidth(200);
		
		col=thePanel.panelReconciliation.getColumnModel().getColumn(index_SWinventory_used); 
		col.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer()
		);
		col.setPreferredWidth(130);
		col.setMaxWidth(200);
		
		
		//updates
		thePanel.panelReconciliation.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelReconciliation,
				modelLicencesReconciliation,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						
						//System.out.println (" --- licence key " + rowmap.get("licenseKey") + " --- notes: " + rowmap.get("notes"));
						return persist.editLicencesReconciliation(
							(String) rowmap.get("hostId"),
							(String) rowmap.get("licensePoolId")
							//(String) rowmap.get("notes")
						);
						
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						//System.out.println (" --- delete Reconciliation with hostId " + rowmap.get("hostId") + " --- softwareLicenseId " + rowmap.get("softwareLicenseId"));
						modelLicencesReconciliation.requestReload();
						return persist.deleteLicencesReconciliation(
							(String) rowmap.get("hostId"),
							(String) rowmap.get("licensePoolId")
						);
					}
				},
				updateCollection
			)
		);
		
		/*
		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++)
		{
			searchCols[j] = j;
		}
		*/
		
		Integer[] searchCols = new Integer[2];
		searchCols[0] = 0;
		searchCols[1] = 1;
		
		
		
		thePanel.panelReconciliation.setSearchColumns(searchCols);
		thePanel.panelReconciliation.setSearchSelectMode(true);
		
		//thePanel.panelReconciliation.showFiltered( true );
		
	}
}
