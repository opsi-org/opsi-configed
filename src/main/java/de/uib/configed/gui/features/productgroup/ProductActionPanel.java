/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productgroup;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ServerActionManager;
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.features.productpage.PanelProductSettings;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.searchpane.SearchPaneComponent;
import de.uib.configed.gui.features.searchpane.SearchPaneMsg;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModelFromTable;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.list.ListCellRendererByIndex;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ProductActionPanel extends JPanel {
	private SearchPaneComponent searchPane;
	private JComponent component;

	private JButton buttonReloadProductStates;
	private JButton buttonExecuteNow;

	private JComboBox<String> comboVisibility;

	private PanelProductSettings panelProductSettings;

	private ProductSettingsType type;

	public ProductActionPanel(PanelProductSettings panelProductSettings, ProductSettingsType type) {
		this.panelProductSettings = panelProductSettings;
		this.type = type;

		initData();

		initComponents(type);
	}

	public void restoreFilter() {
		searchPane.dispatch(new SearchPaneMsg.ActionMsg.RestoreFilter());
	}

	public boolean isFilteredBySelection() {
		return searchPane.isFilteredBySelection();
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
		FilterKey filterKey = type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? FilterKey.LOCALBOOT_PRODUCTS_TABLE
				: FilterKey.NETBOOT_PRODUCTS_TABLE;
		searchPane = new SearchPaneComponent(new SearchTargetModelFromInstallationStateTable(panelProductSettings),
				filterKey, false, false, true);
		component = searchPane.initUI();
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
		labelStrip.setMinimumSize(new Dimension());

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

		this.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + " 0 0 0, fillx", "", "[]0"));
		this.add(component, "span, growx, gapbottom " + Globals.GAP_SIZE + ", wrap");
		this.add(toolBarActions, "aligny center, split 2, gapbottom " + Globals.GAP_SIZE);
		this.add(toolBarSetValues, "aligny center, pushx, gapbefore push, gapbottom " + Globals.GAP_SIZE + ", wrap");
	}

	private void addLocalbootSpecificActions(JToolBar toolBarActions) {
		buttonExecuteNow = new JButton(Icons.getIntellijIcon("run"));
		buttonExecuteNow.setToolTipText(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));

		JLabel labelVisibility = new JLabel(Configed.getResourceValue("GroupPanel.labelVisibility"));
		labelVisibility.setToolTipText(Configed.getResourceValue("GroupPanel.labelVisibility.tooltip"));
		labelVisibility.setMinimumSize(new Dimension());

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

		panelProductSettings.getProductTable().setPendingSelection(saveSelectedProducts);
	}

	public void setFilterMark(boolean selected) {
		// searchPane.setFilterMark(selected);
	}

	@SuppressWarnings("java:S2972")
	private static class SearchTargetModelFromInstallationStateTable extends SearchTargetModelFromTable {
		private PanelProductSettings panelProductSettings;

		public SearchTargetModelFromInstallationStateTable(PanelProductSettings panelProductSettings) {
			super(panelProductSettings.getProductTable());
			Logging.info(this, "table null? ", table == null);

			this.panelProductSettings = panelProductSettings;
		}

		@Override
		public void setCursorRow(int row) {
			Logging.debug(this, "setCursorRow row, produced modelrow, produced viewrow, not implemented ");
		}

		@Override
		public void setFiltered(boolean filtered) {
			if (filtered) {
				selectedRows = table.getSelectedRows();

				int[] modelRowFilter = new int[selectedRows.length];
				for (int i = 0; i < selectedRows.length; i++) {
					modelRowFilter[i] = table.convertRowIndexToModel(selectedRows[i]);
				}

				Logging.info(this, "setFiltered modelRowFilter ", Arrays.toString(modelRowFilter));

				if (selectedRows.length != 0) {
					panelProductSettings.getProductTable().reduceToSelected();
				}
			} else {
				panelProductSettings.valueChanged(false);
			}
		}
	}

}
