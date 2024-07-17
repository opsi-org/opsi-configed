/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.PanelGenEditTable;

public class FGlobalSoftwareInfo extends FGeneralDialog {
	private PanelGenEditTable panelGlobalSoftware;

	private List<String> columnNames;

	private int keyCol;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools myController;

	public FGlobalSoftwareInfo(JFrame owner, ControlPanelAssignToLPools myController) {
		super(owner, Configed.getResourceValue("FGlobalSoftwareInfo.title"), false,
				new String[] { Configed.getResourceValue("buttonClose"),
						Configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove") },
				10, 10);

		this.myController = myController;

		panelGlobalSoftware = new PanelGenEditTable("", false, 2);

		allpane.add(panelGlobalSoftware, BorderLayout.CENTER);
		JLabel infoLabel = new JLabel(Configed.getResourceValue("FGlobalSoftwareInfo.info"));
		additionalPane.add(infoLabel);
		additionalPane.setVisible(true);

		super.setSize(new Dimension(infoLabel.getPreferredSize().width + 100, 300));

		jButton1.setEnabled(false);

		initDataStructure();
	}

	private void initDataStructure() {
		columnNames = new ArrayList<>();
		columnNames.add("ID");
		for (String key : SWAuditEntry.KEYS_FOR_IDENT) {
			columnNames.add(key);
		}

		panelGlobalSoftware.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelGlobalSoftware.addListSelectionListener((ListSelectionEvent listSelectionEvent) -> {
			if (!listSelectionEvent.getValueIsAdjusting()) {
				jButton1.setEnabled(panelGlobalSoftware.getTheTable().getSelectedRowCount() > 0);
			}
		});
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public void setTableModel(GenTableModel model) {
		panelGlobalSoftware.setTableModel(model);
	}

	@Override
	public void doAction1() {
		Logging.debug(this, "doAction1");
		result = 1;
		getOwner().setVisible(true);
		leave();
	}

	@Override
	public void doAction2() {
		Logging.debug(this, "doAction2");

		Logging.info(this, "removeAssociations for ", " licensePool ", myController.getSelectedLicensePool(),
				" selected SW keys ", panelGlobalSoftware.getSelectedKeys());

		boolean success = persistenceController.getSoftwareDataService()
				.removeAssociations(myController.getSelectedLicensePool(), panelGlobalSoftware.getSelectedKeys());

		if (success) {
			for (String key : panelGlobalSoftware.getSelectedKeys()) {
				int row = panelGlobalSoftware.findViewRowFromValue(key, keyCol);
				Logging.info(this, "doAction2 key, ", key, ", row ", row);
				Logging.info(this, "doAction2 model row ",
						panelGlobalSoftware.getTheTable().convertRowIndexToModel(row));
				panelGlobalSoftware.getTableModel()
						.deleteRow(panelGlobalSoftware.getTheTable().convertRowIndexToModel(row));
			}
			result = 2;
		}
	}

	@Override
	public void leave() {
		setVisible(false);
		// we dont dispose the window, dispose it in the enclosing class
	}

	public PanelGenEditTable getPanelGlobalSoftware() {
		return panelGlobalSoftware;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}
}
