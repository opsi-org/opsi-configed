/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.dashboard;

import java.io.IOException;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.features.dashboard.collector.ClientData;
import de.uib.configed.gui.features.dashboard.collector.DepotData;
import de.uib.configed.gui.features.dashboard.collector.LicenseData;
import de.uib.configed.gui.features.dashboard.collector.ModuleData;
import de.uib.configed.gui.features.dashboard.collector.ProductData;
import de.uib.configed.gui.features.dashboard.view.ClientView;
import de.uib.configed.gui.features.dashboard.view.MainView;
import de.uib.configed.gui.features.dashboard.view.ProductView;
import de.uib.configed.gui.features.dashboard.view.ViewManager;
import de.uib.configed.share.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class Dashboard extends JFXPanel {
	public static final String MAIN_VIEW = "main";
	public static final String CLIENT_VIEW = "client";
	public static final String PRODUCT_VIEW = "product";

	private MainView mainView;
	private ConfigedMain configedMain;

	public Dashboard(ConfigedMain configedMain) {
		mainView = null;
		this.configedMain = configedMain;

		init();
	}

	private void init() {
		OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
				.getPersistenceController();
		ClientData.initData(persistenceController);
		ProductData.initData(persistenceController);
		ModuleData.initData(persistenceController);
		LicenseData.initData(persistenceController);
		DepotData.initData(persistenceController);

		Platform.setImplicitExit(false);
		Platform.runLater(() -> {
			try {
				initFX();
			} catch (IOException ioE) {
				Logging.error(this, ioE, "Unable to open fxml file");
			}
		});
	}

	public void clearAllData() {
		ClientData.clear();
		ProductData.clear();
		ModuleData.clear();
		LicenseData.clear();
		DepotData.clear();
	}

	private void initFX() throws IOException {
		mainView = new MainView(this, configedMain);
		ClientView clientView = new ClientView(this);
		ProductView productView = new ProductView(this);

		ViewManager.addView(MAIN_VIEW, mainView);
		ViewManager.addView(CLIENT_VIEW, clientView);
		ViewManager.addView(PRODUCT_VIEW, productView);
		ViewManager.displayView(MAIN_VIEW);

		mainView.init();
	}
}
