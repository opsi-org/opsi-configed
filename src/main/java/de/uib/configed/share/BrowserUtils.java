/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import de.uib.configed.gui.Globals;
import de.uib.configed.gui.messages.Messages;
import javafx.application.Application;
import javafx.stage.Stage;

public class BrowserUtils extends Application {
	private static BrowserUtils instance;

	private static String firstURL;

	@Override
	public void start(Stage stage) {
		setInstance(this);

		getHostServices().showDocument(firstURL);
	}

	private static void setInstance(BrowserUtils instance) {
		BrowserUtils.instance = instance;
	}

	public static void openLink(String url) {
		if (instance == null) {
			firstURL = url;
			new Thread(() -> Application.launch(BrowserUtils.class)).start();
		} else {
			instance.getHostServices().showDocument(url);
		}
	}

	public static void openDocumentation() {
		String link = "de".equals(Messages.getLocale().getLanguage()) ? Globals.OPSI_DOC_PAGE_DE
				: Globals.OPSI_DOC_PAGE_EN;

		openLink(link);
	}
}
