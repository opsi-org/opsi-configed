/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicensesUsage.java
 *
 */

package de.uib.configed.gui.licenses;

import java.awt.Dimension;

import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ControlPanelLicensesUsage;
import de.uib.configed.Globals;
import de.uib.utils.swing.AutoCompletionComboBox;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.gui.FilterKey;
import de.uib.utils.table.gui.PanelGenEdit;

public class PanelLicensesUsage extends MultiTablePanel {
	private JSplitPane splitPane;

	private PanelGenEdit panelUsage;
	private PanelGenEdit panelLicensePools;

	private JPanel panelGetAndAssignSL;
	private JComboBox<String> comboClient;

	private int lPoolHeight = 100;

	private ControlPanelLicensesUsage licensesUsageController;
	private int initialSplit;

	public PanelLicensesUsage(ControlPanelLicensesUsage licensesUsageController) {
		super(licensesUsageController);
		this.licensesUsageController = licensesUsageController;
		initSubPanel();
		initComponents();
	}

	public void setDivider() {
		if (initialSplit < 1) {
			splitPane.setDividerLocation(0.7);
			initialSplit++;
			revalidate();
		}
	}

	private void initSubPanel() {
		panelLicensePools = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicensepools"), false, 0,
				new int[] { PopupMenuTrait.POPUP_RELOAD });
		panelLicensePools.setFilterKey(FilterKey.LICENSE_POOL_USAGE_TABLE);

		panelGetAndAssignSL = new JPanel();
		JLabel labelGetAndAssignSL = new JLabel(
				Configed.getResourceValue("ConfigedMain.Licenses.Usage.LabelAssignLicense"));

		comboClient = new AutoCompletionComboBox<>();
		comboClient.setPreferredSize(new Dimension(200, 20));
		comboClient.setEditable(true);

		JButton buttonGet = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonGet.addActionListener(
				event -> licensesUsageController.getSoftwareLicenseReservation((String) comboClient.getSelectedItem()));

		GroupLayout panelGetAndAssignSLLayout = new GroupLayout(panelGetAndAssignSL);
		panelGetAndAssignSL.setLayout(panelGetAndAssignSLLayout);
		panelGetAndAssignSLLayout.setHorizontalGroup(panelGetAndAssignSLLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(panelGetAndAssignSLLayout.createSequentialGroup().addComponent(labelGetAndAssignSL)
						.addGap(20, 20, 20)
						.addComponent(comboClient, GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE))
				.addComponent(panelLicensePools, Alignment.TRAILING, 20, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(panelGetAndAssignSLLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
						buttonGet, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)));

		panelGetAndAssignSLLayout.setVerticalGroup(panelGetAndAssignSLLayout.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(panelGetAndAssignSLLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(labelGetAndAssignSL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE).addComponent(panelLicensePools, lPoolHeight, lPoolHeight, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(buttonGet, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private void initComponents() {
		panelUsage = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleUsage"), true, 0,
				new int[] { PanelGenEdit.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE, PanelGenEdit.POPUP_CANCEL,
						PopupMenuTrait.POPUP_RELOAD },
				true);

		panelUsage.getJTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelUsage.setFilterKey(FilterKey.LICENSE_USAGE_TABLE);

		panelUsage.getTableSearchPane().setFiltering();

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));
		splitPane.setTopComponent(panelUsage);
		splitPane.setBottomComponent(panelGetAndAssignSL);
		splitPane.setResizeWeight(0.5);
	}

	public void setClientsSource(ComboBoxModel<String> modelsource) {
		comboClient.setModel(modelsource);
	}

	public PanelGenEdit getPanelUsage() {
		return panelUsage;
	}

	public PanelGenEdit getPanelLicensePools() {
		return panelLicensePools;
	}
}
