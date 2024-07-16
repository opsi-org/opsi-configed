/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.permission;

import de.uib.utils.ExtendedDate;
import de.uib.utils.ExtendedInteger;
import de.uib.utils.logging.Logging;

public class ModulePermissionValue {
	private ExtendedInteger maxClients;
	private ExtendedDate expiresDate;
	private Boolean booleanValue;

	public ModulePermissionValue(Object ob, ExtendedDate defaultExpires) {
		Logging.info(this.getClass(), "value object given: ", ob);
		booleanValue = null;
		expiresDate = ExtendedDate.ZERO;
		maxClients = ExtendedInteger.ZERO;
		if (ob != null) {
			booleanValue = checkBoolean(ob);

			if (booleanValue == null) {
				expiresDate = retrieveExpiresDate(ob);
				maxClients = retrieveMaxClients(ob);
				Logging.debug(this.getClass(), "maxClients directly given " + maxClients);
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

		if (ob instanceof Boolean b) {
			result = b;
		} else if (ob instanceof String stringValue) {
			stringValue = stringValue.trim();
			boolean checked = "yes".equalsIgnoreCase(stringValue) || "true".equalsIgnoreCase(stringValue);
			if (checked) {
				result = "yes".equalsIgnoreCase(stringValue);
			}
		} else {
			Logging.info("ob cannot be interpreted as boolean, it is ", ob);
		}

		return result;
	}

	private ExtendedInteger retrieveMaxClients(Object object) {
		if (object == null) {
			return ExtendedInteger.ZERO;
		}

		ExtendedInteger result = null;

		Boolean b = checkBoolean(object);
		if (b != null) {
			if (Boolean.TRUE.equals(b)) {
				result = ExtendedInteger.INFINITE;
			} else {
				result = ExtendedInteger.ZERO;
			}
		} else if (object instanceof Integer integer) {
			result = new ExtendedInteger(integer);
		} else if (object instanceof String string) {
			Integer number = null;
			try {
				number = Integer.valueOf(string);
			} catch (NumberFormatException ex) {
				Logging.debug(this, "not a number: " + object);
			}
			if (number != null) {
				result = new ExtendedInteger(number);
			}
		} else {
			Logging.warning(this, "ob has unexpected type ", object.getClass(), " in retrieveMaxClients");
		}

		return result;
	}

	private static ExtendedDate retrieveExpiresDate(Object ob) {
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
