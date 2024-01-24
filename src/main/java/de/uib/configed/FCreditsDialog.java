/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.productpage.TextMarkdownPane;
import de.uib.utilities.logging.Logging;

public final class FCreditsDialog extends FGeneralDialog {
	private FCreditsDialog(JFrame owner) {
		super(owner, Configed.getResourceValue("FCreditsDialog.title") + " " + Globals.APPNAME, true,
				new String[] { Configed.getResourceValue("buttonClose") }, 500, 300);
		allpane.setPreferredSize(new Dimension(Globals.DEFAULT_FTEXTAREA_WIDTH, Globals.DEFAULT_FTEXTAREA_HEIGHT));
		TextMarkdownPane jTextPane = new TextMarkdownPane();
		jTextPane.setAlignmentX(SwingConstants.CENTER);
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("FCreditsDialog.message1"));
		message.append("<br>");
		message.append(Configed.getResourceValue("FCreditsDialog.message2"));
		message.append("<br>");
		appendCreditsFromFile(message);
		jTextPane.setText(message.toString());
		scrollpane.getViewport().add(jTextPane, null);
	}

	private void appendCreditsFromFile(StringBuilder message) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("credits.md")))) {
			String line;
			while ((line = br.readLine()) != null) {
				message.append(line + "<br>");
			}
		} catch (IOException e) {
			Logging.warning(this, "unable to read credits file", e);
		}
	}

	public static void display(JFrame owner) {
		FCreditsDialog fCredits = new FCreditsDialog(owner);
		fCredits.setVisible(true);
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return false;
	}

}
