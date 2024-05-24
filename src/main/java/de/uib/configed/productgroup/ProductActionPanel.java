/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productgroup;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.SearchTargetModelFromInstallationStateTable;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.ListCellRendererByIndex;
import de.uib.utils.swing.list.StandardListCellRenderer;
import de.uib.utils.table.gui.TableSearchPane;

public class ProductActionPanel extends JPanel {
	private TableSearchPane searchPane;
	private JTable tableProducts;

	private JButton buttonReloadProductStates;

	private JButton buttonExecuteNow;

	private PanelProductSettings associate;

	public ProductActionPanel(PanelProductSettings associate, JTable table) {
		this.associate = associate;
		this.tableProducts = table;

		initData();

		initComponents();
	}

	public void updateSearchFields() {
		searchPane.setSearchFieldsAll();
	}

	public void setFilteredMode(boolean b) {
		Logging.debug(this, "setGuiIsFiltered " + b);
		searchPane.setFilteredMode(b);
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
		searchPane = new TableSearchPane(new SearchTargetModelFromInstallationStateTable(tableProducts, associate),
				true);
		searchPane.setSearchMode(TableSearchPane.SearchMode.FULL_TEXT_SEARCH);
		searchPane.setFiltering(true);

		// filter icon inside searchpane
		searchPane.showFilterIcon(true);
	}

	private void initComponents() {
		buttonReloadProductStates = new JButton(Utils.getIntellijIcon("refresh"));
		buttonReloadProductStates.setToolTipText(Configed.getResourceValue("GroupPanel.ReloadProductStatesTooltip"));
		buttonReloadProductStates.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		buttonExecuteNow = new JButton(Utils.getIntellijIcon("run"));
		buttonExecuteNow.setToolTipText(Configed.getResourceValue("ConfigedMain.Opsiclientd.executeAll"));
		buttonExecuteNow.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		Map<String, String> values = new LinkedHashMap<>();

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked.tooltip"));

		DefaultListModel<String> modelChooseAction = new DefaultListModel<>();
		for (String key : values.keySet()) {
			modelChooseAction.addElement(key);
		}

		// create list with tooltips
		JList<String> listChooseAction = new JList<>(modelChooseAction);
		StandardListCellRenderer renderActionList = new ListCellRendererByIndex(values);

		listChooseAction.setCellRenderer(renderActionList);
		listChooseAction.setVisibleRowCount(2);

		JScrollPane scrollChooseAction = new JScrollPane(listChooseAction,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listChooseAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() > 1) {
					String s = listChooseAction.getSelectedValue();
					handleCollectiveAction(s, (InstallationStateTableModel) tableProducts.getModel());
				}
			}
		});

		listChooseAction.setSelectedIndex(0);

		JLabel labelStrip = new JLabel("  " + Configed.getResourceValue("GroupPanel.labelAggregateProducts"));

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

						.addGroup(layoutMain.createSequentialGroup()
								.addComponent(labelStrip, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollChooseAction, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, 3 * Globals.LINE_HEIGHT)))
				.addGap(Globals.GAP_SIZE));

		layoutMain.setHorizontalGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(buttonExecuteNow, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE)
						.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE)

						.addGroup(layoutMain.createParallelGroup()
								.addGroup(layoutMain.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addComponent(
										labelStrip, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										Short.MAX_VALUE))
								.addGroup(layoutMain.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addComponent(
										scrollChooseAction, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)))

						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)));
	}

	private void handleCollectiveAction(String selected, InstallationStateTableModel insTableModel) {
		Set<String> saveSelectedProducts = associate.getSelectedIDs();

		Logging.info(this, "handleCollectiveAction, selected products " + associate.getSelectedRowsInModelTerms());
		Logging.info(this, "handleCollectiveAction, selected products " + saveSelectedProducts);

		if (!insTableModel.infoIfNoClientsSelected()) {
			insTableModel.initCollectiveChange();

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
				associate.getSelectedRowsInModelTerms().stream().forEach((Integer x) -> {
					Logging.info(" row id " + x + " product " + insTableModel.getValueAt(x, 0));
					insTableModel.collectiveChangeActionRequest((String) insTableModel.getValueAt(x, 0),
							new ActionRequest(actionType));
				});
			}

			insTableModel.finishCollectiveChange();
		}

		associate.setSelection(new HashSet<>(saveSelectedProducts));
	}
}
