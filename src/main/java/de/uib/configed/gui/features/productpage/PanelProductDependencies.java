/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.data.DependenciesModel;
import de.uib.configed.gui.data.DependenciesModel.DependenciesModelListener;
import de.uib.configed.gui.data.RequirementsTableModel;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.features.table.TableConfig;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class PanelProductDependencies extends JPanel implements DependenciesModelListener {
	// The label shown on top of the dependencies-panel
	private JLabel depotLabel;
	private DependenciesTreePanel dependenciesTreePanel;

	private GenericTableViewComponent tableViewComponent;

	private DependenciesModel dependenciesModel;

	public PanelProductDependencies(JLabel depotLabel) {
		this.depotLabel = depotLabel;

		initComponents();
	}

	private static List<TableColumnConfig> buildColumnConfigs() {
		List<TableColumnConfig> columns = new ArrayList<>();

		columns.add(TableColumnConfig.builder().key(RequirementsTableModel.COL_REQUIRED_PRODUCT)
				.header(Configed.getResourceValue("ProductInfoPane.RequirementsTable.requiredProduct")).editable(false)
				.prefferedWidth(200).toggleable(false).renderer(new ColorTableCellRenderer()).build());

		columns.add(TableColumnConfig.builder().key(RequirementsTableModel.COL_REQUIREMENT_DEFAULT)
				.header(Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeDefault"))
				.editable(false).prefferedWidth(150).renderer(new ColorTableCellRenderer()).build());

		columns.add(TableColumnConfig.builder().key(RequirementsTableModel.COL_REQUIREMENT_BEFORE)
				.header(Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeBefore"))
				.editable(false).prefferedWidth(150).renderer(new ColorTableCellRenderer()).build());

		columns.add(TableColumnConfig.builder().key(RequirementsTableModel.COL_REQUIREMENT_AFTER)
				.header(Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeAfter"))
				.editable(false).prefferedWidth(150).renderer(new ColorTableCellRenderer()).build());

		return columns;
	}

	private void initComponents() {
		JLabel labelInfoProductDependenciesTable = new JLabel(
				Configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTable"));
		JLabel labelInfoProductDependenciesTree = new JLabel(
				Configed.getResourceValue("PanelProductDependencies.labelInfoProductDependenciesTree"));

		TableConfig tableConfig = TableConfig.builder().fillViewportHeight(true).showTableHeader(true)
				.dragEnabled(false).autoCreateRowSorter(false).reorderingAllowed(false).columnSelectionAllowed(false)
				.enableHeaderContextMenu(false).selectionMode(ListSelectionModel.SINGLE_SELECTION).sortKeys(null)
				.build();

		GenericTableViewModel model = GenericTableViewModel.builder().rows(new ArrayList<>())
				.columns(buildColumnConfigs()).tableConfig(tableConfig).diffStrategy(null).keyValueTable(false)
				.isDirty(false).build();

		tableViewComponent = new GenericTableViewComponent(model, null, null);

		dependenciesTreePanel = new DependenciesTreePanel();

		this.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow, 0:0]",
				"[]0[grow,fill]" + Globals.MIN_GAP_SIZE + "[]0[grow,fill]" + Globals.MIN_GAP_SIZE + "[]"));
		this.add(labelInfoProductDependenciesTable, "gapleft " + Globals.MIN_GAP_SIZE);
		this.add(tableViewComponent.initUI(), "grow, push");
		this.add(labelInfoProductDependenciesTree, "gapleft " + Globals.MIN_GAP_SIZE);
		this.add(dependenciesTreePanel, "grow, push, gapleft " + Globals.MIN_GAP_SIZE);
		this.add(dependenciesTreePanel.getDependenciesTreePathPanel(),
				"height " + (Globals.DEFAULT_JLABEL_HEIGHT + 2) + "!, growx");
	}

	public void setDependenciesModel(DependenciesModel dependenciesModel) {
		this.dependenciesModel = dependenciesModel;

		dependenciesModel.addListener(this);

		dependenciesModel.getRequirementsModel().buildRequirementRows();

		dependenciesTreePanel.setDependenciesTreeModel(dependenciesModel.getDependenciesTreeModel());
	}

	public void setEditValues(String productId, String depotId) {
		Logging.info(this, "set product  ", productId);

		dependenciesModel.setActualProduct(depotId, productId);
	}

	@Override
	public void updateProduct(String productId) {
		List<Map<String, Object>> rows = dependenciesModel.getRequirementsModel().buildRequirementRows();

		tableViewComponent.dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(rows));

		dependenciesTreePanel.updateTree();
	}

	@Override
	public void updateDepot(String depotId) {
		depotLabel.setText(Configed.getResourceValue("depot") + ": " + depotId);
	}
}
