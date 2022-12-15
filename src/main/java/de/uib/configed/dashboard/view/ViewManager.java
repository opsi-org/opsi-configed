package de.uib.configed.dashboard.view;

import java.util.HashMap;
import java.util.Map;

public class ViewManager {
	private static Map<String, View> views = new HashMap<>();

	private ViewManager() {
	}

	public static void addView(String name, View view) {
		views.put(name, view);
	}

	public static void removeView(String name) {
		views.remove(name);
	}

	public static void displayView(String name) {
		views.get(name).display();
	}
}
