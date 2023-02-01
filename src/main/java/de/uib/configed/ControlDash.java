/*
* ControlDash.java
* part of
* (open pc server integration) www.opsi.org
*
* Copyright (c) 2021 uib.de
*
* This program is free software; you may redistribute it and/or
* modify it under the terms of the GNU General Public
* License, version AGPLv3, as published by the Free Software Foundation
*
*/

package de.uib.configed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;

import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.PanelDashControl;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlDash {
	PersistenceController persist;

	public static final String CONFIG_KEY = "configed.dash_config";

	protected PanelDashControl panelDash;

	protected static FTextArea fDash;

	protected String message = "";

	private static ControlDash instance;

	public static ControlDash getInstance(PersistenceController persis) {
		if (instance == null) {
			instance = new ControlDash(persis);
			instance.loadData();
		}

		return instance;
	}

	private ControlDash(PersistenceController persis) {
		Logging.info(this, "ControlDash constructed");
		persist = persis;
		loadData();
	}

	public void loadData() {

		SwingUtilities.invokeLater(() -> {
			message = "";
			showInfo();

			StringBuilder mess = new StringBuilder();

			mess.append(Configed.getResourceValue("Dash.topicLicences1"));
			mess.append("\n");
			mess.append("\n");

			if (!persist.isWithLicenceManagement())
				mess.append(Configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			else {
				mess.append(showLicenceContractWarnings());
				mess.append(calculateVariantLicencepools());
			}

			message = mess.toString();
			showInfo();
		});
	}

	public void showInfo() {

		if (fDash == null) {
			panelDash = new PanelDashControl() {

				@Override
				protected void showDashOnStartupWasSetTo(boolean b) {
					super.showDashOnStartupWasSetTo(b);
					persist.setGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART, b,
							"(editable on dash window)");
				}

				@Override
				protected void showDashOnLicencesActivationWasSetTo(boolean b) {
					super.showDashOnLicencesActivationWasSetTo(b);
					persist.setGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT, b,
							"(editable on dash window)");
				}
			};

			panelDash.setShowDashOnStartup(
					persist.getGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART,
							PersistenceController.DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART));

			panelDash.setShowDashOnLicencesActivation(
					persist.getGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT,
							PersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT));

			String[] options = new String[] { Configed.getResourceValue("Dash.close"),
					Configed.getResourceValue("Dash.reload") };

			Icon[] icons = new Icon[] { Globals.createImageIcon("images/reload16.png", "reload"),
					Globals.createImageIcon("images/cancel16.png", "cancel") };

			fDash = new FTextArea(null, Globals.APPNAME + " Dash", false, options, icons, 600, 500, panelDash) {
				@Override
				protected boolean wantToBeRegisteredWithRunningInstances() {
					return true;
				}

				@Override
				public void doAction1() {
					Logging.debug(this, "doAction1");
					loadData();
					Logging.info(this, "update data ");
					panelDash.setShowDashOnLicencesActivation(persist.getGlobalBooleanConfigValue(
							PersistenceController.KEY_SHOW_DASH_FOR_LICENCEMANAGEMENT,
							PersistenceController.DEFAULTVALUE_SHOW_DASH_FOR_LICENCEMANAGEMENT));
					panelDash.setShowDashOnStartup(
							persist.getGlobalBooleanConfigValue(PersistenceController.KEY_SHOW_DASH_ON_PROGRAMSTART,
									PersistenceController.DEFAULTVALUE_SHOW_DASH_ON_PROGRAMSTART));
				}

				@Override
				public void doAction2() {
					Logging.debug(this, "doAction2");
					super.doAction2();
				}

				@Override
				public void setVisible(boolean b) {
					super.setVisible(b);
					jButton1.requestFocus();

				}

				@Override
				public void leave() {
					Logging.debug(this, "leave");
					setVisible(false);
				}

			};

			fDash.checkAdditionalPane();

			if (ConfigedMain.getMainFrame() != null) {
				fDash.setLocation(ConfigedMain.getMainFrame().getX() + Globals.LOCATION_DISTANCE_X,
						ConfigedMain.getMainFrame().getY() + Globals.LOCATION_DISTANCE_Y);
			}

		}

		fDash.setMessage(message);
		fDash.setVisible(true);
	}

	protected String showLicenceContractWarnings() {

		StringBuilder result = new StringBuilder();

		// TODO why do the same thing twice?
		NavigableMap<String, NavigableSet<String>> contractsExpired = persist.getLicenceContractsExpired();

		NavigableMap<String, NavigableSet<String>> contractsToNotify = persist.getLicenceContractsExpired();

		Logging.info(this, "contractsExpired " + contractsExpired);

		Logging.info(this, "contractsToNotify " + contractsToNotify);

		result.append("  ");
		result.append(Configed.getResourceValue("Dash.expiredContracts"));
		result.append(":  \n");

		for (Entry<String, NavigableSet<String>> entry : contractsExpired.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getKey() + ": " + ID);
				result.append("\n");
			}
		}
		result.append("\n");

		result.append("  ");
		result.append(Configed.getResourceValue("Dash.contractsToNotify"));
		result.append(":  \n");

		for (Entry<String, NavigableSet<String>> entry : contractsToNotify.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getKey() + ": " + ID);
				result.append("\n");
			}
		}

		return result.toString();

	}

	protected String calculateVariantLicencepools() {
		StringBuilder result = new StringBuilder();

		GenTableModel modelSWnames;

		List<String> columnNames;
		List<String> classNames;

		TableUpdateCollection updateCollection;

		columnNames = new ArrayList<>();
		for (String key : de.uib.configed.type.SWAuditEntry.ID_VARIANTS_COLS)
			columnNames.add(key);

		classNames = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new TableUpdateCollection();

		final TreeSet<String> namesWithVariantPools = new TreeSet<>();

		modelSWnames = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
						() -> (Map) persist.getInstalledSoftwareName2SWinfo())),
				0, new int[] {}, (TableModelListener) null, updateCollection) {

			@Override
			public void produceRows() {
				super.produceRows();

				Logging.info(this, "producing rows for modelSWnames");
				int foundVariantLicencepools = 0;
				namesWithVariantPools.clear();

				int i = 0;
				while (i < getRowCount()) {
					String swName = (String) getValueAt(i, 0);

					if (checkExistNamesWithVariantLicencepools(swName)) {
						namesWithVariantPools.add(swName);
						foundVariantLicencepools++;
					}

					i++;
				}

				Logging.info(this, "produced rows, foundVariantLicencepools " + foundVariantLicencepools);
			}

			@Override
			public void reset() {
				Logging.info(this, "reset");
				super.reset();
			}
		};
		modelSWnames.produceRows();

		List<List<Object>> specialrows = modelSWnames.getRows();
		if (specialrows != null) {
			Logging.info(this, "initDashInfo, modelSWnames.getRows() size " + specialrows.size());
		}

		result.append("\n");
		result.append("  ");
		result.append(Configed.getResourceValue("Dash.similarSWEntriesForLicencePoolExist"));
		result.append(":  \n");

		for (String name : namesWithVariantPools) {
			result.append(name);
			result.append("\n");
		}

		result.append("\n");
		result.append("\n");

		return result.toString();

	}

	private java.util.Set<String> getRangeSWxLicencepool(String swName)
	// nearly done in produceModelSWxLicencepool, but we collect the range of the
	// model-map
	{
		Set<String> range = new HashSet<>();

		for (String swID : persist.getName2SWIdents().get(swName)) {
			String licpool = persist.getFSoftware2LicencePool(swID);

			if (licpool == null)
				range.add(FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL);
			else
				range.add(licpool);
		}

		return range;
	}

	private boolean checkExistNamesWithVariantLicencepools(String name) {
		java.util.Set<String> range = getRangeSWxLicencepool(name);

		if (range.size() > 1) {
			Logging.info(this, "checkExistNamesWithVariantLicencepools, found  for " + name + " :  " + range);
			return true;
		}
		return false;
	}
}
