/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.UIManager;
import javax.swing.event.TableModelListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.TableEditItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import utils.Utils;

public class LicenseDisplayer {
	@FXML
	private TextFlow textflow;
	@FXML
	private Button closeButton;
	@FXML
	private AnchorPane mainAnchorPane;
	@FXML
	private BorderPane mainBorderPane;
	@FXML
	private ScrollPane scrollPane;

	private String message = "";
	private OpsiServiceNOMPersistenceController persist = PersistenceControllerFactory.getPersistenceController();
	private LicenseDisplayer controller;

	private Stage stage;

	private ConfigedMain configedMain;

	public void setConfigedMain(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void loadData() {
		StringBuilder mess = new StringBuilder();
		mess.append(showLicenceContractWarnings());
		mess.append(calculateVariantLicencepools());
		message = mess.toString();
		showInfo();
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

		stage.getIcons().add(SwingFXUtils.toFXImage(Helper.toBufferedImage(Utils.getMainIcon()), null));
		stage.setTitle(Configed.getResourceValue("Dashboard.license.title"));
		stage.setScene(scene);

		// Hide stage before showing so that we know the size before it gets visible
		stage.setOnShowing(event -> stage.hide());
		stage.setOnShown(event -> centerAndShowStage());

		controller = fxmlLoader.getController();
		loadData();

		Platform.runLater(() -> {
			styleAccordingToSelectedTheme();
			stage.showAndWait();
		});
	}

	private void styleAccordingToSelectedTheme() {
		String panelBackgroundColor = ComponentStyler.getHexColor(UIManager.getColor("Panel.background"));
		controller.mainAnchorPane.setStyle("-fx-background-color: " + panelBackgroundColor);
		controller.mainBorderPane.setStyle("-fx-background-color: " + panelBackgroundColor);

		ComponentStyler.styleTextFlowComponent(controller.textflow);
		ComponentStyler.styleButtonComponent(controller.closeButton);

		Set<Node> scrollBars = controller.scrollPane.lookupAll(".scroll-bar");

		if (scrollBars.isEmpty()) {
			Logging.warning("no scrollbars were found");
		}

		for (Node scrollBar : scrollBars) {
			ComponentStyler.styleScrollBarComponent((ScrollBar) scrollBar);
		}
	}

	public void display() {
		stage.show();
		loadData();
	}

	private void centerAndShowStage() {
		Rectangle mainRectangle = ConfigedMain.getMainFrame().getBounds();
		stage.setX(mainRectangle.getX() + mainRectangle.getWidth() / 2 - stage.getWidth() / 2);
		stage.setY(mainRectangle.getY() + mainRectangle.getHeight() / 2 - stage.getHeight() / 2);
		stage.show();
	}

	private String showLicenceContractWarnings() {
		StringBuilder result = new StringBuilder();
		NavigableMap<String, NavigableSet<String>> contractsExpired = persist.getLicenseDataService()
				.getLicenseContractsToNotifyPD();
		NavigableMap<String, NavigableSet<String>> contractsToNotify = persist.getLicenseDataService()
				.getLicenseContractsToNotifyPD();

		Logging.info(this, "contractsExpired " + contractsExpired);
		Logging.info(this, "contractsToNotify " + contractsToNotify);

		result.append("  ");
		result.append(Configed.getResourceValue("Dashboard.expiredContracts"));
		result.append(":  \n");

		for (Entry<String, NavigableSet<String>> entry : contractsExpired.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
				result.append("\n");
			}
		}
		result.append("\n");

		result.append("  ");
		result.append(Configed.getResourceValue("Dashboard.contractsToNotify"));
		result.append(":  \n");

		for (Entry<String, NavigableSet<String>> entry : contractsToNotify.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
				result.append("\n");
			}
		}

		return result.toString();
	}

	private String calculateVariantLicencepools() {
		GenTableModel modelSWnames;

		List<String> columnNames;
		List<String> classNames;

		List<TableEditItem> updateCollection;

		columnNames = new ArrayList<>();
		for (String key : SWAuditEntry.ID_VARIANTS_COLS) {
			columnNames.add(key);
		}

		classNames = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new ArrayList<>();

		final TreeSet<String> namesWithVariantPools = new TreeSet<>();

		modelSWnames = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (configedMain != null && !configedMain.isAllLicenseDataReloaded()) {
							persist.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persist.getSoftwareDataService().getInstalledSoftwareName2SWinfoPD();
					}
				})), 0, new int[] {}, (TableModelListener) null, updateCollection) {
			@Override
			public void produceRows() {
				super.produceRows();

				Logging.info(this, "producing rows for modelSWnames");
				int foundVariantLicencepools = 0;
				namesWithVariantPools.clear();

				for (int i = 0; i < getRowCount(); i++) {
					String swName = (String) getValueAt(i, 0);
					if (checkExistNamesWithVariantLicencepools(swName)) {
						namesWithVariantPools.add(swName);
						foundVariantLicencepools++;
					}
				}

				Logging.info(this, "produced rows, foundVariantLicencepools " + foundVariantLicencepools);
			}
		};

		modelSWnames.produceRows();

		List<List<Object>> specialrows = modelSWnames.getRows();
		if (specialrows != null) {
			Logging.info(this, "initDashInfo, modelSWnames.getRows() size " + specialrows.size());
		}

		StringBuilder result = new StringBuilder();
		result.append("\n");
		result.append("  ");
		result.append(Configed.getResourceValue("Dashboard.similarSWEntriesForLicencePoolExist"));
		result.append(":  \n");
		for (String name : namesWithVariantPools) {
			result.append(name);
			result.append("\n");
		}
		result.append("\n");
		result.append("\n");
		return result.toString();
	}

	private Set<String> getRangeSWxLicencepool(String swName) {
		// nearly done in produceModelSWxLicencepool, but we collect the range of the
		// model-map
		Set<String> range = new HashSet<>();
		for (String swID : persist.getSoftwareDataService().getName2SWIdentsPD().get(swName)) {
			String licpool = persist.getSoftwareDataService().getFSoftware2LicencePoolPD(swID);

			if (licpool == null) {
				range.add(FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL);
			} else {
				range.add(licpool);
			}
		}
		return range;
	}

	private boolean checkExistNamesWithVariantLicencepools(String name) {
		Set<String> range = getRangeSWxLicencepool(name);
		if (range.size() > 1) {
			Logging.info(this, "checkExistNamesWithVariantLicencepools, found  for " + name + " :  " + range);
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
