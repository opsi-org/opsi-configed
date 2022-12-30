/*
* LicenseDisplayer.java
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

package de.uib.configed.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.TableModelListener;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.TableUpdateCollection;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class LicenseDisplayer {
	@FXML
	private TextFlow textflow;
	@FXML
	private Button closeButton;

	private String message = "";
	private PersistenceController persist;
	private LicenseDisplayer controller;

	private Stage stage;

	public LicenseDisplayer() {
		persist = PersistenceControllerFactory.getPersistenceController();
	}

	public void loadData() {
		Platform.runLater(() -> {
			message = "";
			showInfo();

			StringBuilder mess = new StringBuilder();

			mess.append(showLicenceContractWarnings());
			mess.append(calculateVariantLicencepools());

			message = mess.toString();
			showInfo();
		});
	}

	private void showInfo() {
		final Text msg = new Text(message);
		final ObservableList<Node> list = controller.textflow.getChildren();
		list.clear();
		list.add(msg);
	}

	public void initAndShowGUI() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(LicenseDisplayer.class.getResource("/fxml/dialogs/license_dialog.fxml"));
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		stage = new Stage();

		stage.getIcons().add(SwingFXUtils.toFXImage(Helper.toBufferedImage(Globals.mainIcon), null));
		stage.setTitle(configed.getResourceValue("Dashboard.license.title"));
		stage.setScene(scene);
		stage.show();

		controller = fxmlLoader.getController();

		loadData();
	}

	public void display() {
		stage.show();
		loadData();
	}

	protected String showLicenceContractWarnings() {
		StringBuilder result = new StringBuilder();
		NavigableMap<String, NavigableSet<String>> contractsExpired = persist.getLicenceContractsExpired();
		NavigableMap<String, NavigableSet<String>> contractsToNotify = persist.getLicenceContractsToNotify();

		logging.info(this, "contractsExpired " + contractsExpired);
		logging.info(this, "contractsToNotify " + contractsToNotify);

		result.append("  ");
		result.append(configed.getResourceValue("Dash.expiredContracts"));
		result.append(":  \n");

		for (Map.Entry<String, NavigableSet<String>> entry : contractsExpired.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
				result.append("\n");
			}
		}
		result.append("\n");

		result.append("  ");
		result.append(configed.getResourceValue("Dash.contractsToNotify"));
		result.append(":  \n");

		for (Map.Entry<String, NavigableSet<String>> entry : contractsToNotify.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
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

		modelSWnames = new GenTableModel(null, // no updates
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
						() -> (Map) persist.getInstalledSoftwareName2SWinfo())),
				0, new int[] {}, (TableModelListener) null, // panelSWnames ,
				updateCollection) {

			@Override
			public void produceRows() {
				super.produceRows();

				logging.info(this, "producing rows for modelSWnames");
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

				logging.info(this, "produced rows, foundVariantLicencepools " + foundVariantLicencepools);
			}

			@Override
			public void reset() {
				logging.info(this, "reset");
				super.reset();
			}
		};

		modelSWnames.produceRows();

		List<List<Object>> specialrows = modelSWnames.getRows();
		if (specialrows != null) {
			logging.info(this, "initDashInfo, modelSWnames.getRows() size " + specialrows.size());
		}

		result.append("\n");
		result.append("  ");
		result.append(configed.getResourceValue("Dash.similarSWEntriesForLicencePoolExist"));
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

		if (range.size() > 1)
		// && range.contains( FSoftwarename2LicencePool.valNoLicencepool ))
		{
			logging.info(this, "checkExistNamesWithVariantLicencepools, found  for " + name + " :  " + range);
			return true;
		}
		return false;
	}

	@FXML
	public void close() {
		Stage currentStage = (Stage) closeButton.getScene().getWindow();
		currentStage.close();
	}
}
