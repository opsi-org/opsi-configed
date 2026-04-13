/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.permission;

import de.uib.configed.share.ExtendedDate;
import de.uib.configed.share.ExtendedInteger;
import de.uib.configed.share.logging.Logging;

public class ModulePermissionValue {
	private ExtendedInteger maxClients;
	private ExtendedDate expiresDate;
	private Boolean booleanValue;

	public ModulePermissionValue(Object ob, ExtendedDate defaultExpires) {
		Logging.info(this, "value object given: ", ob);
		booleanValue = null;
		expiresDate = ExtendedDate.ZERO;
		maxClients = ExtendedInteger.ZERO;
		if (ob != null) {
			booleanValue = checkBoolean(ob);

			if (booleanValue == null) {
				expiresDate = retrieveExpiresDate(ob);
				maxClients = retrieveMaxClients(ob);
				Logging.debug(this, "maxClients directly given ", maxClients);
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
		return switch (ob) {
		case Boolean b -> b;
		case String stringValue -> {
			stringValue = stringValue.trim();
			boolean checked = "yes".equalsIgnoreCase(stringValue) || "true".equalsIgnoreCase(stringValue);
			yield checked ? "yes".equalsIgnoreCase(stringValue) : null;
		}
		default -> {
			Logging.info("ob cannot be interpreted as boolean, it is ", ob);
			yield null;
		}
		};
	}

	private ExtendedInteger retrieveMaxClients(Object object) {
		if (object == null) {
			return ExtendedInteger.ZERO;
		}

		Boolean b = checkBoolean(object);
		if (b != null) {
			return Boolean.TRUE.equals(b) ? ExtendedInteger.INFINITE : ExtendedInteger.ZERO;
		} else {
			return switch (object) {
			case Integer extendedInteger -> new ExtendedInteger(extendedInteger);
			case String string -> parseStringToExtendedInteger(string);
			default -> {
				Logging.warning(this, "ob has unexpected type ", object.getClass(), " in retrieveMaxClients");
				yield null;
			}
			};
		}
	}

	private static ExtendedInteger parseStringToExtendedInteger(String string) {
		try {
			return new ExtendedInteger(Integer.valueOf(string));
		} catch (NumberFormatException ex) {
			Logging.debug("not a number: ", string);
			return null;
		}
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
