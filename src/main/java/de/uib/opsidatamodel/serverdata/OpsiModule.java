/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

public enum OpsiModule {
	LICENSE_MANAGEMENT("license_management"), UEFI("uefi"), USER_ROLES("userroles"), LOCAL_IMAGING("local_imaging"),
	VPN("vpn");

	private final String displayName;

	OpsiModule(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
