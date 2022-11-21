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

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.utilities.logging.logging;

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

	public PanelProductDependencies(ConfigedMain mainController, JLabel depotLabel) {
		this.mainController = mainController;
		this.depotLabel = depotLabel;

		initComponents();
	}

	public void initComponents() {

		labelInfoProductDependenciesTable = new JLabel(
				configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTable"));
		labelInfoProductDependenciesTree = new JLabel(
				configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTree"));
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
						.addComponent(labelInfoProductDependenciesTable))

				.addComponent(dependenciesPanel, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)

				.addGroup(layout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelInfoProductDependenciesTree))

				.addComponent(dependenciesTree, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)
				.addComponent(dependenciesTree.getDependenciesTreePathPanel(), Globals.minHSize, Globals.prefHSize,
						Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(labelInfoProductDependenciesTable)
				.addComponent(dependenciesPanel, 3 * Globals.minVSize, Globals.prefVSize, Short.MAX_VALUE)
				.addComponent(labelInfoProductDependenciesTree)
				.addComponent(dependenciesTree, 3 * Globals.minVSize, Globals.prefVSize, Short.MAX_VALUE)
				.addComponent(dependenciesTree.getDependenciesTreePathPanel(), 2 * Globals.minVSize,
						2 * Globals.minVSize,
						2 * Globals.minVSize));

	}

	public void clearEditing() {

		dependenciesModel = new DependenciesModel(mainController.getPersistenceController());
		dependenciesModel.addListener(this);

		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());

		dependenciesTree.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}

	public void setDependenciesModel(DependenciesModel dependenciesModel) {
		this.dependenciesModel = dependenciesModel;

		dependenciesModel.addListener(this);

		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());
		dependenciesTable.setDefaultRenderer(Object.class,
				dependenciesModel.getRequirementsModel().getTableCellRenderer());

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
