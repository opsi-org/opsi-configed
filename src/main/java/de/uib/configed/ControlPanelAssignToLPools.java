package de.uib.configed;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.licences.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.*;

public class ControlPanelAssignToLPools extends ControlMultiTablePanel
//Tab Licencepools
{
	private final int maxWidthIdColumnForRegisteredSoftware = 300; 
	public PanelAssignToLPools thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelLicencepools;
	GenTableModel modelProductId2LPool;
	GenTableModel modelWindowsSoftwareIds;
	
	TableModelFilterCondition windowsSoftwareFilterConditon_showOnlySelected; // we replace the filter from GenTableModel
	//static String labelWindowsSoftwareFilterCondition_showOnlySelected = "showOnlySelected";
	TableModelFilterCondition windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool;
	static String labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool = "restrictToNonAssociated";
	
	private TreeSet<Object> unAssignedSoftwareIds; 
	
	ConfigedMain mainController;
	
	public PersistenceController persist;
	
	public enum SoftwareShowMode 
		{ALL, ASSIGNED}; //activate filter for selection in software table
		
	public enum SoftwareShowAllMeans 
		{ALL, ASSIGNED_OR_ASSIGNED_TO_NOTHING, ASSIGNED_TO_NOTHING};
		
	public enum SoftwareDirectionOfAssignment 
		{POOL2SOFTWARE, SOFTWARE2POOL};
		
	
	
	
	private SoftwareShowMode softwareShow = SoftwareShowMode.ALL;
	private SoftwareShowAllMeans softwareShowAllMeans = SoftwareShowAllMeans.ALL;//SoftwareShowAllMeans.ASSIGNED_OR_ASSIGNED_TO_NOTHING; //
	private SoftwareDirectionOfAssignment softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
	//private SoftwareDirectionOfAssignment softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.SOFTWARE2POOL;
	
	Integer totalSWEntries;
	Integer totalUnassignedSWEntries;
	Integer totalShownEntries;
	
	//introducing a column for displaying the cursor row
	public final int windowsSoftwareId_KeyCol  = 1; 
	int colMarkCursorRow = 0;
	
	private HashMap<String, List<String>> removeKeysFromOtherLicencePool;
	
	
	
	public ControlPanelAssignToLPools(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelAssignToLPools(this) ;
		this.persist = persist;
		this.mainController = mainController; 
		init();
		
	}
	
	public TabClientAdapter getTabClient()
	{
		return thePanel;
	}
	
	
	private TreeSet<Object> getUnAssignedSoftwareIds()
	{
		//the object is cached in persist
		return persist.getSoftwareWithoutAssociatedLicencePool();
	}
		
	
	public void setSoftwareIdsFromLicencePool()
	{
		String selectedLicencePool = getSelectedLicencePool();
		logging.info(this, "setSoftwareIdsFromLicencePoot, selectedLicencePool " + selectedLicencePool);
		
		setSoftwareIdsFromLicencePool( selectedLicencePool );
		
	}
		
	
	private void setSoftwareIdsFromLicencePool(final String poolID)
	{
		//if (poolID == null)
		//	return;
		
		//if (softwareDirectionOfAssignment != SoftwareDirectionOfAssignment.POOL2SOFTWARE)
		//	return;
		
		logging.info(this, "setSoftwareIdsFromLicencePool " + poolID + " should be thePanel.panelLicencepools.getSelectedRow() " + thePanel.panelLicencepools.getSelectedRow());
		logging.info(this, "setSoftwareIdsFromLicencePool, call thePanel.fSoftwarename2LicencePool.setGlobalPool ");
		if (thePanel.fSoftwarename2LicencePool != null)
			thePanel.fSoftwarename2LicencePool.setGlobalPool( poolID );
		
		
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
		
		
		//logging.info(this, "setSoftwareIdsFromLicencePool old selected keys " + thePanel.panelRegisteredSoftware.getSelectedKeys());
		java.util.List<String> selectKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		
		
		//String lpoolID = thePanel.panelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRow(), 0).toString();
			
		boolean wasUsingSelectedFilter = modelWindowsSoftwareIds.isUsingFilter( GenTableModel.labelFilterConditionShowOnlySelected );		
		logging.info(this, "setSoftwareIdsFromLicencePool wasUsingSelectedFilter "  + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter( GenTableModel.labelFilterConditionShowOnlySelected, false  ); //wasUsingSelectedFilter ); //false);
		
		//boolean wasUsingBaseSelectionFilter = 	modelWindowsSoftwareIds.isUsingFilter( labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool );
		//logging.info(this, "setSoftwareIdsFromLicencePool wasUsingBaseSelectedFilter "  + wasUsingBaseSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool , false);
		
		thePanel.panelRegisteredSoftware.showFiltered( false );
		//modelWindowsSoftwareIds.setUsingFilter(  GenTableModel.labelFilterConditionShowOnlySelected, wasUsingSelectedFilter );
		
		
		
		thePanel.fieldSelectedLicencePoolId.setText(poolID);
		thePanel.fieldSelectedLicencePoolId.setToolTipText(poolID);
		
		List<String> softwareIdsForPool = new ArrayList<String>();
		if (poolID != null)
		{
			softwareIdsForPool =  persist.getSoftwareListByLicencePool(poolID);
		}
		
		logging.info(this, "setSoftwareIdsFromLicencePool  softwareIds for licencePool  "  + poolID + " : " + softwareIdsForPool.size());
		logging.info(this, "setSoftwareIdsFromLicencePool  unknown softwareIds for licencePool  "  + poolID + " : " + persist.getUnknownSoftwareListForLicencePool(poolID).size());
		
		totalUnassignedSWEntries = getUnAssignedSoftwareIds().size();
		logging.info(this, "setSoftwareIdsFromLicencePool unAssignedSoftwareIds " + totalUnassignedSWEntries );
			
		
		resetCounters(poolID);
		thePanel.fieldCountAllWindowsSoftware.setText("0");
		
		/*
		for (String ID : softwareIdsForPool)
		{
				String[] rowValues = ID.split(de.uib.utilities.Globals.pseudokeySeparator);
				
				logging.info(this, "knownSoftwareIdsForPool rowValues.length " +
						+ rowValues.length + " values " + Arrays.toString(rowValues));
		}
		*/
			
		
		
		thePanel.buttonShowAssignedNotExisting.setEnabled( persist.getUnknownSoftwareListForLicencePool(poolID).size() > 0 );
		if ( thePanel.fMissingSoftwareInfo == null )
			thePanel.fMissingSoftwareInfo = new FGlobalSoftwareInfo( de.uib.configed.Globals.frame1, this );
		
		if (persist.getUnknownSoftwareListForLicencePool(poolID).size() > 0)
		{
			Map<String, Object> missingSoftwareMap = new HashMap<String, Object>();
			
			for (String ID : persist.getUnknownSoftwareListForLicencePool(poolID))
			{
				String[] rowValues = ID.split(de.uib.utilities.Globals.pseudokeySeparator);
				
				//logging.info(this, "unknownSoftwareIdsForPool rowValues.length " +
				//		+ rowValues.length + " values " + Arrays.toString(rowValues));
				
				Map<String, String> rowMap = new HashMap<String, String>();
				for (String colName : thePanel.fMissingSoftwareInfo.columnNames)
					rowMap.put( colName, "");
				
				rowMap.put( "ID", ID );
				
				Vector<String> identKeys = de.uib.configed.type.SWAuditEntry.KEYS_FOR_IDENT;
				if (rowValues.length != identKeys.size() )
					logging.warning(this, "illegal ID " + ID);
				else
				{
					int i = 0;
					for (String key : identKeys)
					{
						rowMap.put(key, rowValues[i]);
						//logging.info(this, "unknownSoftwareIdsForPool key, val " + key + ", " +  rowValues[i] ); 
						i++;
					}
				}
				
				rowMap.put("ID", ID);
				logging.info(this, "unknownSoftwareIdsForPool " + rowMap);
				
				missingSoftwareMap.put(ID, rowMap);
			}
				
			
			
			thePanel.fMissingSoftwareInfo.setTableModel(
				new GenTableModel(
					new MapTableUpdateItemFactory(
						thePanel.fMissingSoftwareInfo.columnNames,
						thePanel.fMissingSoftwareInfo.classNames,
						0
						), //dummy  
					new DefaultTableProvider(
						new RetrieverMapSource(
							thePanel.fMissingSoftwareInfo.columnNames, 
							thePanel.fMissingSoftwareInfo.classNames,
							//() -> (Map) persist.getInstalledSoftwareInformation()
							() -> (Map) missingSoftwareMap
						)
					),
				
					0, // columnNames.indexOf("ID"), //key column  
					new int[]{},
					(TableModelListener)(thePanel.fMissingSoftwareInfo.panelGlobalSoftware) ,
					updateCollection
					)
				);
			
		}
			
		
		thePanel.fieldCountAssignedStatus.setToolTipText(" <html><br /></html>");
		if (softwareIdsForPool != null)
		{
			thePanel.fieldCountAssignedStatus.setText( produceCount( softwareIdsForPool.size() , (poolID == null) ) );
			
			StringBuffer b = new StringBuffer("<html>");
			b.append(configed.getResourceValue("PanelAssignToLPools.assignedStatusListTitle"));
			b.append("<br />");b.append("<br />");
			for (Object ident : softwareIdsForPool)
			{
				b.append(ident.toString());
				b.append("<br />");
			}
			b.append("</html>");
			thePanel.fieldCountAssignedStatus.setToolTipText(b.toString());
		}
		
		if (softwareIdsForPool == null)
			softwareIdsForPool = new ArrayList<String>();
		
		/* test
		softwareIdsForPool = new ArrayList<String>();
		softwareIdsForPool.add("Microsoft Office Office 64-bit Components 2010;14.0.6029.1000;;;x64");
		*/
		
		//if ( softwareIdsForPool != null) // &&  softwareIdsForPool.size() > 0 )
		//modelWindowsSoftwareIds.setFilter(new TreeSet( softwareIdsForPool ));
		
		
		//boolean usingFilter = modelWindowsSoftwareIds.isUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected);
		
		
		totalSWEntries =  modelWindowsSoftwareIds.getRowCount();
		//thePanel.fieldCountAllWindowsSoftware.setText( produceCount( totalSWEntries ) );
			
		produceFilterSets( softwareIdsForPool );
	
		logging.info(this, "setSoftwareIdsFromLicencePool setUsingFilter " + GenTableModel.labelFilterConditionShowOnlySelected + " to " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected , wasUsingSelectedFilter);
		thePanel.panelRegisteredSoftware.showFiltered( wasUsingSelectedFilter );
		      
		//logging.info(this, "setSoftwareIdsFromLicencePool setUsingFilter " + labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool + " to "  + wasUsingBaseSelectionFilter);
		modelWindowsSoftwareIds.setUsingFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool , 
			getSoftwareShowAllMeans() != SoftwareShowAllMeans.ALL);
			//isShowOnlyAssociationsToSelectedPoolOrNoPool());
			//wasUsingBaseSelectionFilter);
		
		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		logging.info(this, "modelWindowsSoftwareIds row count " + totalShownEntries);
		thePanel.fieldCountAllWindowsSoftware.setText( produceCount( totalSWEntries ) );//  +  " -   shown " + totalShownEntries + " - unassigned " + totalUnassignedSWEntries);
		thePanel.fieldCountDisplayedWindowsSoftware.setText( produceCount( totalShownEntries ) ); 
		thePanel.fieldCountNotAssignedSoftware.setText( produceCount( totalUnassignedSWEntries ) );
		
		//logging.info(this, "setSoftwareIdsFromLicencePool old selected keys " + thePanel.panelRegisteredSoftware.getSelectedKeys());
		
		if ( softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE)
		{
			selectKeys =  softwareIdsForPool;
			thePanel.fieldCountAssignedInEditing.setText( produceCount( softwareIdsForPool.size() , (poolID == null) ) );
		}
		else
		{
			//selectKeys old keys
			Set<Object> existingKeys = modelWindowsSoftwareIds.getExistingKeys();
			int count = 0;
			for (String key : selectKeys)
			{
				if (existingKeys.contains( key ) )
					count++;
			}
			
			thePanel.fieldCountAssignedInEditing.setText( produceCount( count ) );
		}
		
		//if ( softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE )
		{
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
			logging.debug(this, "setSoftwareIdsFromLicencePool  setSelectedValues " + selectKeys);
			thePanel.panelRegisteredSoftware.setSelectedValues(selectKeys, windowsSoftwareId_KeyCol);
			
			if (selectKeys.size() > 0)
				thePanel.panelRegisteredSoftware.moveToValue(
					selectKeys.get(selectKeys.size()-1).toString(), 
					windowsSoftwareId_KeyCol, false);
			
			logging.debug(this, "setSoftwareIdsFromLicencePool  selectedKeys " + thePanel.panelRegisteredSoftware.getSelectedKeys());
			if (wasUsingSelectedFilter) setVisualSelection( thePanel.panelRegisteredSoftware.getSelectedKeys() );	
			thePanel.panelRegisteredSoftware.setDataChanged(false);
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
		}
		/*
		else
		{
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
			thePanel.panelRegisteredSoftware.getTheTable().clearSelection();
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
		}
		*/
			
	}
	
	private String produceCount( Integer count )
	{
		if (count == null || count < 0)
			return "";
		return "" + count;
	}
	
	private String produceCount( Integer count, boolean licencePoolNull )
	{
		if (count == null || licencePoolNull || count < 0)
			return "";
		return "" + count;
	}
	
	private void resetCounters( String licencePoolId )
	{
		logging.info(this, "resetCounters for pool " + licencePoolId);
		String baseCount = "0";
		if (licencePoolId == null)
			baseCount = "";
		
		thePanel.fieldCountAssignedStatus.setText(baseCount);
		thePanel.fieldCountAssignedInEditing.setText(baseCount);
		
		thePanel.buttonShowAssignedNotExisting.setEnabled( false );
	}
	
		
	
	public void validateWindowsSoftwareKeys()
	//called by valueChanged method of ListSelectionListener
	{
		
		String selectedLicencePool = getSelectedLicencePool();
		logging.debug(this, "validateWindowsSoftwareKeys for licencePoolID " + selectedLicencePool);
		
		if (selectedLicencePool == null)
			return;
		
		logging.debug(this, "validateWindowsSoftwareKeys thePanel.panelRegisteredSoftware.isAwareOfSelectionListener " + 
				thePanel.panelRegisteredSoftware.isAwareOfSelectionListener());
			
		if (!thePanel.panelRegisteredSoftware.isAwareOfSelectionListener())
			return;
		
		//selectedLicencePool = thePanel.panelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRow(), 0).toString();
				
		java.util.List softwareIdsForPool =  persist.getSoftwareListByLicencePool(selectedLicencePool);
		//logging.debug(this, "software ids " + softwareIdsForPool);
		
		
		java.util.List<String> selKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		String showSelKeys = null;
		if (selKeys != null)
			showSelKeys = "" + selKeys.size();
		logging.info(this, "validateWindowsSoftwareKeys selectedKeys " + showSelKeys + " associated to selectedLicencePool " + selectedLicencePool); 
		
		
		if (selKeys == null)
		{
			resetCounters( selectedLicencePool );
			return;
		}
		
		ArrayList<String> cancelSelectionKeys = new ArrayList<String>();
		removeKeysFromOtherLicencePool = new HashMap<String, List<String>>();
		
		
		for (String key : selKeys)
		{
			//key is already assigned to a different licencePool?
			
			boolean gotAssociation = (persist.getFSoftware2LicencePool(key) != null);
			logging.debug(this, "validateWindowsSoftwareKeys key " + key + " gotAssociation " + gotAssociation);
			
			
			Boolean newAssociation = null;
			if ( gotAssociation )
			{
				newAssociation  =  !(persist.getFSoftware2LicencePool(key).equals(selectedLicencePool));
				logging.debug(this, "validateWindowsSoftwareKeys has association to " +  persist.getFSoftware2LicencePool(key));
				
			}
				
			
			if  (
				newAssociation != null && newAssociation
			)
			{	
				String otherPool =  persist.getFSoftware2LicencePool(key);
				
				if ( otherPool.equals( FSoftwarename2LicencePool.valNoLicencepool ) )
				{
					logging.info(this, "validateWindowsSoftwareKeys, assigned to valNoLicencepool");
				}
				else
				{
				
					String info = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned") + "\n\n" + otherPool; 
					String option = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.options");
					String title = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.title"); 
					
				
					logging.info(" software with ident \"" + key + "\" already associated to license pool " +  otherPool );
					
					FTextArea dialog = new FTextArea( de.uib.configed.Globals.frame1,
						Globals.APPNAME + " " + title, true,
						new String[]{
							configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
							configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2")
							}
						, 400, 200
						);
					dialog.setMessage(info + "\n\n" + option);
					dialog.setVisible(true);
					
					logging.info(this, "validateWindowsSoftwareKeys result " + dialog.getResult());
						
				
					if (dialog.getResult() == 1)
					{
						//we cancel the new selection
						cancelSelectionKeys.add(key);
					}
					else
					{
					
						//or delete the assignment to the licence pool
					
						List<String> removeKeys = removeKeysFromOtherLicencePool.get( otherPool );
						if (removeKeys == null) 
						{
							removeKeys = new ArrayList<String>();
							removeKeysFromOtherLicencePool.put( otherPool, removeKeys );
						}
						removeKeys.add( key );
					
					}
				}
				
			}
		}
		
		logging.info(this, "cancelSelectionKeys " + cancelSelectionKeys);
		if (cancelSelectionKeys.size() > 0) //without this condition we run into a loop
		{
			selKeys.removeAll(cancelSelectionKeys);
			logging.info(this, "selKeys after removal " + selKeys);
			thePanel.panelRegisteredSoftware.setSelectedValues(selKeys, windowsSoftwareId_KeyCol);
		}
		
		
		thePanel.fieldCountAssignedInEditing.setText("" + selKeys.size());
		
		
	}
		
	
	public void init()
	{
		updateCollection = new TableUpdateCollection();
		
		Vector<String> columnNames;
		Vector<String> classNames; 
	
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
		modelLicencepools.setEditableColumns(new int[]{0,1});
		thePanel.panelLicencepools.setEmphasizedColumns(new int[]{0,1});
		
		JMenuItemFormatted menuItemAddPool = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.NewLicencepool"));
		menuItemAddPool.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				Object[] a = new Object[2];
				a[0] = "";
				a[1] = "";
				modelLicencepools.addRow(a);
				//thePanel.panelLicencepools.moveToLastRow();
				thePanel.panelLicencepools.moveToValue("" + a[0], 0);
				
				//setting back the other tables is provided by ListSelectionListener 
				//thePanel.panelProductId2LPool.setSelectedValues(null, 0);
				//setSoftwareIdsFromLicencePool();
			}
		});
		
		thePanel.panelLicencepools.addPopupItem(menuItemAddPool);
		
		// special treatment of columns
		javax.swing.table.TableColumn col;
		thePanel.panelLicencepools.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//updates
		thePanel.panelLicencepools.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelLicencepools,
				modelLicencepools,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						//logging.info(this, "sendUpdate "  +  rowmap);
						//hack for avoiding unvoluntary reuse of a licence pool id
						boolean existsNewRow = 
						(
							mainController.licencePoolTableProvider.getRows().size() 
							< 
							modelLicencepools.getRowCount()
						);
						
						
						if ( 
							existsNewRow
							&&
							persist.getLicencepools().containsKey( (String) rowmap.get("licensePoolId") )
						)
						{
							//signalled even if only one of several rows fulfill the condition;
							//but we leave it until the service methods reflect the situation more accurately
							//logging.error("licence pool already existing");
							
							String info = configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists") 
								+ " \n(\"" +  rowmap.get("licensePoolId") + "\" ?)";
					
							String title = configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists.title"); 
							
							JOptionPane.showMessageDialog( thePanel, 
								info,
								title,
								JOptionPane.INFORMATION_MESSAGE);
								
							//modelLicencepools.reset();
							return null; //no success
						}
						
						if ( existsNewRow )
							modelLicencepools.requestReload();
						
						return persist.editLicencePool(
							(String) rowmap.get( LicencepoolEntry.idSERVICEKEY ),
							(String) rowmap.get( LicencepoolEntry.descriptionKEY )
						);
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						//logging.info(this, "sendDelete "  +  rowmap);
						modelLicencepools.requestReload();
						return persist.deleteLicencePool(
							(String) rowmap.get("licensePoolId")
						);
					}
				},
				updateCollection
			)
		);
	
		//--- panelProductId2LPool
		columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("productId"); 
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); 
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool, columnNames, classNames, 0); 
		modelProductId2LPool =
			new GenTableModel(
				updateItemFactoryProductId2LPool,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getRelationsProductId2LPool();
							}
						})
					),
			
				-1,  new int[]{0,1},
				(TableModelListener) thePanel.panelProductId2LPool,  
				updateCollection);
		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);
		
		tableModels.add(modelProductId2LPool);
		tablePanes.add(thePanel.panelProductId2LPool);
		
		modelProductId2LPool.reset();		
		thePanel.panelProductId2LPool.setTableModel(modelProductId2LPool);
		modelProductId2LPool.setEditableColumns(new int[]{0,1});
		thePanel.panelProductId2LPool.setEmphasizedColumns(new int[]{0,1});
		
		
		JMenuItemFormatted menuItemAddRelationProductId2LPool = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//logging.info(this, "actionPerformed" ); 
				Object[] a = new Object[2];
				a[0] = "";
				if (thePanel.panelLicencepools.getSelectedRow() > -1)
					a[0] = modelLicencepools.getValueAt
					(thePanel.panelLicencepools.getSelectedRowInModelTerms(), 0);
					
				a[1] = "";
				
				modelProductId2LPool.addRow(a);
				//thePanel.panelProductId2LPool.moveToLastRow();
				//logging.info(this, "addRelationProductId2LPool.addActionListener line with a[0] " + a[0]);
				thePanel.panelProductId2LPool.moveToValue("" + a[0], 0);
			}
		});
		
		thePanel.panelProductId2LPool.addPopupItem(menuItemAddRelationProductId2LPool);
		
		
		// special treatment of columns
		col=thePanel.panelProductId2LPool.getColumnModel().getColumn(0); 
		JComboBox comboLP0 = new JComboBox();
		comboLP0.setFont(Globals.defaultFontBig);
		//org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		//combo.setRenderer ();
		//col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(
			new de.uib.utilities.table.gui.AdaptingCellEditor(
				comboLP0, 
				new de.uib.utilities.ComboBoxModeller(){
					//public ComboBoxModel getComboBoxModel(int row, int column){
					//	return new DefaultComboBoxModel(modelLicencepools.getOrderedColumn(0));
					public ComboBoxModel getComboBoxModel(int row, int column){
						
						Vector poolIds =  mainController.licencePoolTableProvider.getOrderedColumn(//1, 
								mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"),
								false);
						
						
						//logging.debug(this, "retrieved poolIds: " + poolIds);
						
						if (poolIds.size() <= 1)
							poolIds.add("");
						//hack, since combo box shows nothing otherwise
						
						ComboBoxModel model = new DefaultComboBoxModel(
							poolIds); 
						
						//logging.debug(this, "got comboboxmodel  for poolIds, size " + model.getSize());
						
						return model;
					}
				}
			)
		);
		
		col=thePanel.panelProductId2LPool.getColumnModel().getColumn(1); 
		JComboBox comboLP1 = new JComboBox();
		comboLP1.setFont(Globals.defaultFontBig);
		//org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		//combo.setRenderer ();
		//col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(
			new de.uib.utilities.table.gui.AdaptingCellEditor(
				comboLP1, 
				new de.uib.utilities.ComboBoxModeller(){
					public ComboBoxModel getComboBoxModel(int row, int column){
						return new DefaultComboBoxModel(new Vector(persist.getProductIds()));
					}
				}
			)
		);
		
		//updates
		thePanel.panelProductId2LPool.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelProductId2LPool,
				modelProductId2LPool,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> m){
						//System.out.println(" sendUpdate, " + m.get("productId") + ", " + m.get("licensePoolId"));
						return persist.editRelationProductId2LPool(
							(String) m.get("productId"),
							(String) m.get("licensePoolId")
						);
					}
					public boolean sendDelete(Map<String, Object> m){
						modelProductId2LPool.requestReload();
						return persist.deleteRelationProductId2LPool(
							(String) m.get("productId"), 
							(String) m.get("licensePoolId")
							);
					}
				},
				updateCollection
			)
		);
		
		//--- panelRegisteredSoftware
		
		columnNames = new Vector<String>(de.uib.configed.type.SWAuditEntry.getDisplayKeys());
		columnNames.add(colMarkCursorRow, "CURSOR");  //introducing a column for displaying the cursor row
		
		columnNames.remove("licenseKey");
		
		//logging.info(this, "panelRegisteredSoftware columnNames " + columnNames);
		
		classNames = new Vector<String>();
		for (int i = 0; i <= columnNames.size(); i++)
		{
			classNames.add("java.lang.String");
		}
		classNames.setElementAt("java.lang.Boolean", colMarkCursorRow); //introducing a column for displaying the cursor row
		
		logging.info(this, "panelRegisteredSoftware constructed with (size) cols " + "(" + columnNames.size() + ") " + columnNames);
		logging.info(this, "panelRegisteredSoftware constructed with (size) classes " + "(" + classNames.size() + ") " +classNames);
		
		boolean withRowCounter = false;
		modelWindowsSoftwareIds =
			new GenTableModel(
				null,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								persist.installedSoftwareInformationRequestRefresh();
								return persist.getInstalledSoftwareInformationForLicensing();
							}
						}
						,  withRowCounter 
						)
					//,
					
					),
			
				windowsSoftwareId_KeyCol /*columnNames.indexOf("ID")*/, //key column  
				new int[]{},
				(TableModelListener) thePanel.panelRegisteredSoftware,  
				updateCollection);
		
		logging.info(this, "modelWindowsSoftwareIds row count " + modelWindowsSoftwareIds.getRowCount());
		tableModels.add(modelWindowsSoftwareIds);
		tablePanes.add(thePanel.panelRegisteredSoftware);	
		
		
		modelWindowsSoftwareIds.reset();	
		modelWindowsSoftwareIds.setColMarkCursorRow( colMarkCursorRow );
		
		
		
		thePanel.panelRegisteredSoftware.setTableModel(modelWindowsSoftwareIds);
		thePanel.panelRegisteredSoftware.setListSelectionMode(	
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		);
		modelWindowsSoftwareIds.setEditableColumns(new int[]{});
		thePanel.panelRegisteredSoftware.setEmphasizedColumns(new int[]{});
		
		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++)
		{
			searchCols[j] = j;
		}
		
		
		softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
		thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener( 
			softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE
			);
		
		thePanel.panelRegisteredSoftware.setSearchColumns(searchCols);
		thePanel.panelRegisteredSoftware.setSearchSelectMode(false);
		
		
		windowsSoftwareFilterConditon_showOnlySelected = new DefaultTableModelFilterCondition( windowsSoftwareId_KeyCol );
		modelWindowsSoftwareIds.chainFilter( GenTableModel.labelFilterConditionShowOnlySelected, new TableModelFilter(windowsSoftwareFilterConditon_showOnlySelected));
		modelWindowsSoftwareIds.setUsingFilter( GenTableModel.labelFilterConditionShowOnlySelected, false );
		thePanel.panelRegisteredSoftware.showFilterIcon(true);
		thePanel.panelRegisteredSoftware.setFiltermarkToolTipText( configed.getResourceValue("PanelAssignToLPools.searchPane.filtermark.tooltip") );
		
		windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool = new DefaultTableModelFilterCondition( windowsSoftwareId_KeyCol );
		modelWindowsSoftwareIds.chainFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool,  new TableModelFilter( windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool ) );
		modelWindowsSoftwareIds.setUsingFilter( labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool, false );
		
		
		//windowsSoftwarefilterCondition_showAssociationsToSelectedPoolOrNoPool.setFilter( getUnAssignedSoftwareIds() );
		//modelWindowsSoftwareIds.chainFilter("showAssociationsToSelectedPoolOrNoPool",  new TableModelFilter( windowsSoftwarefilterCondition_showAssociationsToSelectedPoolOrNoPool ) );
		//modelWindowsSoftwareIds.setUsingFilter( "showAssociationsToSelectedPoolOrNoPool", (softwareShowAllMeans == SoftwareShowAllMeans.ASSIGNED_OR_ASSIGNED_TO_NOTHING) );;
		//modelWindowsSoftwareIds.chainFilter( "showOnlyNotAssociated", new TableModelFilter(windowsSoftwarefilterCondition_showOnlyNotAssociated));
		//modelWindowsSoftwareIds.setUsingFilter( "showOnlyNotAssociated", true );
		
		
		
		
		thePanel.panelRegisteredSoftware.showFiltered(false);
		thePanel.panelRegisteredSoftware.setDataChanged(false);
		
		JMenuItemFormatted menuItemSoftwareShowAssigned = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//save values
				softwareShow = SoftwareShowMode.ASSIGNED;
				setSWAssignments();
			
			}
		});
		
		thePanel.panelRegisteredSoftware.setFiltermarkActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if (softwareShow == SoftwareShowMode.ALL)
				{
					softwareShow = SoftwareShowMode.ASSIGNED;
					setSWAssignments();
				}
				else if (softwareShow == SoftwareShowMode.ASSIGNED)
				{
					softwareShow = SoftwareShowMode.ALL;
					setSWAssignments();
				}
			
			}
		});
		
		
		
		/*
		JMenuItemFormatted menuItemSoftwareShowNotAssigned = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowNotAssigned"));
		menuItemSoftwareShowNotAssigned.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				softwareShow = SoftwareShowMode.NOT_ASSIGNED;
				//save values
				boolean b = thePanel.panelRegisteredSoftware.isDataChanged();
				thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
				List selectedKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
				thePanel.panelRegisteredSoftware.setSelectedValues(selectedKeys, 0);
				
				modelWindowsSoftwareIds.setInvertedFilter(new TreeSet(selectedKeys)); 
				modelWindowsSoftwareIds.setUsingFilter(true);
				
				
				
				//modelWindowsSoftwareIds.toggleFilter();
				
				thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
				thePanel.panelRegisteredSoftware.setDataChanged(b);
			}
		});
		*/
		
		
		
		JMenuItemFormatted menuItemSoftwareShowAll = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				softwareShow = SoftwareShowMode.ALL;
				setSWAssignments();
			}
		});
		
	
		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAll);
		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAssigned);
		//thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowNotAssigned);
	
		
		
		
		// special treatment of columns
		
	
		if (colMarkCursorRow > -1)
		{
			col=thePanel.panelRegisteredSoftware.getColumnModel()
				.getColumn(colMarkCursorRow); //row cursor column
			col.setMaxWidth(12);
			col.setHeaderValue("");
			
			col.setCellRenderer(new de.uib.utilities.table.gui.BooleanIconTableCellRenderer(
			                           Globals.createImageIcon("images/minibarpointerred.png", ""),
			                           //Globals.createImageIcon("images/draw-circle-10-red.png", ""),
			                           Globals.createImageIcon("images/minibarpointervoid.png", "")
			                       )
			                      );
			 
			
		}
		
		col=thePanel.panelRegisteredSoftware.getColumnModel()
			.getColumn(windowsSoftwareId_KeyCol);
		//col=thePanel.panelRegisteredSoftware.getColumnModel()
		//	.getColumn(columnNames.indexOf("ID"));
		col.setMaxWidth( maxWidthIdColumnForRegisteredSoftware );
		col.setHeaderValue("id ...");
		col=thePanel.panelRegisteredSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("subVersion"));
		col.setHeaderValue("OS variant");
		col.setMaxWidth(80);
		col=thePanel.panelRegisteredSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("architecture"));
		col.setMaxWidth(80);
		col=thePanel.panelRegisteredSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("language"));
		col.setMaxWidth(60);
		
		//updates
		thePanel.panelRegisteredSoftware.setUpdateController(
			new SelectionMemorizerUpdateController(
				thePanel.panelLicencepools,
				0,
				thePanel.panelRegisteredSoftware,
				modelWindowsSoftwareIds,
				new StrList2BooleanFunction(){
					public boolean sendUpdate(String poolId, List softwareIds){
						
						logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds); 
						logging.info(this, "sendUpdate poolId, removeKeysFromOtherLicencePool " +
							removeKeysFromOtherLicencePool);
						
						List<String> oldSWListForPool = persist.getSoftwareListByLicencePool( poolId ); 
						
						boolean result = true;
						
						if (removeKeysFromOtherLicencePool != null)
						{
							for (String otherPool : removeKeysFromOtherLicencePool.keySet() )
							{
								if (result 
									&& removeKeysFromOtherLicencePool.get(otherPool).size() >0
								)
								{
									result = persist.removeAssociations(
										otherPool, removeKeysFromOtherLicencePool.get(otherPool)
										);
									if (result) removeKeysFromOtherLicencePool.remove( otherPool );
								}
							}
						}
						
						if (!result)
							return false;
							
						//cleanup assignments to other pools since an update would not change them (redmine #3282)
						if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE)
						{
							result = persist.setWindowsSoftwareIds2LPool( poolId, softwareIds);
						}
						else
						{
							result = persist.addWindowsSoftwareIds2LPool( poolId, softwareIds);
						}
						
						
						
						
						
						logging.info(this, "sendUpdate, setSoftwareIdsFromLicencePool poolId " + poolId);
						setSoftwareIdsFromLicencePool(poolId);
						
						
					
						//doing it locally for fSoftware2LicencePool
						logging.info(this, "sendUpdate, adapt Softwarename2LicencePool"); 
						logging.info(this, "sendUpdate, we have software ids " + softwareIds.size());
						logging.info(this, "sendUpdate, we have software ids " +  persist.getSoftwareListByLicencePool( poolId ).size()
							+ " they are " +
							persist.getSoftwareListByLicencePool( poolId ));
						//remove all old assignements
						for (String swId : oldSWListForPool )
						{
							logging.info(this, "sendUpdate remove " + swId + " from Software2LicencePool ");
							persist.getFSoftware2LicencePool().remove( swId );
						}
						//set the current ones
						for (Object ident : softwareIds)
						{
							persist.setFSoftware2LicencePool((String) ident, poolId);
						}
						
						
						//modelWindowsSoftwareIds.setFilter(new TreeSet( softwareIds ));
						if (thePanel.fSoftwarename2LicencePool != null)
						{
							thePanel.fSoftwarename2LicencePool.panelSWnames.requestReload();
							//thePanel.fSoftwarename2LicencePool.panelSWnames.reset();
						}
							
						
						if (thePanel.fSoftwarename2LicencePool != null)
						{
							thePanel.fSoftwarename2LicencePool.panelSWxLicencepool.requestReload();
							//thePanel.fSoftwarename2LicencePool.panelSWxLicencepool.reset();
						}
						
						return result;
					}
				}
				)
			{
				public boolean cancelChanges()
				{
					setSoftwareIdsFromLicencePool(null);
					return true;
				}
			}
					
		);
		
		
		
		// -- Softwarename --> LicencePool
		
		logging.info(this,"frame Softwarename --> LicencePool  in " + de.uib.configed.Globals.frame1);
		
		final ControlPanelAssignToLPools contr = this;
		//new Thread(){
		//	public void run()
			{
				thePanel.fSoftwarename2LicencePool = new FSoftwarename2LicencePool( de.uib.configed.Globals.frame1, contr );
				thePanel.fSoftwarename2LicencePool.setTableModel( null ); //test
				thePanel.setDisplaySimilarExist( thePanel.fSoftwarename2LicencePool.checkExistNamesWithVariantLicencepools() );
				thePanel.fSoftwarename2LicencePool.setButtonsEnabled( true );
			}
		//}.start();
		
		
		//combine
		thePanel.panelLicencepools.getListSelectionModel().addListSelectionListener(
			new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e) 
				{
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;
					
					String selectedLicencePool = null;
					thePanel.panelProductId2LPool.setSelectedValues(null, 0);//clear selection
					
					ListSelectionModel lsm =
					(ListSelectionModel)e.getSource();
					
					if (lsm.isSelectionEmpty()) {
						logging.debug(this, "no rows selected");
						
					} 
					else
					{
						//int selectedRow = lsm.getMinSelectionIndex();
						selectedLicencePool= getSelectedLicencePool(); 
						thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);	
					}
					
					setSoftwareIdsFromLicencePool(selectedLicencePool);
					
					if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.SOFTWARE2POOL)
					{
						
						
						thePanel.panelRegisteredSoftware.setDataChanged(
							gotNewSWKeysForLicencePool( selectedLicencePool )
						);
					}
					
				}
			}
		);
		
		setSoftwareIdsFromLicencePool(null);
		initializeVisualSettings();
	}
	
	private void setVisualSelection(List<String> keys)
	{
		logging.debug(this, "setVisualSelection for panelRegisteredSoftware on keys " + keys); 
		thePanel.panelRegisteredSoftware.setSelectedValues(keys, windowsSoftwareId_KeyCol);
		
		if (keys != null && keys.size() > 0)
			thePanel.panelRegisteredSoftware.moveToValue(keys.get(keys.size() -1), windowsSoftwareId_KeyCol, false);
	}
	
	private void produceFilter1(List<String> assignedWindowsSoftwareIds )
	{
		TreeSet<Object> filter1 = null;
		
		if (softwareShowAllMeans != SoftwareShowAllMeans.ALL) 
			filter1 = new TreeSet<Object> (  getUnAssignedSoftwareIds()  );
		
		if (filter1 != null && softwareShowAllMeans == SoftwareShowAllMeans.ASSIGNED_OR_ASSIGNED_TO_NOTHING)
		{
			if (assignedWindowsSoftwareIds != null)
				filter1.addAll( assignedWindowsSoftwareIds );
		}
		
		String filterInfo = "null";
		if (filter1 != null)
			filterInfo = "" + filter1.size();
		
		logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);
		
		
		windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool.setFilter( filter1 );
	}
	
	
	private void produceFilterSets( List<String> assignedWindowsSoftwareIds )
	{
		TreeSet<Object> filter0 = null;
		
		if (softwareShow == SoftwareShowMode.ASSIGNED)
			filter0 = new TreeSet( assignedWindowsSoftwareIds );
		
		String filterInfo = "null";
		if (filter0 != null)
			filterInfo = "" + filter0.size();
		logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);
		windowsSoftwareFilterConditon_showOnlySelected.setFilter( filter0 );
		
		produceFilter1( assignedWindowsSoftwareIds );
	}
		
	
	private void setSWAssignments()
	{
		//save values
		boolean b = thePanel.panelRegisteredSoftware.isDataChanged();
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
		
		List<String> selectedKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		logging.info(this, "setSWAssignments  selectedKeys " + selectedKeys);
			 
		//System.out.println(" toggle filter  " + selectedKeys);
		
		logging.info(this, "setSWAssignments usingFilter " + (softwareShow ==  SoftwareShowMode.ASSIGNED) + " selected keys " + selectedKeys); 
		
		boolean usingShowSelectedFilter = (softwareShow ==  SoftwareShowMode.ASSIGNED); 
		if (usingShowSelectedFilter)
			windowsSoftwareFilterConditon_showOnlySelected.setFilter(new TreeSet(selectedKeys));
		else 
			windowsSoftwareFilterConditon_showOnlySelected.setFilter( null );
		
		thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener( 
			softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE
			);
			
		modelWindowsSoftwareIds.setUsingFilter( GenTableModel.labelFilterConditionShowOnlySelected, usingShowSelectedFilter);
			
		
		thePanel.panelRegisteredSoftware.showFiltered( usingShowSelectedFilter );
		setVisualSelection( selectedKeys );
		
		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		logging.info(this, "modelWindowsSoftwareIds row count 2 " + modelWindowsSoftwareIds.getRowCount());
		thePanel.fieldCountDisplayedWindowsSoftware.setText( produceCount( totalShownEntries ) );
		
		//thePanel.panelRegisteredSoftware.setSelectedValues(selectedKeys, 0);
		
		//if (selectedKeys != null && selectedKeys.size() > 0)
		//	thePanel.panelRegisteredSoftware.moveToValue((String) selectedKeys.get(selectedKeys.size() -1), 0, false);
		
		
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
		thePanel.panelRegisteredSoftware.setDataChanged(b);
	}
	
	public SoftwareShowAllMeans getSoftwareShowAllMeans()
	{
		return softwareShowAllMeans; 
	}
	
	public void setSoftwareShowAllMeans( SoftwareShowAllMeans meaning ) 
	{
		SoftwareShowAllMeans softwareShowAllMeans_old = softwareShowAllMeans;
		softwareShowAllMeans = meaning;
		
		if ( softwareShowAllMeans_old != softwareShowAllMeans )
		{
			boolean tableChangeAware = thePanel.panelRegisteredSoftware.isAwareOfTableChangedListener(); 
			thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener( false );
			
			String selectedLicencePool = null;
			logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected licence row "  + thePanel.panelLicencepools.getSelectedRow());
			selectedLicencePool = getSelectedLicencePool();
			setSoftwareIdsFromLicencePool(selectedLicencePool);
			
			thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(tableChangeAware );
		}
		
	}
	
	
	public void setSoftwareDirectionOfAssignment( SoftwareDirectionOfAssignment direction )
	{
		SoftwareDirectionOfAssignment oldDirection = softwareDirectionOfAssignment;
		this.softwareDirectionOfAssignment = direction;
		
		if (oldDirection != direction)
		{
			switch ( direction )
			{
				case POOL2SOFTWARE:
					thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(true);
					//thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
					break;
					
				case SOFTWARE2POOL:
					thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(false);
					resetCounters(null);
					thePanel.fieldCountAssignedInEditing.setText("");
					//thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
					break;
			}
			
			logging.info(this, "switched to " + direction);
			initializeVisualSettings();
		}
				
	}
	
	public SoftwareDirectionOfAssignment getSoftwareDirectionOfAssignment()
	{
		return this.softwareDirectionOfAssignment;
	}
	
	private boolean gotNewSWKeysForLicencePool(String selectedLicencePool)
	{
		if (selectedLicencePool == null)
			return false;
		
		List<String> oldSWList = persist.getSoftwareListByLicencePool(selectedLicencePool);
		List<String> newKeys = new ArrayList<String>(thePanel.panelRegisteredSoftware.getSelectedKeys());
		newKeys.removeAll( oldSWList );
		
		logging.info(this, "new keys " + newKeys);
		
		return (newKeys.size() > 0);
	}
		
	
	public boolean acknowledgeChangeForSWList()
	{
		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE)
		{
			//any visual change is regarded as a data change
			return true;
		}
		else
		{
			//a change in visual sw list is regarded as data changed if there additional entries
			
			String selectedLicencePool = getSelectedLicencePool();
			if (selectedLicencePool == null)
				return false;
			
			validateWindowsSoftwareKeys();
		
			return gotNewSWKeysForLicencePool( selectedLicencePool );
		}
	}			
	
	
	public String getSelectedLicencePool()
	{
		String result = null;
		
		if (thePanel.panelLicencepools.getSelectedRow() >= 0)
				result = thePanel.panelLicencepools.getValueAt( thePanel.panelLicencepools.getSelectedRow(), 0).toString();
			
		return result;
	}	
	
			
	public void initializeVisualSettings()
	{
		super.initializeVisualSettings();
		setSoftwareIdsFromLicencePool(null);
		resetCounters(null);
		//produceFilterSets( null );
		//windowsSoftwareFilterConditon_showOnlySelected.setFilter(null);
		thePanel.panelProductId2LPool.getTheTable().clearSelection();
		thePanel.panelLicencepools.getTheTable().clearSelection();
		thePanel.panelRegisteredSoftware.getTheTable().clearSelection();
		thePanel.panelRegisteredSoftware.setDataChanged(false);
		
		
	}
	
	
	
}
