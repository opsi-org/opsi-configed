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
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelLicensesUsage;
import de.uib.configed.Globals;
import de.uib.utilities.swing.AutoCompletionComboBox;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelLicensesUsage extends MultiTablePanel {
	private JSplitPane splitPane;

	private PanelGenEditTable panelUsage;
	private PanelGenEditTable panelLicensePools;

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
		panelLicensePools = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicensepools"), false, 0, false,
				new int[] { PanelGenEditTable.POPUP_RELOAD });

		panelGetAndAssignSL = new JPanel();
		JLabel labelGetAndAssignSL = new JLabel(
				Configed.getResourceValue("ConfigedMain.Licenses.Usage.LabelAssignLicense"));

		comboClient = new AutoCompletionComboBox<>();
		comboClient.setPreferredSize(new Dimension(200, 20));
		comboClient.setEditable(true);

		JButton buttonGet = new JButton(Configed.getResourceValue("ConfigedMain.Licenses.Usage.AssignLicense"));
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
		panelUsage = new PanelGenEditTable(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleUsage"), true,
				0, false, new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);

		panelUsage.setMasterFrame(ConfigedMain.getLicensesFrame());
		panelUsage.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelUsage.setFiltering(true);
		panelUsage.showFilterIcon(true);

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

	public PanelGenEditTable getPanelUsage() {
		return panelUsage;
	}

	public PanelGenEditTable getPanelLicensePools() {
		return panelLicensePools;
	}
}
