/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.guidata.DependenciesModel;
import de.uib.utilities.logging.Logging;

public class PanelProductDependencies extends JPanel implements DependenciesModel.DependenciesModelListener {

	// The label shown on top of the dependencies-panel
	private JLabel depotLabel;
	private DependenciesTreePanel dependenciesTreePanel;

	private JTable dependenciesTable;

	private DependenciesModel dependenciesModel;

	public PanelProductDependencies(JLabel depotLabel) {
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

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(labelInfoProductDependenciesTable)
				.addComponent(dependenciesPanel, 3 * Globals.MIN_VSIZE, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(labelInfoProductDependenciesTree)
				.addComponent(dependenciesTreePanel, 3 * Globals.MIN_VSIZE, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(dependenciesTreePanel.getDependenciesTreePathPanel(), 2 * Globals.MIN_VSIZE,
						2 * Globals.MIN_VSIZE, 2 * Globals.MIN_VSIZE));

	}

	public void clearEditing() {
		dependenciesModel.setActualProduct("");
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
}
