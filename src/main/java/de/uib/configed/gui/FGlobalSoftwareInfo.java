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

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class FGlobalSoftwareInfo extends FGeneralDialog {
	private PanelGenEditTable panelGlobalSoftware;

	private List<String> columnNames;
	private List<String> classNames;

	private int keyCol;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools myController;

	public FGlobalSoftwareInfo(JFrame owner, ControlPanelAssignToLPools myController) {
		super(owner, Configed.getResourceValue("FGlobalSoftwareInfo.title"), false,
				new String[] { Configed.getResourceValue("FGlobalSoftwareInfo.buttonClose"),
						Configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove") },
				10, 10);

		this.myController = myController;

		panelGlobalSoftware = new PanelGenEditTable("",

				0, // width
				false, // editing,
				2, true // switchLineColors
		);

		allpane.add(panelGlobalSoftware, BorderLayout.CENTER);
		JLabel infoLabel = new JLabel(Configed.getResourceValue("FGlobalSoftwareInfo.info"));
		additionalPane.add(infoLabel);
		if (!Main.THEMES) {
			additionalPane.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		additionalPane.setVisible(true);

		super.setSize(new Dimension(infoLabel.getPreferredSize().width + 100, 300));
		owner.setVisible(true);

		jButton1.setEnabled(false);
		jButton1.setIcon(Utils.createImageIcon("images/cancel.png", ""));
		jButton2.setIcon(Utils.createImageIcon("images/edit-delete.png", ""));

		initDataStructure();
	}

	private void initDataStructure() {
		columnNames = new ArrayList<>();
		columnNames.add("ID");
		for (String key : SWAuditEntry.KEYS_FOR_IDENT) {
			columnNames.add(key);
		}

		classNames = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
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
		owner.setVisible(true);
		leave();
	}

	@Override
	public void doAction2() {
		Logging.debug(this, "doAction2");

		Logging.info(this, "removeAssociations for " + " licencePool " + myController.getSelectedLicencePool()
				+ " selected SW keys " + panelGlobalSoftware.getSelectedKeys());

		boolean success = persistenceController.removeAssociations(myController.getSelectedLicencePool(),
				panelGlobalSoftware.getSelectedKeys());

		if (success) {
			for (String key : panelGlobalSoftware.getSelectedKeys()) {
				int row = panelGlobalSoftware.findViewRowFromValue(key, keyCol);
				Logging.info(this, "doAction2 key, " + key + ", row " + row);
				Logging.info(this,
						"doAction2 model row " + panelGlobalSoftware.getTheTable().convertRowIndexToModel(row));
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

	public List<String> getClassNames() {
		return classNames;
	}
}
