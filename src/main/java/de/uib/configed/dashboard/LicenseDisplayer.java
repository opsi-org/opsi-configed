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
import de.uib.configed.gui.FSoftwarename2LicensePool;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
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

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private LicenseDisplayer controller;

	private Stage stage;

	private ConfigedMain configedMain;

	public void setConfigedMain(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void loadData() {
		StringBuilder message = new StringBuilder();
		message.append(showLicenseContractWarnings());
		message.append(calculateVariantLicensepools());
		Platform.runLater(() -> showInfo(message.toString()));
	}

	private void showInfo(String info) {
		final ObservableList<Node> list = controller.textflow.getChildren();
		list.clear();
		Text text = new Text(info);
		ComponentStyler.styleTextComponent(text);
		list.add(text);
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

		Platform.setImplicitExit(false);
		Platform.runLater(() -> {
			styleAccordingToSelectedTheme();
			stage.show();
		});
	}

	private void styleAccordingToSelectedTheme() {
		String panelBackgroundColor = ComponentStyler.getHexColor(UIManager.getColor("Panel.background"));
		controller.mainAnchorPane.setStyle("-fx-background-color: " + panelBackgroundColor);
		controller.mainBorderPane.setStyle("-fx-background-color: " + panelBackgroundColor);

		ComponentStyler.styleTextFlowComponent(controller.textflow);
		ComponentStyler.styleButtonComponent(controller.closeButton);

		Set<Node> scrollBars = controller.scrollPane.lookupAll(".scroll-bar");
		for (Node scrollBar : scrollBars) {
			ComponentStyler.styleScrollBarComponent((ScrollBar) scrollBar);
		}
	}

	public void display() {
		loadData();
		stage.show();
	}

	private void centerAndShowStage() {
		Rectangle mainRectangle = ConfigedMain.getMainFrame().getBounds();
		stage.setX(mainRectangle.getX() + mainRectangle.getWidth() / 2 - stage.getWidth() / 2);
		stage.setY(mainRectangle.getY() + mainRectangle.getHeight() / 2 - stage.getHeight() / 2);
		stage.show();
	}

	private String showLicenseContractWarnings() {
		StringBuilder result = new StringBuilder();
		NavigableMap<String, NavigableSet<String>> contractsExpired = persistenceController.getLicenseDataService()
				.getLicenseContractsToNotifyPD();
		NavigableMap<String, NavigableSet<String>> contractsToNotify = persistenceController.getLicenseDataService()
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

	private String calculateVariantLicensepools() {
		GenTableModel modelSWnames;

		List<String> columnNames;

		List<MapBasedTableEditItem> updateCollection;

		columnNames = new ArrayList<>();
		for (String key : SWAuditEntry.ID_VARIANTS_COLS) {
			columnNames.add(key);
		}

		updateCollection = new ArrayList<>();

		final TreeSet<String> namesWithVariantPools = new TreeSet<>();

		modelSWnames = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (configedMain != null && !configedMain.isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getSoftwareDataService().getInstalledSoftwareName2SWinfoPD();
					}
				})), 0, new int[] {}, (TableModelListener) null, updateCollection) {
			@Override
			public void produceRows() {
				super.produceRows();

				Logging.info(this, "producing rows for modelSWnames");
				int foundVariantLicensepools = 0;
				namesWithVariantPools.clear();

				for (int i = 0; i < getRowCount(); i++) {
					String swName = (String) getValueAt(i, 0);
					if (checkExistNamesWithVariantLicensepools(swName)) {
						namesWithVariantPools.add(swName);
						foundVariantLicensepools++;
					}
				}

				Logging.info(this, "produced rows, foundVariantLicensepools " + foundVariantLicensepools);
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
		result.append(Configed.getResourceValue("Dashboard.similarSWEntriesForLicensePoolExist"));
		result.append(":  \n");
		for (String name : namesWithVariantPools) {
			result.append(name);
			result.append("\n");
		}
		result.append("\n");
		result.append("\n");
		return result.toString();
	}

	private Set<String> getRangeSWxLicensepool(String swName) {
		// nearly done in produceModelSWxLicensepool, but we collect the range of the
		// model-map
		Set<String> range = new HashSet<>();
		for (String swID : persistenceController.getSoftwareDataService().getName2SWIdentsPD().get(swName)) {
			String licpool = persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(swID);

			if (licpool == null) {
				range.add(FSoftwarename2LicensePool.VALUE_NO_LICENSE_POOL);
			} else {
				range.add(licpool);
			}
		}
		return range;
	}

	private boolean checkExistNamesWithVariantLicensepools(String name) {
		Set<String> range = getRangeSWxLicensepool(name);
		if (range.size() > 1) {
			Logging.info(this, "checkExistNamesWithVariantLicensepools, found  for " + name + " :  " + range);
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
