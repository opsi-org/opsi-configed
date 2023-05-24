/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

public class SavedSearch {

	public static final String CONFIG_KEY = "configed.saved_search";
	public static final String NAME_KEY = "name";
	public static final String DESCRIPTION_KEY = "description";

	private String name = "";
	private String serialization = "";
	private String description = "";

	public SavedSearch() {
	}

	public SavedSearch(String name, String serialization, String description) {
		setName(name);
		setSerialization(serialization);
		setDescription(description);
	}

	public void setName(Object s) {
		name = "" + s;
	}

	public void setSerialization(Object s) {
		serialization = "" + s;
	}

	public void setDescription(Object s) {
		if (s != null) {
			description = "" + s;
		} else {
			description = name;
		}
	}

	public String getName() {
		return name;
	}

	public String getSerialization() {
		return serialization;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return getName() + " ( " + getDescription() + "): " + getSerialization();
	}
}
