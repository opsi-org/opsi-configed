/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.dashboard;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.UIManager;
import javax.swing.event.TableModelListener;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Softwarename2LicensePoolDialog;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
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

public final class LicenseDisplayer {
	private static LicenseDisplayer instance;

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

	private LicenseDisplayer() {
		try {
			this.initAndShowGUI();
		} catch (IOException ioE) {
			Logging.warning(ioE, "Unable to open FXML file.");
		}
	}

	public static void showLicenseDisplayer() {
		if (instance == null) {
			instance = new LicenseDisplayer();
		} else {
			instance.display();
		}
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

	private void initAndShowGUI() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(LicenseDisplayer.class.getResource("/fxml/dialogs/license_dialog.fxml"));
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		stage = new Stage();

		stage.getIcons().add(SwingFXUtils.toFXImage(Helper.toBufferedImage(Icons.getMainIcon()), null));
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
		Map<String, Set<String>> contractsExpired = persistenceController.getDataServices().license
				.getLicenseContractsToNotifyPD();
		Map<String, Set<String>> contractsToNotify = persistenceController.getDataServices().license
				.getLicenseContractsToNotifyPD();

		Logging.info(this, "contractsExpired ", contractsExpired);
		Logging.info(this, "contractsToNotify ", contractsToNotify);

		result.append("  ");
		result.append(Configed.getResourceValue("Dashboard.expiredContracts"));
		result.append(":  \n");

		for (Entry<String, Set<String>> entry : contractsExpired.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
				result.append("\n");
			}
		}
		result.append("\n");

		result.append("  ");
		result.append(Configed.getResourceValue("Dashboard.contractsToNotify"));
		result.append(":  \n");

		for (Entry<String, Set<String>> entry : contractsToNotify.entrySet()) {
			for (String ID : entry.getValue()) {
				result.append(entry.getValue() + ": " + ID);
				result.append("\n");
			}
		}

		return result.toString();
	}

	private String calculateVariantLicensepools() {

		final Set<String> namesWithVariantPools = new TreeSet<>();

		new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(new ArrayList<>(SWAuditEntry.ID_VARIANTS_COLS),
						ReloadEvent.INSTALLED_SOFTWARE_RELOAD,
						persistenceController.getDataServices().software::getInstalledSoftwareName2SWinfoPD)),
				0, new int[] {}, (TableModelListener) null, new ArrayList<>()) {
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

				Logging.info(this, "produced rows, foundVariantLicensepools ", foundVariantLicensepools);
			}
		}.produceRows();

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
		for (String swID : persistenceController.getDataServices().software.getName2SWIdentsPD().get(swName)) {
			String licpool = persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(swID);

			if (licpool == null) {
				range.add(Softwarename2LicensePoolDialog.VALUE_NO_LICENSE_POOL);
			} else {
				range.add(licpool);
			}
		}
		return range;
	}

	private boolean checkExistNamesWithVariantLicensepools(String name) {
		Set<String> range = getRangeSWxLicensepool(name);
		if (range.size() > 1) {
			Logging.info(this, "checkExistNamesWithVariantLicensepools, found  for ", name, " :  ", range);
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
