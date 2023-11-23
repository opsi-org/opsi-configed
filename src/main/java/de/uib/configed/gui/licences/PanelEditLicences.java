/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelEditLicences.java
 * for backend editing of three tables 
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelEditLicences extends MultiTablePanel {
	private PanelGenEditTable panelKeys;
	private PanelGenEditTable panelSoftwarelicences;
	private PanelGenEditTable panelLicencecontracts;

	private int minVSize = 100;

	/** Creates new form PanelEditLicences */
	public PanelEditLicences(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelKeys = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceOptionsView"), 0, true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelKeys.setMasterFrame(ConfigedMain.getLicencesFrame());
		panelKeys.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelKeys.showFilterIcon(true);
		panelKeys.setFiltering(true);

		panelSoftwarelicences = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSoftwarelicence"), 0, true, 2, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelSoftwarelicences.setMasterFrame(ConfigedMain.getLicencesFrame());
		panelSoftwarelicences.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelSoftwarelicences.setFiltering(true);
		panelSoftwarelicences.showFilterIcon(true);

		panelLicencecontracts = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencecontract"), 0, true, 2, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelLicencecontracts.setMasterFrame(ConfigedMain.getLicencesFrame());
		panelLicencecontracts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelLicencecontracts.showFilterIcon(true);
		panelLicencecontracts.setFiltering(true);
		panelLicencecontracts.setAwareOfTableChangedListener(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup().addContainerGap()
				.addGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(panelKeys, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(panelSoftwarelicences, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(10, 10, 10));
		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addComponent(panelKeys, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelSoftwarelicences, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
				.addGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						panelLicencecontracts, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(10, 10, 10));
		layoutBottomPane.setVerticalGroup(
				layoutBottomPane.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(panelLicencecontracts, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(splitPane, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public PanelGenEditTable getPanelKeys() {
		return panelKeys;
	}

	public PanelGenEditTable getPanelSoftwarelicences() {
		return panelSoftwarelicences;
	}

	public PanelGenEditTable getPanelLicencecontracts() {
		return panelLicencecontracts;
	}
}
