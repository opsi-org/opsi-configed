/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
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
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class Dashboard {
	public static final String MAIN_VIEW = "main";
	public static final String CLIENT_VIEW = "client";
	public static final String PRODUCT_VIEW = "product";

	private static final int WINDOW_HEIGHT = 1200;
	private static final int WINDOW_WIDTH = 860;

	private final JFrame frame;
	private MainView mainView;
	private ConfigedMain configedMain;

	public Dashboard(ConfigedMain configedMain) {
		frame = new JFrame();
		mainView = null;
		this.configedMain = configedMain;
	}

	public void initAndShowGUI() {
		final JFXPanel fxPanel = new JFXPanel();
		frame.add(fxPanel);
		frame.setIconImage(Utils.getMainIcon());
		frame.setTitle(Configed.getResourceValue("Dashboard.title"));
		frame.setMinimumSize(new Dimension(WINDOW_HEIGHT, WINDOW_WIDTH));
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				Platform.runLater(() -> ViewManager.displayView(MAIN_VIEW));

				ClientData.clear();
				ProductData.clear();
				ModuleData.clear();
				LicenseData.clear();
				DepotData.clear();
			}
		});

		Platform.setImplicitExit(false);
		Platform.runLater(() -> {
			try {
				initFX(fxPanel);
			} catch (IOException ioE) {
				Logging.error(this, ioE, "Unable to open fxml file");
			}
		});
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

	public void show() {
		if (!frame.isVisible()) {
			mainView.init();
		}
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);
	}
}
