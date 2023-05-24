
package de.uib.opsidatamodel.modulelicense;

import java.util.ArrayList;
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FTextArea;

public class FOpsiLicenseMissingText extends FTextArea {

	private static FOpsiLicenseMissingText instance;

	private static List<String> messages = new ArrayList<>();

	public FOpsiLicenseMissingText() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("Permission.modules.title"), false,
				new String[] { "ok" }, 450, 250);
	}

	private static FOpsiLicenseMissingText getInstance() {
		if (instance == null) {
			instance = new FOpsiLicenseMissingText();
		}
		return instance;
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	public static void callInstanceWith(String message) {
		if (messages.isEmpty()) {
			messages.add(Configed.getResourceValue("Permission.modules.infoheader"));
		}

		if (messages.indexOf(message) == -1) {
			messages.add(message);
		}

		StringBuilder combined = new StringBuilder("");

		for (String s : messages) {
			combined.append("_____________________________\n");

			combined.append("\n");
			combined.append(s);
		}

		getInstance().setMessage(combined.toString());
		getInstance().setVisible(true);
		getInstance().setLocationRelativeTo(ConfigedMain.getMainFrame());
		getInstance().setAlwaysOnTop(true);
	}

	public static void reset() {
		messages.clear();
	}

}
