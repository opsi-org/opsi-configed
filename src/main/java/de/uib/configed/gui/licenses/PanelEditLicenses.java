/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelEditLicenses.java
 * for backend editing of three tables 
 *
 */

package de.uib.configed.gui.licenses;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.table.gui.PanelGenEditTable;

public class PanelEditLicenses extends MultiTablePanel {
	private PanelGenEditTable panelKeys;
	private PanelGenEditTable panelSoftwarelicenses;
	private PanelGenEditTable panelLicensecontracts;

	private int minVSize = 100;

	public PanelEditLicenses(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelKeys = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicenseOptionsView"), true, 1,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelKeys.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelKeys.setFiltering(true);

		panelSoftwarelicenses = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSoftwarelicense"), true, 2,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelSoftwarelicenses.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelSoftwarelicenses.setFiltering(true);

		panelLicensecontracts = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensecontract"), true, 2,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelLicensecontracts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelLicensecontracts.setFiltering(true);
		panelLicensecontracts.setAwareOfTableChangedListener(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup()
				.addGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(panelKeys, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(panelSoftwarelicenses, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addComponent(panelKeys, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelSoftwarelicenses, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createSequentialGroup().addComponent(panelLicensecontracts,
				GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(panelLicensecontracts, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(splitPane, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public PanelGenEditTable getPanelKeys() {
		return panelKeys;
	}

	public PanelGenEditTable getPanelSoftwarelicenses() {
		return panelSoftwarelicenses;
	}

	public PanelGenEditTable getPanelLicensecontracts() {
		return panelLicensecontracts;
	}
}
