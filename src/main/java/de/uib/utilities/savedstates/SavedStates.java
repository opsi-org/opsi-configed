/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.savedstates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.uib.utilities.logging.Logging;

public class SavedStates {
	private Properties properties;
	private File propertiesFile;

	public SavedStates(File propertiesFile) {
		this.properties = new Properties();
		this.propertiesFile = propertiesFile;
	}

	public void load() throws IOException {
		try (FileInputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);
		} catch (FileNotFoundException e) {
			Logging.warning(this, "saved states file not found", e);
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}

	public void remove(String key) {
		properties.remove(key);
	}

	public void store() throws IOException {
		store("");
	}

	public void store(String comments) throws IOException {
		try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
			properties.store(out, comments);
		} catch (FileNotFoundException e) {
			Logging.error(this, "saved states file not found", e);
		}
	}
}
