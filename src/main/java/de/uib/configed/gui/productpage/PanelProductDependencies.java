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

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.utilities.logging.Logging;

public class PanelProductDependencies extends JPanel implements DependenciesModel.DependenciesModelListener {

	private transient ConfigedMain mainController;

	// The label shown on top of the dependencies-panel
	private JLabel depotLabel;
	private DependenciesTreePanel dependenciesTreePanel;

	private JTable dependenciesTable;

	private DependenciesModel dependenciesModel;

	public PanelProductDependencies(ConfigedMain mainController, JLabel depotLabel) {
		this.mainController = mainController;
		this.depotLabel = depotLabel;

		initComponents();
	}

	private void initComponents() {

		JLabel labelInfoProductDependenciesTable = new JLabel(
				Configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTable"));
		JLabel labelInfoProductDependenciesTree = new JLabel(
				Configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTree"));
		JScrollPane dependenciesPanel = new JScrollPane();
		dependenciesTable = new JTable();

		dependenciesTreePanel = new DependenciesTreePanel();

		if (!Main.THEMES) {
			dependenciesTable.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		dependenciesPanel.setViewportView(dependenciesTable);
		if (!Main.THEMES) {
			dependenciesPanel.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addGroup(layout.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelInfoProductDependenciesTable))

				.addComponent(dependenciesPanel, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)

				.addGroup(layout.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelInfoProductDependenciesTree))

				.addComponent(dependenciesTreePanel, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)
				.addComponent(dependenciesTreePanel.getDependenciesTreePathPanel(), Globals.MIN_HSIZE,
						Globals.PREF_HSIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(labelInfoProductDependenciesTable)
				.addComponent(dependenciesPanel, 3 * Globals.MIN_VSIZE, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addComponent(labelInfoProductDependenciesTree)
				.addComponent(dependenciesTreePanel, 3 * Globals.MIN_VSIZE, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addComponent(dependenciesTreePanel.getDependenciesTreePathPanel(), 2 * Globals.MIN_VSIZE,
						2 * Globals.MIN_VSIZE, 2 * Globals.MIN_VSIZE));

	}

	public void clearEditing() {

		dependenciesModel = new DependenciesModel(mainController.getPersistenceController());
		dependenciesModel.addListener(this);

		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());

		dependenciesTreePanel.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}

	public void setDependenciesModel(DependenciesModel dependenciesModel) {
		this.dependenciesModel = dependenciesModel;

		dependenciesModel.addListener(this);

		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());
		dependenciesTable.setDefaultRenderer(Object.class,
				dependenciesModel.getRequirementsModel().getTableCellRenderer());

		dependenciesTreePanel.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}

	public void setEditValues(String productId, String depotId) {

		Logging.info(this, "set product  " + productId);

		dependenciesModel.setActualProduct(depotId, productId);
	}

	@Override
	public void updateProduct(String productId) {
		dependenciesTreePanel.updateTree();
	}

	@Override
	public void updateDepot(String depotId) {
		depotLabel.setText(Configed.getResourceValue("PanelProductDependencies.Depot") + ": " + depotId);
	}

	public DependenciesTreePanel getDependenciesTree() {

		return dependenciesTreePanel;
	}

	public JTable getDependenciesTable() {

		return dependenciesTable;
	}
}
