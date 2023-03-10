package de.uib.opsidatamodel.productstate;

import java.util.HashMap;
import java.util.Map;

public final class ActionSequence {

	public static final String KEY = "actionSequence";

	private static Map<String, String> displayLabel2label;

	// private constructor to hide the implicit public one
	private ActionSequence() {
	}

	private static void checkCollections() {
		if (displayLabel2label == null) {
			displayLabel2label = new HashMap<>();
		}

		for (int i = -100; i <= 100; i++) {
			String st = "" + i;
			displayLabel2label.put(st, st);
		}

		displayLabel2label.put("" + 0, "");
	}

	public static Map<String, String> getLabel2DisplayLabel() {
		checkCollections();
		return displayLabel2label;
	}
}
