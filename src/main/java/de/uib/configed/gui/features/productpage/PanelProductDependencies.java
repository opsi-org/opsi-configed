/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.data.DependenciesModel;
import de.uib.configed.gui.data.DependenciesModel.DependenciesModelListener;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelProductDependencies extends JPanel implements DependenciesModelListener {
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

		dependenciesPanel.setViewportView(dependenciesTable);

		this.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow, 0:0]",
				"[]0[grow,fill]" + Globals.MIN_GAP_SIZE + "[]0[grow,fill]" + Globals.MIN_GAP_SIZE + "[]"));
		this.add(labelInfoProductDependenciesTable, "gapleft " + Globals.MIN_GAP_SIZE);
		this.add(dependenciesPanel, "grow, push");
		this.add(labelInfoProductDependenciesTree, "gapleft " + Globals.MIN_GAP_SIZE);
		this.add(dependenciesTreePanel, "grow, push, gapleft " + Globals.MIN_GAP_SIZE);
		this.add(dependenciesTreePanel.getDependenciesTreePathPanel(),
				"height " + (Globals.DEFAULT_JLABEL_HEIGHT + 2) + "!, growx");
	}

	public void setDependenciesModel(DependenciesModel dependenciesModel) {
		this.dependenciesModel = dependenciesModel;

		dependenciesModel.addListener(this);

		dependenciesTable.setModel(dependenciesModel.getRequirementsModel());
		dependenciesTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());

		dependenciesTreePanel.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}

	public void setEditValues(String productId, String depotId) {
		Logging.info(this, "set product  ", productId);

		dependenciesModel.setActualProduct(depotId, productId);
	}

	@Override
	public void updateProduct(String productId) {
		dependenciesTreePanel.updateTree();
	}

	@Override
	public void updateDepot(String depotId) {
		depotLabel.setText(Configed.getResourceValue("depot") + ": " + depotId);
	}
}
