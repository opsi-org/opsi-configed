/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

public class RemoteControl {
	public static final String CONFIG_KEY = "configed.remote_control";
	public static final String COMMAND_KEY = "command";
	public static final String DESCRIPTION_KEY = "description";
	public static final String EDITABLE_KEY = "editable";

	private String name = "";
	private String command = "";
	private String description = "";
	private String editable = "true";

	public void setName(Object s) {
		name = "" + s;
	}

	public void setCommand(Object s) {
		command = "" + s;
	}

	public void setDescription(Object s) {
		description = "" + s;
	}

	public void setEditable(Object s) {
		editable = "" + s;
	}

	public String getCommand() {
		return command;
	}

	public String getDescription() {
		return description;
	}

	public String getEditable() {
		return editable;
	}

	@Override
	public String toString() {
		return name + ": " + command + " ( " + description + ") editable " + editable;
	}
}
