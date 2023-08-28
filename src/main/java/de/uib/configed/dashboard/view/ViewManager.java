/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.dashboard.view;

import java.util.HashMap;
import java.util.Map;

public final class ViewManager {
	private static Map<String, View> views = new HashMap<>();

	private ViewManager() {
	}

	public static void addView(String name, View view) {
		views.put(name, view);
	}

	public static void displayView(String name) {
		views.get(name).display();
	}
}
