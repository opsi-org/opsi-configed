package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;

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
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class FGlobalSoftwareInfo extends FGeneralDialog {
	public PanelGenEditTable panelGlobalSoftware;
	private GenTableModel model;

	public ArrayList<String> columnNames;
	public ArrayList<String> classNames;
	TableUpdateCollection updateCollection;

	protected int keyCol = 0;

	PersistenceController persist;

	ControlPanelAssignToLPools myController;

	public FGlobalSoftwareInfo(JFrame owner, ControlPanelAssignToLPools myController) {
		super(
				// de.uib.configed.Globals.mainFrame,
				owner, configed.getResourceValue("FGlobalSoftwareInfo.title"), false,
				new String[] { configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove"),
						configed.getResourceValue("FGlobalSoftwareInfo.buttonClose") },
				10, 10); // initial size of super frame

		this.myController = myController;
		// logging.error(this, " my owner " + owner);
		persist = PersistenceControllerFactory.getPersistenceController();

		panelGlobalSoftware = new PanelGenEditTable("", // "software assigned, but not existing",
				// configed.getResourceValue("ConfigedMain.LicenctiontitleWindowsSoftware2LPool"),
				0, // width
				false, // editing,
				2, true // switchLineColors
		);

		allpane.add(panelGlobalSoftware, BorderLayout.CENTER);
		JLabel infoLabel = new JLabel(configed.getResourceValue("FGlobalSoftwareInfo.info"));
		additionalPane.add(infoLabel);
		additionalPane.setBackground(de.uib.configed.Globals.backLightBlue);
		additionalPane.setVisible(true);
		// additionalPane.setPreferredSize(infoLabel.getPreferredSize());

		setSize(new Dimension(infoLabel.getPreferredSize().width + 100, 300));
		owner.setVisible(true);

		jButton1.setEnabled(false);
		jButton1.setIcon(Globals.createImageIcon("images/edit-delete.png", ""));
		jButton2.setIcon(Globals.createImageIcon("images/cancel.png", ""));

		initDataStructure();
		// setTableModel(null);

	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}
	/*
	 * @Override
	 * protected void initScrollPane()
	 * //we do NOT activate the scroll pane}
	 * //therefore override with nothing
	 * {
	 * }
	 */

	protected void initDataStructure() {
		columnNames = new ArrayList<String>();
		columnNames.add("ID");
		for (String key : de.uib.configed.type.SWAuditEntry.KEYS_FOR_IDENT)
			columnNames.add(key);

		classNames = new ArrayList<String>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new TableUpdateCollection();

		panelGlobalSoftware.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelGlobalSoftware.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				jButton1.setEnabled(panelGlobalSoftware.getTheTable().getSelectedRowCount() > 0);
			}
		});

	}

	public void setTableModel(GenTableModel model) {
		if (model == null)
		// test
		{
			this.model = new GenTableModel(null, // no updates
					new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
						public Map retrieveMap() {
							persist.installedSoftwareInformationRequestRefresh();
							return persist.getInstalledSoftwareInformation();
						}
					})
					// ,

					),

					keyCol, // columnNames.indexOf("ID")
					new int[] {}, (TableModelListener) panelGlobalSoftware, updateCollection);
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
		// owner.setVisible(true);
		// leave();
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
		// setEnabled(false);
	}

	public void exit() {
		super.leave();
	}

}
