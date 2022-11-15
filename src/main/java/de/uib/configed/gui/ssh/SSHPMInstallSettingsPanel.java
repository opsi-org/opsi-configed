package de.uib.configed.gui.ssh;

import de.uib.configed.gui.*;
import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import javax.swing.border.LineBorder.*;

public class SSHPMInstallSettingsPanel extends SSHPMInstallPanel {

	private JLabel lbl_on = new JLabel();
	private JLabel lbl_updateInstalled = new JLabel();
	private JLabel lbl_setupInstalled = new JLabel();
	private JLabel lbl_overwriteExisting = new JLabel();
	private JLabel lbl_properties = new JLabel();
	private JLabel lbl_verbosity = new JLabel();

	private JComboBox cb_verbosity;
	private JTextField tf_selecteddepots;
	private JButton btn_depotselection;
	private JCheckBox cb_properties;
	private JCheckBox checkb_updateInstalled;
	private JCheckBox checkb_setupInstalled;


	public FDepotselectionList fDepotList;
	private Vector<String> depots;
	public SSHPMInstallSettingsPanel() {
		this(null);
	}
	public SSHPMInstallSettingsPanel(JDialog dia) {
		// super();
		if (dia != null)
			setFDepotList(dia);
		initComponents();
		initLayout();
		initDepots();
	}

	private void initComponents() {
	
			lbl_on.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
			lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
			lbl_properties.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties"));

			
			btn_depotselection = new JButton(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
			btn_depotselection.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e)
					{
						initDepots();
						if (btn_depotselection != null)
							fDepotList.centerOn(btn_depotselection);
						fDepotList.setVisible(true);
					}
				}
			);
			
			tf_selecteddepots = new JTextField();
			tf_selecteddepots.setEditable(false);

			cb_verbosity = new JComboBox();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++)
				cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					// changeVerbosity();
				}
			});

			cb_properties = new JCheckBox();
			cb_properties.setSelected(true);
			lbl_updateInstalled.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.updateInstalled"));
			checkb_updateInstalled = new JCheckBox();
			checkb_updateInstalled.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					// changeUpdateInstalled();
				}
			});

			lbl_setupInstalled.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.setupInstalled"));
			checkb_setupInstalled = new JCheckBox();
			checkb_setupInstalled.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					// changeSetupInstalled();
				}
			});
	}
	
	public void setFDepotList(JDialog dia) {
		fDepotList = new FDepotselectionList(dia){
			@Override
			public void setListData(Vector<? extends String> v)
			{
				if (v == null || v.size() == 0)
				{
					setListData(new Vector<String>());
					jButton1.setEnabled(false);
				}
				else
				{
					super.setListData(v);
					jButton1.setEnabled(true);
				}
			}
				
			@Override
			public void doAction1()
			{
				tf_selecteddepots.setText(produceDepotParameter());
				super.doAction1();
			}
		};
	}

	private void initLayout() {
		this.setBackground(Globals.backLightBlue);
		// this.setBorder(new LineBorder(de.uib.configed.Globals.blueGrey));

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(Globals.gapSize)
				.addGroup(layout.createParallelGroup(center)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
							.addGroup(layout.createSequentialGroup()
								.addComponent(lbl_on,PREF, PREF, PREF)
								.addGap(Globals.gapSize)
								.addComponent(tf_selecteddepots, PREF, PREF, Short.MAX_VALUE)
							)
							.addComponent(lbl_verbosity,PREF, PREF, PREF)
							.addComponent(lbl_properties,PREF, PREF, PREF)
							.addComponent(lbl_setupInstalled,PREF, PREF, PREF)
							.addComponent(lbl_updateInstalled,PREF, PREF, PREF)
						)
						.addGap(Globals.gapSize)
						.addGroup(layout.createParallelGroup()
							.addComponent(btn_depotselection, PREF, PREF, PREF) //Globals.iconWidth, Globals.iconWidth, Globals.iconWidth) 
							.addComponent(cb_verbosity, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth) 
							.addComponent(cb_properties,PREF, PREF, PREF)
							.addComponent(checkb_setupInstalled,PREF, PREF, PREF)
							.addComponent(checkb_updateInstalled,PREF, PREF, PREF)
						)
						.addGap(Globals.gapSize, Globals.gapSize, MAX)
					)
				)
				.addGap(Globals.gapSize)
			);

		layout.setVerticalGroup(
			layout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addGroup(layout.createParallelGroup(center)			
				.addComponent(lbl_on,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(tf_selecteddepots, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent(btn_depotselection, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGroup(layout.createParallelGroup(center)
				.addComponent(lbl_verbosity,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_verbosity,leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGroup(layout.createParallelGroup(center)
				.addComponent(lbl_properties,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(cb_properties, leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGroup(layout.createParallelGroup(center)
				.addComponent(lbl_setupInstalled,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(checkb_setupInstalled, leading, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGroup(layout.createParallelGroup(center)
				.addComponent(lbl_updateInstalled,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addComponent(checkb_updateInstalled, leading ,Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
			)
			.addGap(Globals.gapSize)
		);
	}

	protected Vector<String> getAllowedInstallTargets()
	{
		Vector<String> result = new java.util.Vector<String>();
		
		if (persist.isDepotsFullPermission())
		{
			tf_selecteddepots.setEditable(true);
			result.add(persist.DEPOT_SELECTION_NODEPOTS);
			result.add(persist.DEPOT_SELECTION_ALL);
		}
		else
			tf_selecteddepots.setEditable(false);
		
		for (String depot  : persist.getHostInfoCollections().getDepotNamesList())
		{
			if (persist.getDepotPermission( depot ) )
				result.add( depot );
		}
		
		logging.info(this, "getAllowedInstallTargets " + result);
		
		return result;
	}

	protected String produceDepotParameter()
	{
		String depotParameter = ""; 
		java.util.List<String> selectedDepots = fDepotList.getSelectedDepots();
		
		if (selectedDepots.size() == 0)
		{
			if (persist.isDepotsFullPermission())
			{
				depotParameter = persist.DEPOT_SELECTION_NODEPOTS;
			}
			else if (depots.size() > 0)
			{
				depotParameter = depots.get(0);
			}
		}
		else
		{
			if (selectedDepots.contains(
					//configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS")
					persist.DEPOT_SELECTION_NODEPOTS
					)
					
				)
			{
				depotParameter = "";// persist.DEPOT_SELECTION_NODEPOTS;
			}
			else if (selectedDepots.contains(persist.DEPOT_SELECTION_ALL))
			{
				depotParameter = "all";
			}
			else	
			{
				StringBuffer sb = new StringBuffer();            
				for (String s : selectedDepots)
				{
					sb.append(s);
					sb.append(",");
				}
				depotParameter = sb.toString();
				depotParameter = depotParameter.substring(0, depotParameter.length()-1);
			}
		}
		
		logging.info(this, "produce depot parameter " + depotParameter);
		return depotParameter;
	}
		
	protected void initDepots()
	{
		depots = getAllowedInstallTargets();
		logging.info(this, "depots: " + depots.toString());
		fDepotList.setListData( depots );
		if (depots.size() == 0)
		//probably no permission
		{
			// To DO: 
			btn_depotselection.setVisible(false);
		}
		tf_selecteddepots.setText("" + depots.get(0));
	}

	public CommandOpsiPackageManagerInstall updateCommand(CommandOpsiPackageManagerInstall basicCommand) {
		// settings for command c:
		basicCommand.setVerbosity((int) cb_verbosity.getSelectedItem());
		basicCommand.setProperty(cb_properties.isSelected());
		basicCommand.setUpdateInstalled(checkb_updateInstalled.isSelected());
		basicCommand.setSetupInstalled(checkb_setupInstalled.isSelected());
		if ( tf_selecteddepots.getText().contains( configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS") ) )
			basicCommand.setDepotForPInstall( "" );
		else
			basicCommand.setDepotForPInstall( tf_selecteddepots.getText() );
		return basicCommand;
	}
}