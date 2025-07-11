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

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.data.DependenciesModel;
import de.uib.configed.gui.data.DependenciesModel.DependenciesModelListener;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.share.logging.Logging;

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

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoProductDependenciesTable))

				.addComponent(dependenciesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)

				.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoProductDependenciesTree))

				.addComponent(dependenciesTreePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(dependenciesTreePanel.getDependenciesTreePathPanel(), GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelInfoProductDependenciesTable).addComponent(dependenciesPanel, 0, 0, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(labelInfoProductDependenciesTree)
				.addComponent(dependenciesTreePanel, 0, 0, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)

				// We need to add 2 to the height of the tree path panel because the border
				.addComponent(dependenciesTreePanel.getDependenciesTreePathPanel(), Globals.DEFAULT_JLABEL_HEIGHT + 2,
						Globals.DEFAULT_JLABEL_HEIGHT + 2, Globals.DEFAULT_JLABEL_HEIGHT + 2));
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
