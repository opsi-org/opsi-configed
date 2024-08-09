/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard;

import java.io.IOException;

import javax.swing.JPanel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.dashboard.collector.ClientData;
import de.uib.configed.dashboard.collector.DepotData;
import de.uib.configed.dashboard.collector.LicenseData;
import de.uib.configed.dashboard.collector.ModuleData;
import de.uib.configed.dashboard.collector.ProductData;
import de.uib.configed.dashboard.view.ClientView;
import de.uib.configed.dashboard.view.MainView;
import de.uib.configed.dashboard.view.ProductView;
import de.uib.configed.dashboard.view.ViewManager;
import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.WindowEvent;

public class Dashboard extends JPanel {
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
		final JFXPanel fxPanel = new JFXPanel();
		add(fxPanel);

		Platform.setImplicitExit(false);
		Platform.runLater(() -> {
			try {
				initFX(fxPanel);
			} catch (IOException ioE) {
				Logging.error(this, ioE, "Unable to open fxml file");
			}
		});
	}

	private void windowClosing(WindowEvent e) {
		Platform.runLater(() -> ViewManager.displayView(MAIN_VIEW));

		ClientData.clear();
		ProductData.clear();
		ModuleData.clear();
		LicenseData.clear();
		DepotData.clear();
	}

	private void initFX(final JFXPanel fxPanel) throws IOException {
		mainView = new MainView(fxPanel, configedMain);
		ClientView clientView = new ClientView(fxPanel);
		ProductView productView = new ProductView(fxPanel);

		ViewManager.addView(MAIN_VIEW, mainView);
		ViewManager.addView(CLIENT_VIEW, clientView);
		ViewManager.addView(PRODUCT_VIEW, productView);
		ViewManager.displayView(MAIN_VIEW);

		mainView.init();
	}
}
