package de.uib.configed;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.swing.timeedit.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.licences.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.configed.Globals;

public class ControlPanelEnterLicence extends ControlMultiTablePanel
//tab new licence
{
	PanelEnterLicence thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelLicencekeys;
	GenTableModel modelLicencepools;
	GenTableModel modelLicencecontracts;
	
	PersistenceController persist;
	public ConfigedMain mainController;
	
	public ControlPanelEnterLicence(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelEnterLicence(this); //extending TabClientAdapter
		this.persist = persist;
		this.mainController = mainController; 
		init();
	}
	
	public Vector getChoicesAllHosts()
	{
		return new Vector( 
			new TreeMap( 
				persist.getHostInfoCollections().getPcListForDepots( 
					mainController.getSelectedDepots(),
					mainController.getAllowedClients()
					) 
				).keySet() 
				);
	}
	
		
	
	public void saveNewLicence(Map<String, String> m)
	{
		WaitCursor waitCursor = new WaitCursor(Globals.container1, mainController.licencesFrame.getCursor( ));
		persist.editSoftwareLicence(
					m.get(LicenceEntry.idKEY),
					m.get(LicenceEntry.licenceContractIdKEY),
					m.get(LicenceEntry.typeKEY),
					m.get(LicenceEntry.maxInstallationsKEY),
					m.get(LicenceEntry.boundToHostKEY),
					m.get(LicenceEntry.expirationDateKEY)
							);
		mainController.softwarelicencesTableProvider.requestReloadRows();
		// ensure that the visual tables everywhere get the new data when refreshed
		
		String keyValue = 
		persist.editRelationSoftwareL2LPool(
					m.get(LicenceEntry.idKEY),
					m.get("licensePoolId"),
					m.get("licenseKey")
				);
		
		modelLicencekeys.requestReload();
		modelLicencekeys.reset();
		thePanel.panelKeys.setDataChanged(false);
		thePanel.panelKeys.moveToKeyValue(keyValue);
		waitCursor.stop();
		mainController.checkErrorList();
	}
	
	
	public TabClientAdapter getTabClient()
	{
		return thePanel;
	}
	
	public void initializeVisualSettings()
	{
		super.initializeVisualSettings();
		//thePanel.panelLicencecontracts.moveToKeyValue("c_default");
	}
	
	public void init()
	{
		updateCollection = new TableUpdateCollection();
		
		Vector<String> columnNames;
		Vector<String> classNames; 
		
		// panelKeys
		columnNames = new Vector<String>();
		columnNames.add("softwareLicenseId"); columnNames.add("licensePoolId"); columnNames.add("licenseKey");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencekeys = new MapTableUpdateItemFactory(modelLicencekeys, columnNames, classNames, 0); 
		modelLicencekeys =
			new GenTableModel(
				updateItemFactoryLicencekeys,
				mainController.licenceOptionsTableProvider,
				/*
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getRelationsSoftwareL2LPool();
							}
						})
					),
				*/
			
				-1,  new int[]{0,1},
				(TableModelListener) thePanel.panelKeys,  
				updateCollection);
		updateItemFactoryLicencekeys.setSource(modelLicencekeys);
		
		tableModels.add(modelLicencekeys);
		tablePanes.add(thePanel.panelKeys);
		
		modelLicencekeys.reset();		
		thePanel.panelKeys.setTableModel(modelLicencekeys);
		modelLicencekeys.setEditableColumns(new int[]{2});
		thePanel.panelKeys.setEmphasizedColumns(new int[]{2});
		
		thePanel.panelKeys.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelKeys,
				modelLicencekeys,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						return persist.editRelationSoftwareL2LPool(
							(String) rowmap.get("softwareLicenseId"),
							(String) rowmap.get("licensePoolId"),
							(String) rowmap.get("licenseKey")
						);
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						modelLicencekeys.requestReload();
						return persist.deleteRelationSoftwareL2LPool(
							(String) rowmap.get("softwareLicenseId"), 
							(String) rowmap.get("licensePoolId")
						);
					}
				},
				updateCollection
			)
		);
		
		// panelLicencepools
		columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("description");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools, columnNames, classNames, 0);
		modelLicencepools =
			new GenTableModel(
				updateItemFactoryLicencepools,
				mainController.licencePoolTableProvider,
				/*
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getLicencePools();
							}
						})
					),
				*/
			
				0, 
				(TableModelListener) thePanel.panelLicencepools,  
				updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);
		
		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.panelLicencepools);
				
		modelLicencepools.reset();		
		thePanel.panelLicencepools.setTableModel(modelLicencepools);
		
		
		//modelLicencepools.setEditableColumns(new int[]{});
		//thePanel.panelLicencepools.setEmphasizedColumns(new int[]{0,1});
	
		// panelLicencecontracts
		columnNames = new Vector<String>();
		columnNames.add("licenseContractId"); columnNames.add("partner"); 
		columnNames.add("conclusionDate"); columnNames.add("notificationDate"); 
		columnNames.add("expirationDate"); columnNames.add("notes");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); 
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); 
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencecontracts = new MapTableUpdateItemFactory(columnNames, classNames, 0);  
		modelLicencecontracts =
			new GenTableModel(
				updateItemFactoryLicencecontracts,
				mainController.licenceContractsTableProvider,
				/*
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getLicenceContracts();
							}
						})
					),
				*/
			
				0, 
				(TableModelListener) thePanel.panelLicencecontracts,  
				updateCollection);
		updateItemFactoryLicencecontracts.setSource(modelLicencecontracts);
		
		tableModels.add(modelLicencecontracts);
		tablePanes.add(thePanel.panelLicencecontracts);
				
		modelLicencecontracts.reset();		
		thePanel.panelLicencecontracts.setTableModel(modelLicencecontracts);
		modelLicencecontracts.setEditableColumns(new int[]{0,1,2,3,4,5});
		thePanel.panelLicencecontracts.setEmphasizedColumns(new int[]{1,2,3,4,5});
		
		
		// --- PopupMenu
		JMenuItemFormatted menuItemAddContract = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddContract.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				Object[] a = new Object[6];
				a[0] = "c_" + Globals.getSeconds();
				a[1] = "";
				a[2] = Globals.getDate(false);
				a[3] = Globals.ZERODATE;
				a[4] = Globals.ZERODATE;
				a[5] = "";
				
				modelLicencecontracts.addRow(a);
				//thePanel.panelLicencecontracts.moveToLastRow();
				thePanel.panelLicencecontracts.moveToValue("" + a[0], 0);
				
			}
		});
		
		thePanel.panelLicencecontracts.addPopupItem(menuItemAddContract);
		
		// special treatment of columns
		javax.swing.table.TableColumn col;
		
		
		col=thePanel.panelLicencecontracts.getColumnModel().getColumn(2);
		
		FEditDate fedConclusionDate = new FEditDate("", false);
		
		CellEditor4TableText cellEditorConclusionDate = new CellEditor4TableText(
				fedConclusionDate, 
				FEditDate.AREA_DIMENSION ); 
		
		fedConclusionDate.setServedCellEditor( cellEditorConclusionDate );
		col.setCellEditor( cellEditorConclusionDate );
		
		
		// col 3
		col=thePanel.panelLicencecontracts.getColumnModel().getColumn(3);
		FEditDate fedNotificationDate = new FEditDate("", false);
		
		CellEditor4TableText cellEditorNotificationDate = new CellEditor4TableText(
				fedNotificationDate, 
				FEditDate.AREA_DIMENSION );
		
		fedNotificationDate.setServedCellEditor( cellEditorNotificationDate );
		col.setCellEditor( cellEditorNotificationDate );
		
		
		// col 4
		col=thePanel.panelLicencecontracts.getColumnModel().getColumn(4);
		FEditDate fedExpirationDate = new FEditDate("", false);
		
		CellEditor4TableText  cellEditorExpirationDate = new CellEditor4TableText(
				fedExpirationDate, 
				FEditDate.AREA_DIMENSION );
		
		fedExpirationDate.setServedCellEditor( cellEditorExpirationDate );
		col.setCellEditor(cellEditorExpirationDate);
		
		
		// col 5
		col=thePanel.panelLicencecontracts.getColumnModel().getColumn(5);
		
		FEditPane fedNotes = new FEditPane("", "Notes");
		
		CellEditor4TableText cellEditorLicenceContractNotes = 
			new de.uib.utilities.table.gui.CellEditor4TableText(
				fedNotes,
				FEditPane.AREA_DIMENSION
			)
		;
		
		fedNotes.setServedCellEditor( cellEditorLicenceContractNotes );
		col.setCellEditor( cellEditorLicenceContractNotes );
		
		
		//updates
		thePanel.panelLicencecontracts.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelLicencecontracts,
				modelLicencecontracts,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						return persist.editLicenceContract(
							(String) rowmap.get("licenseContractId"),
							(String) rowmap.get("partner"),
							(String) rowmap.get("conclusionDate"),
							(String) rowmap.get("notificationDate"),
							(String) rowmap.get("expirationDate"),
							(String) rowmap.get("notes")
						);
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						modelLicencecontracts.requestReload();
						return persist.deleteLicenceContract(
							(String) rowmap.get("licenseContractId")
						);
					}
				},
				updateCollection
			)
		);
		
		
	}
}
