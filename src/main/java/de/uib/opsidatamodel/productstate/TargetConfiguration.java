package de.uib.opsidatamodel.productstate;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import de.uib.configed.Globals;

public class TargetConfiguration {
	public static final String KEY = "targetConfiguration";

	// conflicting entries from several clients
	public static final int CONFLICT = -4;

	// no valid entry from service
	public static final int INVALID = -2;

	// valid service states
	public static final int UNDEFINED = 0;
	public static final int INSTALLED = 1;
	public static final int ALWAYS = 2;
	public static final int FORBIDDEN = 4;

	// future
	public static final int DISABLED = 5; // do not install or uninstall at the moment
	public static final int INSTALL_NEWEST = 6; // look for the newest version and try to install it

	// mappings
	private static Map<Integer, String> state2label;
	private static Map<String, Integer> label2state;
	private static Map<String, String> label2displayLabel;
	private static Map<String, String> displayLabel2label;

	private static Vector<Integer> states;
	private static Vector<String> labels;
	private static String[] choiceLabels;

	// instance variable
	private int state = INVALID;

	private static void checkCollections() {
		if (states != null)
			return;

		states = new Vector<>();
		states.add(CONFLICT);
		states.add(INVALID);
		states.add(UNDEFINED);
		states.add(INSTALLED);
		states.add(ALWAYS);
		states.add(FORBIDDEN);

		labels = new Vector<>();
		labels.add(Globals.CONFLICT_STATE_STRING);
		labels.add(Globals.NO_VALID_STATE_STRING);
		labels.add("undefined");
		labels.add("installed");
		labels.add("always");
		labels.add("forbidden");

		state2label = new HashMap<>();
		state2label.put(CONFLICT, Globals.CONFLICT_STATE_STRING);
		state2label.put(INVALID, Globals.NO_VALID_STATE_STRING);
		state2label.put(UNDEFINED, "undefined");
		state2label.put(INSTALLED, "installed");
		state2label.put(ALWAYS, "always");
		state2label.put(FORBIDDEN, "forbidden");

		label2state = new HashMap<>();
		label2state.put(Globals.CONFLICT_STATE_STRING, CONFLICT);
		label2state.put(Globals.NO_VALID_STATE_STRING, INVALID);
		label2state.put("undefined", UNDEFINED);
		label2state.put("installed", INSTALLED);
		label2state.put("always", ALWAYS);
		label2state.put("forbidden", FORBIDDEN);

		label2displayLabel = new HashMap<>();
		label2displayLabel.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		label2displayLabel.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		label2displayLabel.put("undefined", "undefined");
		label2displayLabel.put("installed", "installed");
		label2displayLabel.put("always", "always");
		label2displayLabel.put("forbidden", "forbidden");

		displayLabel2label = new HashMap<>();
		displayLabel2label.put(Globals.CONFLICT_STATE_STRING, Globals.CONFLICT_STATE_STRING);
		displayLabel2label.put(Globals.NO_VALID_STATE_STRING, Globals.NO_VALID_STATE_STRING);
		displayLabel2label.put("undefined", "undefined");
		displayLabel2label.put("installed", "installed");
		displayLabel2label.put("always", "always");
		displayLabel2label.put("forbidden", "forbidden");

		choiceLabels = new String[] { label2displayLabel.get("undefined"), label2displayLabel.get("installed"),
				label2displayLabel.get("always"), label2displayLabel.get("forbidden") };
	}

	public static Map<String, String> getLabel2DisplayLabel() {
		checkCollections();

		return label2displayLabel;
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
			return UNDEFINED;

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
		// logging.debug("TargetConfiguration.getDisplayLabelsForChoice() " +
		

		return choiceLabels;
	}

	// instance methods

	public int getVal() {
		return state;
	}

	public String getString() {
		return getLabel(state);
	}

	@Override
	public String toString() {
		return getLabel(state);
	}

	// getting instances
	public static TargetConfiguration produceFromDisplayLabel(String display) {
		return produceFromLabel(displayLabel2label.get(display));
	}

	public static TargetConfiguration produceFromLabel(String label) {
		checkCollections();

		if (label == null)
			return new TargetConfiguration(INVALID);

		if (!labels.contains(label))
			return new TargetConfiguration(INVALID);

		
		// logging.debug(" -------- display " + new
		

		return new TargetConfiguration(getVal(label));
	}

	// constructor
	public TargetConfiguration() {
	}

	public TargetConfiguration(int t) {
		if (existsState(t))
			state = t;
		else
			state = INVALID;
	}
}
