/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;

import de.uib.configed.gui.FShowList;

public abstract class AbstractErrorListProducer extends Thread {
	String title;

	AbstractErrorListProducer(String specificPartOfTitle) {
		String part = specificPartOfTitle;
		title = Globals.APPNAME + ":  " + part;
	}

	protected abstract List<String> getErrors();

	@Override
	public void run() {
		List<String> errors = getErrors();

		if (!errors.isEmpty()) {
			// final
			FShowList fListFeedback = new FShowList(ConfigedMain.getMainFrame(), title, false,
					new String[] { Configed.getResourceValue("buttonClose") }, 800, 200);

			fListFeedback.setMessage("");
			fListFeedback.setButtonsEnabled(true);
			fListFeedback.setCursor(Globals.WAIT_CURSOR);
			fListFeedback.setVisible(true);
			fListFeedback.setLines(errors);
			fListFeedback.setCursor(null);
			fListFeedback.setButtonsEnabled(true);

			fListFeedback.setVisible(true);
		}
	}
}
