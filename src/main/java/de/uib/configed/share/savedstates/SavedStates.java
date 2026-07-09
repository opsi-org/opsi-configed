/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.share.savedstates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.uib.configed.gui.features.hwinfopage.BaseMultiClientReportPanel;
import de.uib.configed.gui.features.swinfopage.SWcsvExporter;
import de.uib.configed.share.logging.Logging;

public class SavedStates {
	private Properties properties;
	private File propertiesFile;

	public SavedStates(File propertiesFile) {
		this.properties = new Properties();
		this.propertiesFile = propertiesFile;
	}

	public void load() throws IOException {
		if (!propertiesFile.exists() && !propertiesFile.createNewFile()) {
			Logging.warning(this, "failed to create saved states properties file", propertiesFile);
		} else {
			Logging.info(this, "successfully created saved states properties file", propertiesFile);
		}

		try (FileInputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);

			replaceHWAndSWAuditExportFilePrefixDefaultValues();
		} catch (FileNotFoundException e) {
			Logging.warning(this, e, "saved states file not found");
		}
	}

	private void replaceHWAndSWAuditExportFilePrefixDefaultValues() {
		Logging.info(this,
				"checking if old default values for hwaudit_export_file_prefix and swaudit_export_file_prefix are still in use");
		boolean oldDefaultChanged = false;
		String hwAuditExportFilePrefix = getProperty("hwaudit_export_file_prefix");
		String swAuditExportFilePrefix = getProperty("swaudit_export_file_prefix");

		if (BaseMultiClientReportPanel.EXPORT_FILE_PREFIX.equals(hwAuditExportFilePrefix)) {
			setProperty("hwaudit_export_file_prefix", BaseMultiClientReportPanel.EXPORT_FILE_PREFIX);

			oldDefaultChanged = true;
		}

		if ("report_swaudit_".equals(swAuditExportFilePrefix)) {
			setProperty("swaudit_export_file_prefix", SWcsvExporter.EXPORT_FILE_PREFIX);

			oldDefaultChanged = true;
		}

		Logging.info(this,
				oldDefaultChanged ? "old defaults replaced with new defaults"
						: ("no old default values are in use: hwaudit_export_file_prefix=" + hwAuditExportFilePrefix
								+ "; swaudit_export_file_prefix=" + swAuditExportFilePrefix));
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

	public void removeAll() {
		properties.clear();
	}

	public void store() throws IOException {
		store(null);
	}

	public void store(String comments) throws IOException {
		try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
			properties.store(out, comments);
		} catch (FileNotFoundException e) {
			Logging.warning(this, e, "saved states file not found");
		}
	}
}
