package de.uib.configed.gui.ssh;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */

import de.uib.opsicommand.sshcommand.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;


import java.awt.event.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import java.io.*;
import java.util.*;
public class SSHCommandControlParameterMethodsPanel  extends JPanel
{
	private GroupLayout thisLayout;
	private JDialog main;
	private final SSHCommandFactory factory = SSHCommandFactory.getInstance();
 	
 	private JLabel lbl_paramMethods = new JLabel();
 	private JLabel lbl_paramFormats = new JLabel();
 	private JLabel lbl_empty = new JLabel();
	private JComboBox cb_parameter_methods;
	private JComboBox cb_parameter_formats;
	private JButton btn_add_param;
	private JButton btn_test_param;
	private JButton btn_test_command;

	public SSHCommandControlParameterMethodsPanel(JDialog owner, int lg, int rg, int ug, int og)
	{
		super();
		logging.info(this, "SSHCommandControlParameterMethodsPane  main " + main);
		main = owner;
		init();
		setGapSize(lg,rg,ug,og);
		initLayout();
		// setComponentsEnabled_RO();
	}

	public SSHCommandControlParameterMethodsPanel(JDialog owner)
	{
		super();
		logging.info(this, "SSHCommandControlParameterMethodsPane  main " + main);
		main = owner;		
		init();

		// initLayout();
	}
	public JPanel getPanel()
	{
		return this;
	}
	public GroupLayout getGrouplayout()
	{
		return thisLayout;
	}
	/** Init components **/
	private void init() 
	{
		logging.debug(this, "init setting up components ");
		Dimension tf_dim = new Dimension( Globals.firstLabelWidth -Globals.graphicButtonWidth, Globals.buttonHeight);
		Dimension tf_dim_long = new Dimension( Globals.firstLabelWidth + Globals.gapSize, Globals.buttonHeight);
		Dimension btn_dim = new Dimension(Globals.graphicButtonWidth + 15 ,Globals.buttonHeight);
		
		lbl_empty.setPreferredSize(tf_dim_long);
		lbl_paramMethods.setText(configed.getResourceValue("SSHConnection.CommandControl.parameterMethods"));
		//lbl_paramMethods.setPreferredSize(tf_dim);
		lbl_paramFormats.setText(configed.getResourceValue("SSHConnection.CommandControl.parameterFormats"));
		//lbl_paramFormats.setPreferredSize(tf_dim);
		cb_parameter_formats = new JComboBox(factory.getParameterHandler().getParameterFormats());
		logging.info(this, "cb_parameter_formats lightweight " + cb_parameter_formats.isLightWeightPopupEnabled() );
		//cb_parameter_formats.setLightWeightPopupEnabled(false);
		cb_parameter_formats.setPreferredSize(tf_dim_long);
		cb_parameter_formats.setMaximumRowCount(5); // we have to delimit it so that is constrained to the component (in Windows) //Globals.comboBoxRowCount);
		cb_parameter_methods = new JComboBox(factory.getParameterHandler().getParameterMethodLocalNames());
		cb_parameter_methods.setSelectedItem(configed.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv"));
		cb_parameter_methods.setPreferredSize(tf_dim_long);
		cb_parameter_methods.setMaximumRowCount(5); //Globals.comboBoxRowCount);
		//cb_parameter_methods.setLightWeightPopupEnabled(false);
		cb_parameter_formats.setEnabled(false);
		
		cb_parameter_methods.addItemListener(new ItemListener() 
		{
			@Override
			public void itemStateChanged(ItemEvent e) 
			{
				if  ( ((String)cb_parameter_methods.getSelectedItem())
					.equals(configed.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv")) )
				{
					cb_parameter_formats.setEnabled(false);
				}
				else { cb_parameter_formats.setEnabled(true); }
			}
		});
		

		btn_test_param= new de.uib.configed.gui.IconButton(
			de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnTestParamMethod") ,
			"images/executing_command.png", "images/executing_command.png", "images/executing_command.png",true
		);
		btn_test_param.setPreferredSize(btn_dim);

		btn_add_param = new de.uib.configed.gui.IconButton(
			de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnAddParamMethod") ,
			"images/list-add.png", "images/list-add.png", "images/list-add_disabled.png",true
		);
		btn_add_param.setSize(btn_dim);
		btn_add_param.setPreferredSize(btn_dim);
		setComponentsEnabled_RO();
	}
	public void setComponentsEnabled_RO()
	{
		btn_test_param.setEnabled(!Globals.isGlobalReadOnly());
		btn_add_param.setEnabled(!Globals.isGlobalReadOnly());
	}
	
	public JButton getButtonAdd()
	{
		return btn_add_param;
	}
	public JButton getButtonTest()
	{
		return btn_test_param;
	}
	int lGap = Globals.gapSize;
	int rGap = Globals.gapSize;
	int uGap = Globals.gapSize;
	int oGap = Globals.gapSize;
	public void setGapSize(int lgap, int rgap, int ugap,int ogap )
	{
		logging.info(this, "setGapSize lgap  " + lgap + " rgap " + rgap + " ugap " + ugap + " ogap " + ogap );
		this.lGap = lgap;
		this.rGap = rgap;
		this.uGap = ugap;
		this.oGap = ogap;
	}
	public void initLayout()
	{
		logging.debug(this, "initLayout " );
		setBackground(Globals.backLightBlue);
		thisLayout = new GroupLayout((JComponent) this);
		setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
			.addGap(lGap)
			.addGroup(thisLayout.createParallelGroup()
				.addGroup(thisLayout.createSequentialGroup()
					.addGroup(thisLayout.createParallelGroup()
						.addComponent(lbl_paramMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_paramFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.minGapSize*2)
					.addGroup(thisLayout.createParallelGroup()
						.addComponent(cb_parameter_methods,Globals.buttonWidth, Globals.buttonWidth, 3*Globals.buttonWidth)
						.addComponent(cb_parameter_formats, Globals.buttonWidth, Globals.buttonWidth, 3*Globals.buttonWidth)
					)
					.addGap(Globals.minGapSize*3, Globals.minGapSize*3, Short.MAX_VALUE)
				)
				
				.addGroup(thisLayout.createSequentialGroup()
					.addComponent(lbl_empty, 10, 10, Short.MAX_VALUE)
					.addComponent(btn_test_param, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth)
					.addComponent(btn_add_param, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth)
				)
				
			)
			.addGap(rGap)
		);
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
			.addGap(oGap * 2)
			.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(cb_parameter_methods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(thisLayout.createSequentialGroup()
					.addComponent(lbl_paramMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGap(Globals.minGapSize)
			.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(cb_parameter_formats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(thisLayout.createSequentialGroup()
					.addComponent(lbl_paramFormats,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGap(Globals.minGapSize)
			.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(btn_add_param, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight)
				.addComponent(btn_test_param, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight)
				.addComponent(lbl_empty, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight, de.uib.configed.Globals.buttonHeight)
			)
			.addGap(uGap * 2)
			//.addGap(50) //we add space for the combo box popup which does not always correctly appear if not placed inside the frame 
		);
		repaint();
		revalidate();
	}

	public void doActionTestParam(JDialog caller)
	{
		String paramText =  "";
		if  ( ((String)cb_parameter_methods.getSelectedItem()).equals(factory.getParameterHandler().method_interactiveElement) )
		{
			paramText = factory.getParameterHandler().replacement_default_1
					+ factory.getParameterHandler().getMethodFromName((String) cb_parameter_methods.getSelectedItem())
					+ factory.getParameterHandler().replacement_default_2;
		}
		else 
			paramText = factory.getParameterHandler().replacement_default_1
				+ factory.getParameterHandler().getMethodFromName((String)cb_parameter_methods.getSelectedItem())
				+ factory.getParameterHandler().param_splitter_default 
				+ cb_parameter_formats.getSelectedItem()
				+ factory.getParameterHandler().replacement_default_2;

		try {
			logging.info(this, "actionPerformed(testParamMethod) parameterText " + paramText);
			String result = "echo \"{0}\"".replace("{0}", factory.getParameterHandler().testParameter(paramText));
			logging.info(this, "actionPerformed(testParamMethod) result " + result);
			String showThisText = "echo \"{0}\"".replace("{0}", paramText) + ":\n" + result;
			if (result.equals(configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed")))
				showThisText = configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed");
			JOptionPane.showMessageDialog(main, 
				showThisText, 
				configed.getResourceValue("SSHConnection.CommandControl.parameterTest.title"), 
				JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception ble)
		{ 
			logging.warning(this, "Exception  testing parameter-method failed.");
		}
		if (caller != null) caller.setVisible(true);
	}

	public void doActionParamAdd(JTextComponent component )
	{
		String text = (String) component.getText();
		String paramText =  "";
		if  ( ((String)cb_parameter_methods.getSelectedItem()).equals(factory.getParameterHandler().method_interactiveElement) )
		{
			paramText = factory.getParameterHandler().replacement_default_1
					+ factory.getParameterHandler().getMethodFromName((String) cb_parameter_methods.getSelectedItem())
					+ factory.getParameterHandler().replacement_default_2;
		}
		else 
			paramText = factory.getParameterHandler().replacement_default_1
				+ factory.getParameterHandler().getMethodFromName((String)cb_parameter_methods.getSelectedItem())
				+ factory.getParameterHandler().param_splitter_default 
				+ cb_parameter_formats.getSelectedItem()
				+ factory.getParameterHandler().replacement_default_2;
		try {
			component.getDocument().insertString(component.getCaretPosition(), paramText, null);
		}
		catch (BadLocationException ble)
		{ 
			logging.warning(this, " BadLocationException  add parameter method to command failed.");
		}
	}
}