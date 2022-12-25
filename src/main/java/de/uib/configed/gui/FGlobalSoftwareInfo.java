package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;

/**
 * FGlobalSoftwareInfo
 * Copyright:     Copyright (c) 2017
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class FGlobalSoftwareInfo extends FGeneralDialog {
	public PanelGenEditTable panelGlobalSoftware;
	private GenTableModel model;

	public Vector<String> columnNames;
	public Vector<String> classNames;
	TableUpdateCollection updateCollection;

	protected int keyCol = 0;

	PersistenceController persist;

	ControlPanelAssignToLPools myController;

	public FGlobalSoftwareInfo(JFrame owner, ControlPanelAssignToLPools myController) {
		super(
				// Globals.mainFrame,
				owner, configed.getResourceValue("FGlobalSoftwareInfo.title"), false,
				new String[] { configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove"),
						configed.getResourceValue("FGlobalSoftwareInfo.buttonClose") },
				10, 10); // initial size of super frame

		this.myController = myController;

		persist = PersistenceControllerFactory.getPersistenceController();

		panelGlobalSoftware = new PanelGenEditTable("", 
				
				0, // width
				false, // editing,
				2, true // switchLineColors
		);

		allpane.add(panelGlobalSoftware, BorderLayout.CENTER);
		JLabel infoLabel = new JLabel(configed.getResourceValue("FGlobalSoftwareInfo.info"));
		additionalPane.add(infoLabel);
		additionalPane.setBackground(Globals.backLightBlue);
		additionalPane.setVisible(true);

		setSize(new Dimension(infoLabel.getPreferredSize().width + 100, 300));
		owner.setVisible(true);

		jButton1.setEnabled(false);
		jButton1.setIcon(Globals.createImageIcon("images/edit-delete.png", ""));
		jButton2.setIcon(Globals.createImageIcon("images/cancel.png", ""));

		initDataStructure();

	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}
	/*
	 * @Override
	 * protected void initScrollPane()
	 * 
	 * //therefore override with nothing
	 * {
	 * }
	 */

	protected void initDataStructure() {
		columnNames = new Vector<>();
		columnNames.add("ID");
		for (String key : de.uib.configed.type.SWAuditEntry.KEYS_FOR_IDENT)
			columnNames.add(key);

		classNames = new Vector<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new TableUpdateCollection();

		panelGlobalSoftware.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelGlobalSoftware.addListSelectionListener(listSelectionEvent -> {
			if (!listSelectionEvent.getValueIsAdjusting())
				jButton1.setEnabled(panelGlobalSoftware.getTheTable().getSelectedRowCount() > 0);
		});
	}

	public void setTableModel(GenTableModel model) {
		if (model == null)
		// test
		{
			this.model = new GenTableModel(null, // no updates
					new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
						persist.installedSoftwareInformationRequestRefresh();
						return (Map) persist.getInstalledSoftwareInformation();
					})
					// ,

					),

					keyCol, 
					new int[] {}, panelGlobalSoftware, updateCollection);
		} else
			this.model = model;

		panelGlobalSoftware.setTableModel(this.model);
	}

	@Override
	public void doAction1() {
		logging.debug(this, "doAction1");

		logging.info(this, "removeAssociations for " + " licencePool " + myController.getSelectedLicencePool()
				+ " selected SW keys " + panelGlobalSoftware.getSelectedKeys());

		boolean success = persist.removeAssociations(myController.getSelectedLicencePool(),
				panelGlobalSoftware.getSelectedKeys());

		if (success) {
			for (String key : panelGlobalSoftware.getSelectedKeys()) {
				int row = panelGlobalSoftware.findViewRowFromValue(key, keyCol);
				logging.info(this, "doAction1 key, " + key + ", row " + row);
				logging.info(this,
						"doAction1 model row " + panelGlobalSoftware.getTheTable().convertRowIndexToModel(row));
				panelGlobalSoftware.getTableModel()
						.deleteRow(panelGlobalSoftware.getTheTable().convertRowIndexToModel(row));
			}
			result = 1;
		}

	}

	@Override
	public void doAction2() {
		logging.debug(this, "doAction2");
		result = 2;
		owner.setVisible(true);
		leave();
	}

	@Override
	public void leave() {
		setVisible(false);
		// we dont dispose the window, dispose it in the enclosing class

	}

	public void exit() {
		super.leave();
	}

}
