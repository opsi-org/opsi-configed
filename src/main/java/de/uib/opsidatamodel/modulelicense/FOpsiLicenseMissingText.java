/**
 * FOpsiLicenseMissingText
 * Special window for this kind of problem info
 * Copyright:     Copyright (c) 2022
 * Organisation:  uib
 * @author Rupert Röder
 */

package de.uib.opsidatamodel.modulelicense;

import java.util.ArrayList;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FTextArea;

public class FOpsiLicenseMissingText extends FTextArea {

	private static FOpsiLicenseMissingText instance;

	private static ArrayList<String> messages = new ArrayList<String>();

	public FOpsiLicenseMissingText() {
		super(Globals.mainFrame, configed.getResourceValue("Permission.modules.title"), false, new String[] { "ok" },
				450, 250);
	}

	private static FOpsiLicenseMissingText getInstance() {
		if (instance == null)
			instance = new FOpsiLicenseMissingText();
		return instance;
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public static void callInstanceWith(String message) {
		if (messages.isEmpty()) {
			messages.add(configed.getResourceValue("Permission.modules.infoheader"));
		}

		if (messages.indexOf(message) == -1) {
			messages.add(message);
		}

		StringBuffer combined = new StringBuffer("");

		for (String s : messages) {
			combined.append("_____________________________\n");

			combined.append("\n");
			combined.append(s);
		}

		getInstance().setMessage(combined.toString());
		getInstance().setVisible(true);

		getInstance().centerOn(Globals.mainFrame);
	}

	public static void reset() {
		messages.clear();
	}

	public static void main(String[] options) {
		callInstanceWith("hallo ");
	}
}
