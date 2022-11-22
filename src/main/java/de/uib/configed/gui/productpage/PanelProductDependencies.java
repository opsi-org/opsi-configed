/*
 * ProductInfoPanes.java
 *
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2022 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 * 
 * This is the whole panel that contains the product dependencies table
 * and the product dependencies tree and some more information on them;
 * it uses the dependencies model for the needed information (and listenes to that model)
 * 
 * @author Nils Otto
 
 */
 
 package de.uib.configed.gui.productpage;

import de.uib.utilities.logging.*;
import de.uib.configed.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.configed.guidata.DependenciesModel;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;

public class PanelProductDependencies extends JPanel implements DependenciesModel.DependenciesModelListener {
	
	private ConfigedMain mainController;
	
	// The label shown on top of the dependencies-panel
	private JLabel depotLabel;
	
	private JLabel labelInfoProductDependenciesTable;
	private JLabel labelInfoProductDependenciesTree;
	
	private DependenciesTree dependenciesTree;
	
	private JScrollPane dependenciesPanel;
	protected JTable dependenciesTable;
	
	private DependenciesModel dependenciesModel;

	private int minLabelVSize = 0;
	private int minTableVSize = 40;
	private int minGapVSize = 2;
	private int minVSize 	= 10;
	private int prefVSize 	= 20;
	private int vGapSize	= 5;
	private int hGapSize = 2;
	
	
	private int minHSize = 50;
	private int prefHSize = 80;
	 
	public PanelProductDependencies(ConfigedMain mainController, JLabel depotLabel) {
		this.mainController = mainController;
		this.depotLabel = depotLabel;
		
		initComponents();
	}
	
	public void initComponents() {
		
		labelInfoProductDependenciesTable = new JLabel(configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTable"));
		labelInfoProductDependenciesTree = new JLabel(configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTree"));
		dependenciesPanel = new javax.swing.JScrollPane();
		dependenciesTable = new JTable();
		
		
		dependenciesTree = new DependenciesTree();
		
		dependenciesTable.setBackground(Globals.backLightBlue);
		dependenciesPanel.setViewportView(dependenciesTable);
		dependenciesPanel.getViewport().setBackground(Globals.backLightBlue);
		
		
		GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			
			.addGroup(layout.createSequentialGroup()
				.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
				.addComponent(labelInfoProductDependenciesTable)
			)
			
			.addComponent(dependenciesPanel, minHSize, prefHSize, Short.MAX_VALUE)
			
			.addGroup(layout.createSequentialGroup()
				.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
				.addComponent(labelInfoProductDependenciesTree)
			)
			
			.addComponent(dependenciesTree, minHSize, prefHSize, Short.MAX_VALUE)
			.addComponent(dependenciesTree.getDependenciesTreePathPanel(), minHSize, prefHSize, Short.MAX_VALUE)
		);
		 
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(labelInfoProductDependenciesTable)
			.addComponent(dependenciesPanel, 3*minVSize, 3*prefVSize, Short.MAX_VALUE)
			.addComponent(labelInfoProductDependenciesTree)
			.addComponent(dependenciesTree, 3*minVSize, 3*prefVSize, Short.MAX_VALUE)
			.addComponent(dependenciesTree.getDependenciesTreePathPanel(), minLabelVSize, prefVSize, GroupLayout.PREFERRED_SIZE)
		);
		
	}
	
	public void clearEditing() {
		
		dependenciesModel = new DependenciesModel(mainController.getPersistenceController());
		dependenciesModel.addListener(this);
		
		dependenciesTable.setModel( dependenciesModel.getRequirementsModel());
		
		dependenciesTree.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}
	
	public void setDependenciesModel(DependenciesModel dependenciesModel) {
		this.dependenciesModel = dependenciesModel;
		
		dependenciesModel.addListener(this);
		
		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());
		dependenciesTable.setDefaultRenderer(Object.class, dependenciesModel.getRequirementsModel().getTableCellRenderer());
		
		dependenciesTree.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}
	
	public void setEditValues(String productId, String depotId) {
		
		logging.info(this, "set product  " + productId);
		
		dependenciesModel.setActualProduct(depotId, productId);
	}
	
	@Override
	public void updateProduct(String productId) {
		dependenciesTree.updateTree();
	}
	
	@Override
	public void updateDepot(String depotId) {
		depotLabel.setText(configed.getResourceValue("PanelProductDependencies.Depot") + ": " + depotId);
	}
		
	
	public DependenciesTree getDependenciesTree() {
		
		return dependenciesTree;
	}
	
	public JTable getDependenciesTable() {
		
		return dependenciesTable;
	}
}
