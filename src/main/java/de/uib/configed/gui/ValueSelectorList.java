/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TablesearchPane;
import utils.Utils;

public class ValueSelectorList extends JPanel implements ActionListener {
	private DepotsList valueList;
	private JScrollPane scrollPaneValueList;
	// this will not be shown in this panel but exported for use in other panels

	private JLabel labelValue;
	private JButton buttonSelectValuesWithEqualProperties;
	private JButton buttonSelectValuesAll;

	private TablesearchPane searchPane;

	private boolean multidepot;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public ValueSelectorList(DepotsList valueList, boolean multidepot) {
		this.valueList = valueList;
		this.multidepot = multidepot;

		List<String> values = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		Map<String, Map<String, Object>> depotInfo = valueList.getDepotInfo();

		for (Entry<String, Map<String, Object>> depotEntry : depotInfo.entrySet()) {
			values.add(depotEntry.getKey());
			if (depotEntry.getValue() == null || depotEntry.getValue().get("description") == null) {
				descriptions.add("");
			} else {
				descriptions.add((String) depotEntry.getValue().get("description"));
			}
		}

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(valueList, values, descriptions);

		searchPane = new TablesearchPane(searchTargetModel, "depotlist");
		searchPane.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);
		searchPane.setSearchFields(new Integer[] { 0, 1 });
		searchPane.setToolTipTextCheckMarkAllColumns(
				Configed.getResourceValue("ValueSelectorList.checkmarkAllColumns.tooltip"));

		initComponents();
		layouting();
	}

	/**
	 * exports the scrollpane which is produced in this class but displayed in
	 * other components
	 *
	 * @return a scrollpane which shows the depotslist
	 */
	public JScrollPane getScrollpaneDepotslist() {
		return scrollPaneValueList;
	}

	private void initComponents() {
		labelValue = new JLabel();
		if (multidepot) {
			labelValue.setText(Configed.getResourceValue("ValueSelectorList.values"));
		} else {
			labelValue.setText(Configed.getResourceValue("ValueSelectorList.value"));
		}

		buttonSelectValuesWithEqualProperties = new JButton("", Utils.createImageIcon("images/equalplus.png", ""));
		buttonSelectValuesWithEqualProperties
				.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectValuesWithEqualProperties"));
		Utils.formatButtonSmallText(buttonSelectValuesWithEqualProperties);
		buttonSelectValuesWithEqualProperties.addActionListener(this);
		buttonSelectValuesWithEqualProperties.setEnabled(multidepot);

		buttonSelectValuesAll = new JButton("", Utils.createImageIcon("images/plusplus.png", ""));
		buttonSelectValuesAll.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectValuesAll"));
		Utils.formatButtonSmallText(buttonSelectValuesAll);
		buttonSelectValuesAll.addActionListener(this);
		buttonSelectValuesAll.setEnabled(multidepot);

		if (!multidepot) {
			searchPane.setEnabled(false);
		}

		searchPane.setNarrow(true);

		// not visible in this panel

		valueList.setMaximumSize(new Dimension(200, 400));

		scrollPaneValueList = new JScrollPane();
		scrollPaneValueList.getViewport().add(valueList);
		scrollPaneValueList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneValueList.setPreferredSize(valueList.getMaximumSize());
	}

	private void layouting() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5, 5, 10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(labelValue,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				).addGap(5, 5, 10).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(searchPane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(5, 5, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(10)
						.addComponent(labelValue, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

						.addGap(10, 10, 10))
				.addGroup(layout.createSequentialGroup().addGap(5, 5, 5)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(5, 5, 5)));
	}

	public String getSelectedValue() {
		return valueList.getSelectedValue();
	}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonSelectValuesAll) {
			Logging.info(this, "action on buttonSelectValuesAll");

			valueList.selectAll();
		} else if (e.getSource() == buttonSelectValuesWithEqualProperties) {
			Logging.info(this, "action on buttonSelectValuesWithEqualProperties");

			if (valueList.getSelectedIndex() > -1) {
				String depotSelected = valueList.getSelectedValue();
				List<String> depotsWithEqualStock = persistenceController.getDepotDataService()
						.getAllDepotsWithIdenticalProductStock(depotSelected);
				valueList.addToSelection(depotsWithEqualStock);
			}
		} else {
			Logging.warning(this, "unexpected action on source " + e.getSource());
		}
	}
}
