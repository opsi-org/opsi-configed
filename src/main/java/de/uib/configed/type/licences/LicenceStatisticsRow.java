/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licences;

import java.util.HashMap;

import de.uib.utilities.ExtendedInteger;

public class LicenceStatisticsRow extends HashMap<String, String> {
	// callkeys
	public static final String ID_KEY = "licensePoolId";
	public static final String LICENSE_OPTIONS_KEY = "licence_options";
	public static final String USED_BY_OPSI_KEY = "used_by_opsi";
	public static final String REMAINING_OPSI_KEY = "remaining_opsi";
	public static final String SW_INVENTORY_USED_KEY = "SWinventory_used";
	public static final String SW_INVENTORY_REMAINING_KEY = "SWinventory_remaining";

	private static final String ZERO = "0";

	private ExtendedInteger allowedUsages;

	public LicenceStatisticsRow(String licencePool) {
		super();
		super.put(USED_BY_OPSI_KEY, ZERO);
		super.put(ID_KEY, licencePool);
		super.put(LICENSE_OPTIONS_KEY, ZERO);
		super.put(REMAINING_OPSI_KEY, ZERO);
		super.put(SW_INVENTORY_USED_KEY, ZERO);
		super.put(SW_INVENTORY_REMAINING_KEY, ZERO);
		allowedUsages = ExtendedInteger.ZERO;
	}

	public void setAllowedUsagesCount(ExtendedInteger count) {
		if (count != null) {
			String value = count.getDisplay();
			allowedUsages = count;
			put(LICENSE_OPTIONS_KEY, value);
			put(REMAINING_OPSI_KEY, value);
			put(SW_INVENTORY_REMAINING_KEY, value);
		}
	}

	public void setOpsiUsagesCount(Integer count) {
		if (count != null) {
			put(USED_BY_OPSI_KEY, count.toString());
			put(REMAINING_OPSI_KEY, allowedUsages.add(count).getDisplay());
		}
	}

	public void setSWauditUsagesCount(Integer count) {
		if (count != null) {
			put(SW_INVENTORY_USED_KEY, count.toString());
			put(SW_INVENTORY_REMAINING_KEY, allowedUsages.add(count).getDisplay());
		}
	}
}
