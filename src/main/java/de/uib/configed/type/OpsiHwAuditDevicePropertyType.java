/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

public class OpsiHwAuditDevicePropertyType {
	private String opsiDbColumnName;
	private String opsiDbColumnType;

	private String hwClassName;

	private Boolean displayed;

	public OpsiHwAuditDevicePropertyType(String hwClass) {
		this.hwClassName = hwClass;
	}

	public void setOpsiDbColumnName(String s) {
		opsiDbColumnName = s;
	}

	public String getOpsiDbColumnName() {
		return opsiDbColumnName;
	}

	public void setOpsiDbColumnType(String s) {
		opsiDbColumnType = s;
	}

	public String getOpsiDbColumnType() {
		return opsiDbColumnType;
	}

	public void setDisplayed(boolean b) {
		displayed = b;
	}

	public Boolean getDisplayed() {
		return displayed;
	}

	@Override
	public String toString() {
		return hwClassName + ": " + opsiDbColumnName + " (" + displayed + ")";
	}
}
