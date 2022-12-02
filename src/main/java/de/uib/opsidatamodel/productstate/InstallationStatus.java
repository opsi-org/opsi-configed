package de.uib.opsidatamodel.productstate;

//import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import de.uib.configed.Globals;

public class InstallationStatus {
	public static final String KEY = "installationStatus";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// does not matter
	public static final int UNDEFINED = -1;

	// valid service states since 4.0
	public static final int NOT_INSTALLED = 0;
	public static final int INSTALLED = 1;
	public static final int UNKNOWN = 2;

	// compatibility mode for older opsi data, not more necessary
	// public static final int FAILED = 4;

	// public static final int INSTALLING = 3;

	// textcolors
	public static final Color NOT_INSTALLEDcolor = Globals.INVISIBLE;
	public static final Color INSTALLEDcolor = Globals.okGreen;
	public static final Color UNKNOWNcolor = Globals.unknownBlue;

	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> label2displayLabel;
	private static Map<String, String> displayLabel2label;
	private static Map<String, Color> label2textColor;

	private static Vector<Integer> states;
	private static Vector<String> labels;
	private static String[] choiceLabels;

	// instance variable
	private int state = INVALID;

	private static void checkCollections() {
		if (states != null)
			return;

		states = new Vector<Integer>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(UNDEFINED);
		states.add(INSTALLED);
		states.add(NOT_INSTALLED);
		// states.add(FAILED);
		states.add(UNKNOWN);

		// states.add(INSTALLING);

		labels = new Vector<String>();
		labels.add(Globals.CONFLICTSTATEstring);
		labels.add(Globals.NOVALIDSTATEstring);
		labels.add("undefined");
		labels.add("installed");
		labels.add("not_installed");
		// labels.add("failed");
		labels.add("unknown");
		// labels.add("installing");

		state2label = new HashMap<Integer, String>();
		state2label.put(CONFLICT, Globals.CONFLICTSTATEstring);
		state2label.put(INVALID, Globals.NOVALIDSTATEstring);
		state2label.put(UNDEFINED, "undefined");
		state2label.put(INSTALLED, "installed");
		state2label.put(NOT_INSTALLED, "not_installed");
		// state2label.put(FAILED, "failed");
		state2label.put(UNKNOWN, "unknown");

		// state2label.put(INSTALLING, "installing");

		label2state = new HashMap<String, Integer>();
		label2state.put(Globals.CONFLICTSTATEstring, CONFLICT);
		label2state.put(Globals.NOVALIDSTATEstring, INVALID);
		label2state.put("undefined", UNDEFINED);
		label2state.put("installed", INSTALLED);
		label2state.put("not_installed", NOT_INSTALLED);
		// label2state.put("failed", FAILED);
		label2state.put("unknown", UNKNOWN);
		// label2state.put("installing", INSTALLING);

		label2displayLabel = new HashMap<String, String>();
		label2displayLabel.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		label2displayLabel.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		label2displayLabel.put("undefined", "undefined");
		label2displayLabel.put("installed", "installed");
		label2displayLabel.put("not_installed", "not_installed");
		// label2displayLabel.put("failed", "failed");
		label2displayLabel.put("unknown", "unknown");
		// label2displayLabel.put("installing", "installing");

		displayLabel2label = new HashMap<String, String>();
		displayLabel2label.put(Globals.CONFLICTSTATEstring, Globals.CONFLICTSTATEstring);
		displayLabel2label.put(Globals.NOVALIDSTATEstring, Globals.NOVALIDSTATEstring);
		displayLabel2label.put("undefined", "undefined");
		displayLabel2label.put("installed", "installed");
		displayLabel2label.put("not_installed", "not_installed");
		// displayLabel2label.put("failed", "failed");
		displayLabel2label.put("unknown", "unknown");
		// displayLabel2label.put("installing", "installing");

		choiceLabels = new String[] { label2displayLabel.get("not_installed"), label2displayLabel.get("installed"),
				label2displayLabel.get("unknown")
				// label2displayLabel.get("failed"),
				// label2displayLabel.get(Globals.NOVALIDSTATEstring)
		};

		label2textColor = new HashMap<String, Color>();
		label2textColor.put("not_installed", NOT_INSTALLEDcolor);
		label2textColor.put("installed", INSTALLEDcolor);
		label2textColor.put("unknown", UNKNOWNcolor);

	}

	public static Map<String, String> getLabel2DisplayLabel() {
		checkCollections();

		return label2displayLabel;
	}

	public static Map<String, Color> getLabel2TextColor() {
		checkCollections();

		return label2textColor;
	}

	public static boolean existsState(int state) {
		checkCollections();

		return (states.contains(state));
	}

	public static boolean existsLabel(String label) {
		checkCollections();

		return (labels.contains(label));
	}

	public static String getLabel(int state) {
		checkCollections();

		if (!existsState(state))
			return null;

		return state2label.get(state);
	}

	public static Vector<String> getLabels() {
		checkCollections();

		return labels;
	}

	public static Integer getVal(String label) {
		checkCollections();

		if (label == null || label.equals(""))
			// return NOT_INSTALLED; produces false results when recursively following
			// action requests
			return UNKNOWN;

		if (!existsLabel(label))
			return null;

		return label2state.get(label);
	}

	public static String getDisplayLabel(int state) {
		checkCollections();

		return label2displayLabel.get(getLabel(state));
	}

	public static final String[] getDisplayLabelsForChoice() {
		checkCollections();

		return choiceLabels;
	}

	// instance methods

	public int getVal() {
		return state;
	}

	public String getString() {
		return getLabel(state);
	}

	public String toString() {
		return getLabel(state);
	}

	// getting instances
	public static InstallationStatus produceFromDisplayLabel(String display) {
		return produceFromLabel(displayLabel2label.get(display));
	}

	public static InstallationStatus produceFromLabel(String label) {
		checkCollections();

		if (label == null)
			return new InstallationStatus(INVALID);

		if (!labels.contains(label))
			return new InstallationStatus(INVALID);

		// logging.debug(" -------- label " + label + " --- val " + getVal(label));
		// logging.debug(" -------- display " + new
		// InstallationStatus(getVal(label)));

		return new InstallationStatus(getVal(label));
	}

	// constructor
	public InstallationStatus() {
	}

	public InstallationStatus(int t) {
		if (existsState(t))
			state = t;
		else
			state = INVALID;
	}

	public static void main(String[] args) {
		// logging.debug(" test InstallationStatus.java");
		checkCollections();
		Iterator iter = states.iterator();

		int i = 0;

		while (iter.hasNext()) {
			i++;
			int state = (Integer) iter.next();
			// logging.debug("state " + i + " : " + state + " label " +
			// getLabel(state));
		}
	}

}
