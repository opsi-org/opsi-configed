package de.uib.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import de.uib.utilities.logging.logging;

public class PropertiesStore
// is a decorator of a HashMap
{
	File myStore;

	protected static final String keySeparator = "=";

	private HashMap<String, String> internalStore;

	public PropertiesStore() {
		this(null);
	}

	public PropertiesStore(File store) {
		internalStore = new HashMap<>();
		myStore = store;
	}

	public void setStore(File store) {
		myStore = store;
	}

	@Override
	public String toString() {
		return internalStore.toString();
	}

	public void load() throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(myStore))) {

			String line = reader.readLine();

			while (line != null) {
				logging.debug(this, "line: " + line);
				String trimmed = line.trim();

				if (trimmed.length() == 0 || trimmed.charAt(0) == '#' || trimmed.charAt(0) == ';') {
					// continue
				} else {
					int posSeparator = line.indexOf(keySeparator);

					String key = line.substring(0, posSeparator);
					String value = null;
					if (line.length() > posSeparator)
						value = line.substring(posSeparator + 1);
					if (value != null)
						setProperty(key, value);
					logging.debug(this, "key, value " + key + ", " + value);
				}

				line = reader.readLine();
			}
		}
	}

	public String getProperty(String key) {
		return getProperty(key, null);
	}

	public String getProperty(String key, String defaultValue) {
		return getProp(key, defaultValue);
	}

	public void removeProperty(String key) {
		internalStore.remove(key);
	}

	// used by subsubclasses
	protected final String getProp(String key, String defaultValue) {
		String result = internalStore.get(key);
		if (result == null)
			return defaultValue;

		return result;
	}

	// can be overridden by subclasses in a way to restrict key usage
	public void setProperty(String key, String value) {
		setProp(key, value);
	}

	// used by subsubclasses
	protected final void setProp(String key, String value) {
		internalStore.put(key, value);
	}

	private TreeSet<String> formOutputLines() {
		TreeSet<String> orderedLines = new TreeSet<>();

		for (String key : internalStore.keySet()) {
			if (getProperty(key) != null)
				orderedLines.add(key + keySeparator + getProperty(key));
		}

		return orderedLines;
	}

	public void store(String comments) throws IOException {
		List<String> outLines = new ArrayList<>();

		if (comments != null)
			outLines.add("# " + comments);
		outLines.add("# " + java.text.DateFormat
				.getDateTimeInstance(java.text.DateFormat.LONG, java.text.DateFormat.LONG).format(new Date()));
		outLines.addAll(formOutputLines());

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(myStore))) {
			for (String line : outLines) {
				writer.write(line);
				writer.newLine();
			}
		}
	}
}