package de.uib.configed;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.licences.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.*;


public class ControlPanelLicencesUsage extends ControlMultiTablePanel
{
	
	PanelLicencesUsage thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelLicencesUsage;
	GenTableModel modelLicencekeys;
	GenTableModel modelWindowsSoftwareIds;
	GenTableModel modelLicencepools;
	
	PersistenceController persist;
	ConfigedMain mainController;
	
	
	public ControlPanelLicencesUsage(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelLicencesUsage(this) ;
		this.persist = persist;
		this.mainController = mainController; 
		init();
	}
	
	public String getSoftwareLicenceReservation(String clientId)
	{
		if (clientId == null || clientId.equals(""))
		{
			JOptionPane.showMessageDialog( mainController.licencesFrame,
				  configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectClient"),
				  configed.getResourceValue("ConfigedMain.Licences.hint.title"),
				  JOptionPane.OK_OPTION);
			
			return "";
		}
		
		List selectedLPoolIds = thePanel.panelLicencepools.getSelectedKeys();
		
		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1)
		{
			JOptionPane.showMessageDialog( mainController.licencesFrame,
				  configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectOneLicencepool"),
				  configed.getResourceValue("ConfigedMain.Licences.hint.title"),
				  JOptionPane.OK_OPTION);
			
			return "";
		}
		
		String licencePoolId = (String) selectedLPoolIds.iterator().next();
		
		//System.out.println ( " ---------- licencePoolId " + licencePoolId);
		
		String result = persist.getLicenceUsage(clientId, licencePoolId);
		
		if (result != null)
		{
			thePanel.panelUsage.reload();
			//thePanel.panelUsage.moveToValue(clientId, 0, true);
			thePanel.panelUsage.moveToKeyValue(result);
		}
		else
		{
			thePanel.panelUsage.moveToValue(clientId, 0, true);
		}
			
		
		return result;
			
	}
	
	public TabClientAdapter getTabClient()
	{
		return thePanel;
	}
	
	/*
	protected Vector getChoicesAllHosts()
	{
		TreeSet set = new TreeSet();
		set.add("");
		set.addAll(new TreeMap( 
				persist.getHostInfoCollections().getPcListForDepots( mainController.getSelectedDepots() ) 
				).keySet());
		return new Vector(set); 
	}
	*/
	
	@Override
	public void initializeVisualSettings()
	{
		thePanel.setDivider();
	}
	
	
	public void init()
	{
		updateCollection = new TableUpdateCollection();
		
		Vector<String> columnNames;
		Vector<String> classNames; 
	
		//--- panelLicencesUsage
		columnNames = new Vector<String>();
		columnNames.add(LicenceUsageEntry.clientIdKEY); //"hostId");
		columnNames.add(LicenceUsageEntry.licenceIdKEY); //"softwareLicenseId"); 
		columnNames.add(LicenceUsageEntry.licencepoolIdKEY); //"licensePoolId");
		columnNames.add(LicenceUsageEntry.licencekeyKEY); //"licenseKey"); 
		columnNames.add(LicenceUsageEntry.notesKEY); //"notes"); 
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); 
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencesUsage = new MapTableUpdateItemFactory(modelLicencesUsage, columnNames, classNames, 0);
		modelLicencesUsage =
			new GenTableModel(
				updateItemFactoryLicencesUsage,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								persist.licencesUsageRequestRefresh();
								return persist.getLicencesUsage();
							}
						})
					),
			
				-1, 
				new int[]{0,1,2},
				
				(TableModelListener) thePanel.panelUsage,  
				updateCollection);
		updateItemFactoryLicencesUsage.setSource(modelLicencesUsage);
		
		tableModels.add(modelLicencesUsage);
		tablePanes.add(thePanel.panelUsage);
				
		modelLicencesUsage.reset();		
		thePanel.panelUsage.setTableModel(modelLicencesUsage);
		//thePanel.panelUsage.setListSelectionModel(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelLicencesUsage.setEditableColumns(new int[]{3,4});
		thePanel.panelUsage.setEmphasizedColumns(new int[]{3,4});
		
		// --- PopupMenu
		/*
		JMenuItemFormatted menuItemAddUsage = new JMenuItemFormatted("add Usage");//configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddUsage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		thePanel.panelUsage.addPopupItem(menuItemAddUsage);
		
		
		JMenuItemFormatted menuItemDeleteRelationLicenceUsage = new JMenuItemFormatted("delete usage");//configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemDeleteRelationLicenceUsage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
		
				int selRowsCount = thePanel.panelUsage.getSelectedRowCount();
				
				if (selRowsCount == 0)
				{
					JOptionPane.showMessageDialog( mainController.licencesFrame,
						  "keine Zeile ausgewählt", //configed.getResourceValue("ConfigedMain.Licences.noRowSelected"),
						  configed.getResourceValue("ConfigedMain.Licences.hint.title"),
						  JOptionPane.OK_OPTION);
					
					return;
				}
				else
				{
					modelLicencesUsage.deleteRow(thePanel.panelUsage.getSelectedRowInModelTerms());
				}
			}
		});
		
		thePanel.panelUsage.addPopupItem(menuItemDeleteRelationLicenceUsage);
		*/
		
		
		// special treatment of columns
		javax.swing.table.TableColumn col;
		col=thePanel.panelUsage.getColumnModel().getColumn(4);
		col.setCellEditor(
			new de.uib.utilities.table.gui.CellEditor4TableText()
		);
		
		//updates
		thePanel.panelUsage.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelUsage,
				modelLicencesUsage,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						
						//System.out.println (" -- hostId " + rowmap.get("hostId") + " -- softwareLicenseId " + rowmap.get("softwareLicenseId") + " -- licensePoolId" + rowmap.get("licensePoolId") +  " --- licence key " + rowmap.get("licenseKey") + " --- notes: " + rowmap.get("notes"));
						return persist.editLicenceUsage(
							(String) rowmap.get(LicenceUsageEntry.clientIdKEY), //"hostId"),
							(String) rowmap.get(LicenceUsageEntry.licenceIdKEY), //"softwareLicenseId"),
							(String) rowmap.get(LicenceUsageEntry.licencepoolIdKEY), //"licensePoolId"),
							(String) rowmap.get(LicenceUsageEntry.licencekeyKEY), //"licenseKey"),
							(String) rowmap.get(LicenceUsageEntry.notesKEY) //"notes")
						);
						
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						//System.out.println (" --- delete usage with hostId " + rowmap.get("hostId") + " --- softwareLicenseId " + rowmap.get("softwareLicenseId"));
						modelLicencesUsage.requestReload();
						return persist.deleteLicenceUsage(
							(String) rowmap.get(LicenceUsageEntry.clientIdKEY),  //""hostId"),
							(String) rowmap.get(LicenceUsageEntry.licenceIdKEY), //"softwareLicenseId"),
							(String) rowmap.get(LicenceUsageEntry.licencepoolIdKEY) //"licensePoolId")
						);
					}
				},
				updateCollection
			)
		);
		
		
		//--- panelLicencepools
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
		
		//combo clients
		//thePanel.setClientsList( new DefaultComboBoxModel(getChoicesAllHosts()) );
		thePanel.setClientsSource( 
			new de.uib.utilities.ComboBoxModeller(){
					public ComboBoxModel getComboBoxModel(int row, int column)
					{
						Vector choicesAllHosts = new Vector( new TreeMap( 
								persist.getHostInfoCollections().getPcListForDepots( 
									mainController.getSelectedDepots(),
									mainController.getAllowedClients()
									)
								).keySet()
							);
							
						choicesAllHosts.insertElementAt("", 0);
						
						logging.debug(this, "choicesAllHosts " + choicesAllHosts);
						
						return new DefaultComboBoxModel(choicesAllHosts);
					}
			}
		);
		
	}
}
