/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

/*
 * PanelEditLicenses.java
 * for backend editing of three tables 
 *
 */

package de.uib.configed.gui.features.licenses;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import net.miginfocom.swing.MigLayout;

public class PanelEditLicenses extends MultiTablePanel {
	private PanelGenEdit panelKeys;
	private PanelGenEdit panelSoftwarelicenses;
	private PanelGenEdit panelLicensecontracts;

	private int minVSize = 100;

	public PanelEditLicenses(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		panelKeys = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicenseOptionsView"),
				true, 1, new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelKeys.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelKeys.setFilterKey(FilterKey.LICENSE_KEYS_EDIT_TABLE);

		panelSoftwarelicenses = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSoftwarelicense"), true, 2,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelSoftwarelicenses.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelSoftwarelicenses.setFilterKey(FilterKey.LICENSE_SOFTWARE_TABLE);

		panelLicensecontracts = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensecontract"), true, 2,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelLicensecontracts.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelLicensecontracts.setFilterKey(FilterKey.LICENSE_CONTRACTS_EDIT_TABLE);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelLicensecontracts.setAwareOfTableChangedListener(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5);

		JPanel topPane = new JPanel(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));

		topPane.add(panelKeys, "grow");
		topPane.add(panelSoftwarelicenses, "grow");

		JPanel bottomPane = new JPanel(new MigLayout(
				"insets " + Globals.GAP_SIZE + " 0 " + Globals.MIN_GAP_SIZE + " 0, fill", "[grow]", "[grow]"));

		bottomPane.add(panelLicensecontracts, "grow, hmin " + minVSize);

		this.setLayout(new MigLayout("insets 0, fill", "[]", "[]0"));
		this.add(splitPane, "grow");

		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);
	}

	public PanelGenEdit getPanelKeys() {
		return panelKeys;
	}

	public PanelGenEdit getPanelSoftwarelicenses() {
		return panelSoftwarelicenses;
	}

	public PanelGenEdit getPanelLicensecontracts() {
		return panelLicensecontracts;
	}
}
