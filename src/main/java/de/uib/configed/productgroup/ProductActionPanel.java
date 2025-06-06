/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productgroup;

import java.awt.Dimension;
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
import javax.swing.JToolBar;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.ServerActionManager;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.gui.productpage.PanelProductSettings.ProductSettingsType;
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

	private JComboBox<String> comboVisibility;

	private PanelProductSettings panelProductSettings;

	public ProductActionPanel(PanelProductSettings panelProductSettings, ProductSettingsType type) {
		this.panelProductSettings = panelProductSettings;

		initData();

		initComponents(type);
	}

	public void updateSearchFields() {
		searchPane.setSearchFieldsAll();
	}

	public void setReloadActionHandler(ActionListener al) {
		buttonReloadProductStates.addActionListener(al);
	}

	public void setSaveAndExecuteActionHandler(ActionListener al) {
		buttonExecuteNow.addActionListener(al);
	}

	public String getVisibility() {
		return switch (comboVisibility.getSelectedIndex()) {
		case 0 -> ServerActionManager.KEY_PROCESS_ACTION_REQUEST_DEFAULT;
		case 1 -> ServerActionManager.KEY_PROCESS_ACTION_REQUEST_VISIBLE;
		case 2 -> ServerActionManager.KEY_PROCESS_ACTION_REQUEST_HIDDEN;
		default -> ServerActionManager.KEY_PROCESS_ACTION_REQUEST_DEFAULT;
		};
	}

	private void initData() {
		searchPane = new TableSearchPane(new SearchTargetModelFromInstallationStateTable(panelProductSettings));
	}

	private void initComponents(ProductSettingsType type) {
		buttonReloadProductStates = new JButton(Icons.getIntellijIcon("refresh"));
		buttonReloadProductStates.setToolTipText(Configed.getResourceValue("GroupPanel.ReloadProductStatesTooltip"));

		Map<String, String> values = new LinkedHashMap<>();
		values.put("setup", Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked.tooltip"));
		values.put("uninstall", Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked.tooltip"));
		values.put("none", Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked.tooltip"));
		// create list with tooltips
		JComboBox<String> jComboBox = new JComboBox<>(values.keySet().toArray(new String[0]));
		jComboBox.setRenderer(new ListCellRendererByIndex(values));

		JButton buttonSetValues = new JButton(Icons.getIntellijIcon("checkmark"));
		buttonSetValues.setToolTipText(Configed.getResourceValue("GroupPanel.applyOnSelectedProducts"));
		buttonSetValues.addActionListener(actionEvent -> handleCollectiveAction((String) jComboBox.getSelectedItem()));

		JLabel labelStrip = new JLabel(Configed.getResourceValue("GroupPanel.labelAggregateProducts"));

		JToolBar toolBarActions = new JToolBar();
		toolBarActions.add(buttonReloadProductStates);
		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			addLocalbootSpecificActions(toolBarActions);
		}

		JToolBar toolBarSetValues = new JToolBar();
		toolBarSetValues.add(labelStrip);
		toolBarSetValues.addSeparator(new Dimension(Globals.MIN_GAP_SIZE, 0));
		toolBarSetValues.add(jComboBox);
		toolBarSetValues.add(buttonSetValues);

		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);

		layoutMain.setVerticalGroup(layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(
						searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(toolBarActions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(toolBarSetValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		layoutMain.setHorizontalGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(layoutMain.createSequentialGroup()
						.addComponent(toolBarActions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)

						.addComponent(toolBarSetValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));
	}

	private void addLocalbootSpecificActions(JToolBar toolBarActions) {
		buttonExecuteNow = new JButton(Icons.getIntellijIcon("run"));
		buttonExecuteNow.setToolTipText(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));

		JLabel labelVisibility = new JLabel(Configed.getResourceValue("GroupPanel.labelVisibility"));
		labelVisibility.setToolTipText(Configed.getResourceValue("GroupPanel.labelVisibility.tooltip"));

		comboVisibility = new JComboBox<>(
				new String[] { Configed.getResourceValue("GroupPanel.comboVisibility.default"),
						Configed.getResourceValue("GroupPanel.comboVisibility.visible"),
						Configed.getResourceValue("GroupPanel.comboVisibility.hidden") });

		toolBarActions.add(buttonExecuteNow);
		toolBarActions.add(labelVisibility);
		toolBarActions.addSeparator(new Dimension(Globals.GAP_SIZE, 0));
		toolBarActions.add(comboVisibility);
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

			int actionType = switch (selected) {
			case "setup" -> ActionRequest.SETUP;
			case "uninstall" -> ActionRequest.UNINSTALL;
			case "none" -> ActionRequest.NONE;
			default -> ActionRequest.INVALID;
			};

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
}
