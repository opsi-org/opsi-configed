/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

public class AdditionalQuery {

	public static final String CONFIG_KEY = "configed.query_supplementary";
	public static final String QUERY_KEY = "query";
	public static final String DESCRIPTION_KEY = "description";
	public static final String EDITABLE_KEY = "editable";

	private String name = "";
	private String query = "";
	private String description = "";
	private String editable = "true";

	public void setName(Object s) {
		name = "" + s;
	}

	public void setQuery(Object s) {
		query = "" + s;
	}

	public void setDescription(Object s) {
		description = "" + s;
	}

	public void setEditable(Object s) {
		editable = "" + s;
	}

	public String getName() {
		return name;
	}

	public String getQuery() {
		return query;
	}

	public String getDescription() {
		return description;
	}

	public String getEditable() {
		return editable;
	}

	@Override
	public String toString() {
		return getName() + ": " + " ( " + getDescription() + ") editable " + getEditable() + getQuery();
	}
}
