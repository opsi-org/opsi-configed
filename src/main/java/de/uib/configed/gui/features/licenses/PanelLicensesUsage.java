/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

/*
 * PanelLicensesUsage.java
 *
 */

package de.uib.configed.gui.features.licenses;

import java.awt.Dimension;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ControlPanelLicensesUsage;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.AutoCompletionComboBox;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import net.miginfocom.swing.MigLayout;

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

		JLabel labelGetAndAssignSL = new JLabel(
				Configed.getResourceValue("ConfigedMain.Licenses.Usage.LabelAssignLicense"));

		comboClient = new AutoCompletionComboBox<>();
		comboClient.setPreferredSize(new Dimension(200, 20));
		comboClient.setEditable(true);

		JButton buttonGet = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonGet.addActionListener(
				event -> licensesUsageController.getSoftwareLicenseReservation((String) comboClient.getSelectedItem()));

		panelGetAndAssignSL = new JPanel();
		panelGetAndAssignSL.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", fill", "",
				"[][grow][" + Globals.MIN_GAP_SIZE + "][pref!]"));
		panelGetAndAssignSL.add(labelGetAndAssignSL, "split 2");
		panelGetAndAssignSL.add(comboClient, "width 263!, gapleft 20, wrap");
		panelGetAndAssignSL.add(panelLicensePools, "span, grow, push, wrap, hmin " + lPoolHeight);
		panelGetAndAssignSL.add(buttonGet);
	}

	private void initComponents() {
		panelUsage = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleUsage"), true, 0,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);

		panelUsage.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelUsage.setFilterKey(FilterKey.LICENSE_USAGE_TABLE);

		panelUsage.getTableSearchPane().setFiltering();

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		this.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", fill", "", "[]0"));
		this.add(splitPane, "grow");

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
