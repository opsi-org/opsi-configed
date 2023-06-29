/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import java.util.Map;

import de.uib.utilities.ExtendedDate;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.Logging;

public class ModulePermissionValue {

	public static final Map<String, Boolean> MODULE_CHECKED = Map.ofEntries(Map.entry("license_management", true),
			Map.entry("local_imaging", true), Map.entry("monitoring", true), Map.entry("wim-capture", true),
			Map.entry("scalability1", true), Map.entry("linux_agent", true), Map.entry("vpn", true),
			Map.entry("mysql_backend", true), Map.entry("uefi", true), Map.entry("userroles", true),
			Map.entry("directory-connector", true), Map.entry("macos_agent", true), Map.entry("secureboot", true),
			Map.entry("win-vhd", true), Map.entry("os_install_by_wlan", true));

	private ExtendedInteger maxClients;
	private ExtendedDate expiresDate;
	private Boolean booleanValue;

	public ModulePermissionValue(Object ob, ExtendedDate defaultExpires) {
		Logging.info(this, "value object given: " + ob);
		booleanValue = null;
		expiresDate = ExtendedDate.ZERO;
		maxClients = ExtendedInteger.ZERO;
		if (ob != null) {
			booleanValue = checkBoolean(ob);

			if (booleanValue == null) {
				expiresDate = retrieveExpiresDate(ob);
				maxClients = retrieveMaxClients(ob);
				Logging.debug(this, "maxClients directly given " + maxClients);
			} else if (Boolean.TRUE.equals(booleanValue)) {
				maxClients = ExtendedInteger.INFINITE;
			} else {
				// maxClients may remain zero since value is false
			}
		}

		if (expiresDate == ExtendedDate.ZERO) {
			expiresDate = defaultExpires;
		}
	}

	private static Boolean checkBoolean(Object ob) {

		Boolean result = null;

		if (ob instanceof Boolean) {
			result = (Boolean) ob;
		} else if (ob instanceof String) {
			String sValue = ((String) ob).trim();
			boolean checked = "yes".equalsIgnoreCase(sValue) || "true".equalsIgnoreCase(sValue);
			if (checked) {
				result = "yes".equalsIgnoreCase(sValue);
			}
		} else {
			Logging.info("ob cannot be interpreted as boolean, it is " + ob.toString());
		}

		return result;
	}

	private ExtendedInteger retrieveMaxClients(Object ob) {
		ExtendedInteger result = null;

		if (ob == null) {
			result = ExtendedInteger.ZERO;
		} else {
			Boolean b = checkBoolean(ob);
			if (b != null) {
				if (b) {
					result = ExtendedInteger.INFINITE;
				} else {
					result = ExtendedInteger.ZERO;
				}
			} else if (ob instanceof Integer) {
				result = new ExtendedInteger((Integer) ob);
			} else if (ob instanceof String) {
				Integer number = null;
				try {
					number = Integer.valueOf((String) ob);
				} catch (NumberFormatException ex) {
					Logging.debug(this, "not a number: " + ob);
				}
				if (number != null) {
					result = new ExtendedInteger(number);
				}
			} else {
				Logging.warning(this, "ob has unexpected type " + ob.getClass() + " in retrieveMaxClients");
			}
		}

		return result;
	}

	private ExtendedDate retrieveExpiresDate(Object ob) {
		ExtendedDate result = null;

		if (ob != null) {
			result = new ExtendedDate(ob);
		}

		if (result == null || result.getDate() == null) {
			result = ExtendedDate.ZERO;
		}

		return result;
	}

	public ExtendedInteger getMaxClients() {
		return maxClients;
	}

	public ExtendedDate getExpires() {
		return expiresDate;
	}

	public Boolean getBoolean() {
		return booleanValue;
	}

	@Override
	public String toString() {
		return "  :" + getBoolean() + " maxClients: " + getMaxClients() + ";  expires  " + getExpires();
	}
}
