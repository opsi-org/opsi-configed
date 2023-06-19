/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.List;

import de.uib.Main;
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
		// final
		FShowList fListFeedback = new FShowList(ConfigedMain.getMainFrame(), title, false, new String[] { "ok" }, 800,
				200);
		if (!Main.FONT) {
			fListFeedback.setFont(Globals.defaultFont);
		}
		fListFeedback.setMessage("");
		fListFeedback.setButtonsEnabled(true);
		fListFeedback.setCursor(Globals.WAIT_CURSOR);
		fListFeedback.setVisible(true);
		fListFeedback.glassTransparency(true, 800, 200, 0.04F);

		List<String> errors = getErrors();

		if (!errors.isEmpty()) {
			fListFeedback.setLines(errors);
			fListFeedback.setCursor(null);
			fListFeedback.setButtonsEnabled(true);

			fListFeedback.setVisible(true);
		} else {
			fListFeedback.leave();
		}
	}
}
