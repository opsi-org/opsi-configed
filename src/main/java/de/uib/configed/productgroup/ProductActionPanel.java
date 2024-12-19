/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productgroup;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.SearchTargetModelFromInstallationStateTable;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.ListCellRendererByIndex;
import de.uib.utils.table.gui.TableSearchPane;

public class ProductActionPanel extends JPanel {
	private TableSearchPane searchPane;

	private JButton buttonReloadProductStates;
	private JButton buttonExecuteNow;

	private PanelProductSettings panelProductSettings;

	public ProductActionPanel(PanelProductSettings panelProductSettings) {
		this.panelProductSettings = panelProductSettings;

		initData();

		initComponents();
	}

	public void updateSearchFields() {
		searchPane.setSearchFieldsAll();
	}

	public boolean isFilteredMode() {
		return searchPane.isFilteredMode();
	}

	public void setReloadActionHandler(ActionListener al) {
		buttonReloadProductStates.addActionListener(al);
	}

	public void setSaveAndExecuteActionHandler(ActionListener al) {
		buttonExecuteNow.addActionListener(al);
	}

	private void initData() {
		searchPane = new TableSearchPane(new SearchTargetModelFromInstallationStateTable(panelProductSettings));
		searchPane.setFiltering();
	}

	private void initComponents() {
		buttonReloadProductStates = new JButton(Icons.getIntellijIcon("refresh"));
		buttonReloadProductStates.setToolTipText(Configed.getResourceValue("GroupPanel.ReloadProductStatesTooltip"));

		buttonExecuteNow = new JButton(Icons.getIntellijIcon("run"));
		buttonExecuteNow.setToolTipText(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));

		Map<String, String> values = new LinkedHashMap<>();

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked.tooltip"));

		// create list with tooltips
		JComboBox<String> jComboBox = new JComboBox<>(values.keySet().toArray(new String[0]));
		jComboBox.setRenderer(new ListCellRendererByIndex(values));

		JButton buttonSetValues = new JButton(Icons.getIntellijIcon("checkmark"));
		buttonSetValues.setToolTipText(Configed.getResourceValue("GroupPanel.applyOnSelectedProducts"));
		buttonSetValues.addActionListener(actionEvent -> handleCollectiveAction((String) jComboBox.getSelectedItem()));

		JLabel labelStrip = new JLabel(Configed.getResourceValue("GroupPanel.labelAggregateProducts"));

		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);

		layoutMain.setVerticalGroup(layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(
						searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonExecuteNow, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelStrip, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSetValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		layoutMain
				.setHorizontalGroup(
						layoutMain.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)

								.addGroup(layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(buttonExecuteNow, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)

										.addComponent(labelStrip, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(jComboBox, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH)
										.addGap(Globals.GAP_SIZE)
										.addComponent(buttonSetValues, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)));
	}

	private void handleCollectiveAction(String selected) {
		Set<String> saveSelectedProducts = panelProductSettings.getProductTable().getSelectedIDs();

		Logging.info(this, "handleCollectiveAction, selected products ",
				panelProductSettings.getProductTable().getSelectedRowsInModelTerms());
		Logging.info(this, "handleCollectiveAction, selected products ", saveSelectedProducts);

		InstallationStateTableModel installationStateTableModel = (InstallationStateTableModel) panelProductSettings
				.getProductTable().getModel();

		if (!installationStateTableModel.infoIfNoClientsSelected()) {
			installationStateTableModel.initCollectiveChange();

			int actionType;
			if (selected.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"))) {
				actionType = ActionRequest.SETUP;
			} else if (selected
					.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"))) {
				actionType = ActionRequest.UNINSTALL;
			} else if (selected.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"))) {
				actionType = ActionRequest.NONE;
			} else {
				actionType = ActionRequest.INVALID;
			}

			if (actionType != ActionRequest.INVALID) {
				panelProductSettings.getProductTable().getSelectedRowsInModelTerms().stream().forEach((Integer x) -> {
					Logging.info(" row id ", x, " product ", installationStateTableModel.getValueAt(x, 0));
					installationStateTableModel.collectiveChangeActionRequest(
							(String) installationStateTableModel.getValueAt(x, 0), new ActionRequest(actionType));
				});
			}

			installationStateTableModel.finishCollectiveChange();
		}

		panelProductSettings.getProductTable().setSelection(new HashSet<>(saveSelectedProducts));
	}

	public void setFilterMark(boolean selected) {
		searchPane.setFilterMark(selected);
	}
}
