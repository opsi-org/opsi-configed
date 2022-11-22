package de.uib.configed.type;

public class AdditionalQuery {

	public static final String CONFIG_KEY = "configed.query_supplementary";
	public static final String QUERY_KEY = "query";
	public static final String DESCRIPTION_KEY = "description";
	public static final String EDITABLE_KEY = "editable";

	public String name = "";
	public String query = "";
	public String description = "";
	public String editable = "true";

	public AdditionalQuery() {
	}

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

	public String toString() {
		return getName() + ": " + " ( " + getDescription() + ") editable " + getEditable() + getQuery();
	}
}
