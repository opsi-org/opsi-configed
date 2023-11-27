/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TablesearchPane;

public class ValueSelectorList extends JPanel {
	private DepotsList valueList;
	private JScrollPane scrollPaneValueList;
	// this will not be shown in this panel but exported for use in other panels

	private JLabel labelValue;

	private TablesearchPane searchPane;

	private boolean multidepot;

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

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelValue, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(searchPane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelValue, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE)));
	}

	public String getSelectedValue() {
		return valueList.getSelectedValue();
	}
}
