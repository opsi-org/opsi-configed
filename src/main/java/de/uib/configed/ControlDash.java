/*
* ControlDash.java
* part of
* (open pc server integration) www.opsi.org
*
* Copyright (c) 2021 uib.de
*
* This program is free software; you may redistribute it and/or
* modify it under the terms of the GNU General Public
* License, version AGPLv3, as published by the Free Software Foundation
*
*/

package de.uib.configed;

import java.util.*;
import de.uib.messages.Messages;
import de.uib.configed.gui.*;
import de.uib.configed.gui.productpage.*;
import de.uib.utilities.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.provider.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.thread.WaitingCycle;
import de.uib.opsidatamodel.*;
import de.uib.opsidatamodel.datachanges.*;
import de.uib.opsicommand.ConnectionState;
import de.uib.configed.Globals;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.MessageFormat;
import javax.swing.event.*;
import java.net.URL;
import java.io.*;


public class ControlDash
{
	PersistenceController persist;

	
	public final static String CONFIG_KEY = "configed.dash_config";
	
	protected PanelDashControl panelDash;

	protected static FTextArea fDash;

	protected String message = "";


	private static ControlDash instance;
	
	
	
	public static ControlDash getInstance( PersistenceController persis )
	{
		if (instance == null)
		{
			instance = new ControlDash( persis );
			instance.loadData();
		}
		
		return instance;
	}
	
	

	private ControlDash( PersistenceController persis )
	{
		logging.info(this, "ControlDash constructed");
		persist = persis;
		loadData();
	}

	public void loadData()
	{

		SwingUtilities.invokeLater(
		    new Thread(){
			    public void run()
			    {
				    message = "";
				    showInfo();

				    StringBuffer mess = new StringBuffer();
				    
				    mess.append(configed.getResourceValue("Dash.topicLicences1"));
				    mess.append("\n");
				    mess.append("\n");
				    
				    
				    if ( !persist.isWithLicenceManagement() )
				    	mess.append( configed.getResourceValue("ConfigedMain.LicencemanagementNotActive") );
				    else
				    {
						mess.append( showLicenceContractWarnings() );
						mess.append( calculateVariantLicencepools() );
					}
					

				    message = mess.toString();
				    showInfo( );
			    }
		    }
		);
		//.start();
	}

	public void showInfo(  )
	{
		
		if (fDash == null)
		{
			panelDash = new PanelDashControl( ){
				
				@Override
				protected void showDashOnStartupWasSetTo(boolean b)
				{
					super.showDashOnStartupWasSetTo( b );
					persist.setGlobalBooleanConfigValue( PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART, b, "(editable on dash window)" );
				}
				
				@Override
				protected void showDashOnLicencesActivationWasSetTo(boolean b)
				{
					super.showDashOnLicencesActivationWasSetTo( b );
					persist.setGlobalBooleanConfigValue( 
						PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT, 
						b, "(editable on dash window)" );
				}
			};
	
		
			panelDash.setShowDashOnStartup( 
				persist.getGlobalBooleanConfigValue( 
					PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART, 
					PersistenceController.DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART)
			);
				
			panelDash.setShowDashOnLicencesActivation( 
				persist.getGlobalBooleanConfigValue( 
					PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT, 
					PersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT)
			);
			
			
			
			
			//fDash = new FGeneralDialog( Globals.mainFrame, Globals.APPNAME + " info", panelDash);
			
			String[] options = new String[]
				{
					configed.getResourceValue("Dash.reload"),
					configed.getResourceValue("Dash.close")
				}
			;
			
			Icon[] icons = new Icon[]
				{
					Globals.createImageIcon("images/reload16.png", "reload"),
					Globals.createImageIcon("images/cancel16.png", "cancel")
				}
			;
					
			
			fDash = 
				new FTextArea (null, Globals.APPNAME + " Dash", false, 
					options, icons,  600, 500, panelDash)
			{
				@Override
				protected boolean wantToBeRegisteredWithRunningInstances()
				{
					return true;
				}
				
				@Override
				public void doAction1()
				{
					logging.debug(this, "doAction1");
					loadData();
					logging.info(this, "update data "); 
					panelDash.setShowDashOnLicencesActivation( persist.getGlobalBooleanConfigValue( 
						PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT, 
						PersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT) );
					panelDash.setShowDashOnStartup( persist.getGlobalBooleanConfigValue( 
						PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART, 
						PersistenceController.DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART) );
				}
			
				@Override
				public void doAction2()
				{
					logging.debug(this, "doAction2");
					super.doAction2();
				}
				
				
				@Override
				public void	setVisible(boolean b)
				{
					super.setVisible( b );
					jButton1.requestFocus();
					//registerWithRunningInstances();
				}
				
				@Override
				public void leave ()
				{
					logging.debug(this, "leave");
					setVisible (false);
				}
					
				
			};
			//fDash.setAdditionalPane( new PanelDashInfo() );
			fDash.checkAdditionalPane();
			//fDash.setSize( new Dimension( 400, 500 ) );
			
			if (Globals.mainFrame != null)
			{
				fDash.setLocation(
					(int) Globals.mainFrame.getX() + Globals.locationDistanceX,
					(int) Globals.mainFrame.getY() + Globals.locationDistanceY
				)
				;
			}

		}


		//logging.info(this, "centerOn " + Globals.mainFrame);
		//fDash.centerOn( Globals.frame1 );
		



		fDash.setMessage( message );

		fDash.setVisible( true );



	}



	protected String  showLicenceContractWarnings()
	{

		StringBuffer result = new StringBuffer();

		TreeMap<String, TreeSet<String>> contractsExpired = persist.getLicenceContractsExpired();

		TreeMap<String, TreeSet<String>> contractsToNotify = persist.getLicenceContractsToNotify();


		logging.info(this, "contractsExpired " + contractsExpired);

		logging.info(this, "contractsToNotify " + contractsToNotify);

		result.append( "  "); 
		result.append( configed.getResourceValue("Dash.expiredContracts") );
		result.append( ":  \n");

		for (String date : contractsExpired.keySet() )
		{
			for (String ID : contractsExpired.get( date ) )
			{
				result.append( date + ": " + ID);
				result.append( "\n");
			}
		}
		result.append( "\n");
		
		result.append( "  "); 
		result.append( configed.getResourceValue("Dash.contractsToNotify") );
		result.append( ":  \n");


		for (String date : contractsToNotify.keySet() )
		{
			for (String ID : contractsToNotify.get( date ) )
			{
				result.append( date + ": " + ID);
				result.append( "\n");
			}
		}

		return result.toString();

	}



	protected String calculateVariantLicencepools()
	{
		StringBuffer result = new StringBuffer();

		GenTableModel modelSWnames;

		Vector<String> columnNames;
		Vector<String> classNames;

		TableUpdateCollection updateCollection;

		columnNames = new Vector<String>();
		for (String key : de.uib.configed.type.SWAuditEntry.ID_VARIANTS_COLS)
			columnNames.add( key );

		classNames = new Vector<String>();
		for (int i = 0; i < columnNames.size(); i++)
		{
			classNames.add("java.lang.String");
		}

		updateCollection = new TableUpdateCollection();

		final TreeSet<String> namesWithVariantPools = new TreeSet<String>();

		modelSWnames = new GenTableModel(
		                   null,  //no updates
		                   new DefaultTableProvider(
		                       new RetrieverMapSource(columnNames, classNames,
		                                              //new MapRetriever(){
		                                              //	public Map retrieveMap()
		                                              //{
		                                              //persist.installedSoftwareInformationRequestRefresh();
		                                              //return persist.getInstalledSoftwareName2SWinfo();
		                                              //}
		                                              //}
		                                              () -> (Map) persist.getInstalledSoftwareName2SWinfo()
		                                             )
		                       //,

		                   ),

		                   0,
		                   new int[]{},
		                   (TableModelListener)null, //panelSWnames ,
		                   updateCollection
		               )

		               /*{
		               @Override
		               public Object getValueAt( int row , int col)
		               {
		               logging.info("modelSWnames getValueAt row, col " + row + ", " + col);
		               return super.getValueAt( row, col);
	               }
	               }
		               */

		               {
			               @Override
			               protected void initColumns()
			               {
				               super.initColumns();
			               }

			               @Override
			               public void produceRows()
			               {
				               super.produceRows();


				               logging.info(this, "producing rows for modelSWnames");
				               int foundVariantLicencepools = 0;
				               namesWithVariantPools.clear();

				               int i = 0;
				               while (i < getRowCount())
				               {
					               String swName = (String) getValueAt(i,0 );

					               if ( checkExistNamesWithVariantLicencepools( swName ) )
					               {
						               //logging.info(this, "foundVariantLicencepoold  for " + swName);
						               namesWithVariantPools.add( swName );
						               foundVariantLicencepools++;
					               }

					               i++;
				               }
				               //myController.thePanel.setDisplaySimilarExist( foundVariantLicencepools );
				               logging.info(this, "produced rows, foundVariantLicencepools " + foundVariantLicencepools);
			               }

			               @Override
			               public void reset()
			               {
				               logging.info(this, "reset");
				               super.reset();
			               }
		               }
		               ;
		modelSWnames.produceRows();


		//modelSWnames.requestReload();

		Vector<Vector<Object>> specialrows = modelSWnames.getRows();
		if (specialrows != null)
		{
			logging.info(this, "initDashInfo, modelSWnames.getRows() size " + specialrows.size());
		}

		result.append( "\n" );
		result.append( "  "); 
		result.append( configed.getResourceValue("Dash.similarSWEntriesForLicencePoolExist") );
		result.append( ":  \n");

		
		for ( String name : namesWithVariantPools )
		{
			result.append( name );
			result.append( "\n" );
		}

		result.append( "\n" );
		result.append("\n");

		return result.toString();

	}

	private java.util.Set<String> getRangeSWxLicencepool( String swName )
	//nearly done in produceModelSWxLicencepool, but we collect the range of the model-map
	{
		Set<String> range = new HashSet<String>();

		for (String swID: persist.getName2SWIdents().get( swName ))
		{
			String licpool = persist.getFSoftware2LicencePool( swID );

			if ( licpool == null )
				range.add( FSoftwarename2LicencePool.valNoLicencepool ) ;
			else
				range.add ( licpool  );
		}

		return range;
	}


	private boolean checkExistNamesWithVariantLicencepools( String name )
	{
		java.util.Set<String> range = getRangeSWxLicencepool( name );


		if (range.size() > 1)
			//&& range.contains( FSoftwarename2LicencePool.valNoLicencepool ))
		{
			logging.info(this, "checkExistNamesWithVariantLicencepools, found  for " + name + " :  " + range);
			return true;
		}
		return false;
	}




}
