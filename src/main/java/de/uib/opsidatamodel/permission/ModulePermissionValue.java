package de.uib.opsidatamodel.permission;

import java.util.HashMap;
import java.util.Map;

import de.uib.utilities.ExtendedDate;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.Logging;

public class ModulePermissionValue {
	de.uib.opsicommand.Executioner exec;

	private ExtendedInteger maxClients;
	private ExtendedDate expiresDate;
	private Boolean booleanValue;

	public static final String KEY_EXPIRES = "expires";
	public static final String KEY_MAX_CLIENTS = "maxclients";

	public static final Map<String, Boolean> MODULE_CHECKED = Map.ofEntries(Map.entry("license_management", true),
			Map.entry("local_imaging", true), Map.entry("monitoring", true), Map.entry("wim-capture", true),
			Map.entry("scalability1", true), Map.entry("linux_agent", true), Map.entry("vpn", true),
			Map.entry("mysql_backend", true), Map.entry("uefi", true), Map.entry("userroles", true),
			Map.entry("directory-connector", true), Map.entry("macos_agent", true), Map.entry("secureboot", true),
			Map.entry("win-vhd", true), Map.entry("os_install_by_wlan", true));

	private Boolean checkBoolean(Object ob) {

		Boolean result = null;

		if (ob instanceof Boolean) {
			result = (Boolean) ob;
		}

		else if (ob instanceof String) {
			String sValue = ((String) ob).trim();
			boolean checked = sValue.equalsIgnoreCase("yes") || sValue.equalsIgnoreCase("true");
			if (checked)
				result = sValue.equalsIgnoreCase("yes");
		}

		return result;
	}

	private ExtendedInteger retrieveMaxClients(Object ob) {
		ExtendedInteger result = null;

		if (ob == null)

			result = ExtendedInteger.ZERO;

		else {
			Boolean b = checkBoolean(ob);
			if (b != null) {
				if (b)
					result = ExtendedInteger.INFINITE;
				else
					result = ExtendedInteger.ZERO;
			} else if (ob instanceof Integer) {
				result = new ExtendedInteger((Integer) ob);
			} else if (ob instanceof String) {
				Integer number = null;
				try {
					number = Integer.valueOf((String) ob);
				} catch (NumberFormatException ex) {
					Logging.debug(this, "not a number: " + ob);
				}
				if (number != null)
					result = new ExtendedInteger(number);
			}

		}

		return result;
	}

	private ExtendedDate retrieveExpiresDate(Object ob) {
		ExtendedDate result = null;

		if (ob != null) {
			try {
				result = new ExtendedDate(ob);
			} catch (ClassCastException ex) {
				Logging.warning(this, "no String: " + ob);
			} catch (Exception ex) {
				Logging.debug(this, "DateParseException for " + ob);
			}

		}

		if (result == null)
			result = ExtendedDate.ZERO;

		return result;
	}

	private Map<String, Object> interpretAsJson(Object ob) {
		Map<String, Object> result = exec.getMapFromItem(ob);

		if (result.entrySet().isEmpty())
			return new HashMap<>();

		return result;
	}

	public ModulePermissionValue(de.uib.opsicommand.Executioner exec, Object ob, ExtendedDate defaultExpires) {
		this.exec = exec;
		Logging.info(this, "value object given: " + ob);
		booleanValue = null;
		expiresDate = ExtendedDate.ZERO;
		maxClients = ExtendedInteger.ZERO;
		if (ob != null) {
			Map<String, Object> detailled = interpretAsJson(ob);
			Logging.debug(this, "detailled " + detailled);
			if (!detailled.isEmpty()) {
				maxClients = retrieveMaxClients(detailled.get(KEY_MAX_CLIENTS));
				Logging.debug(this, "detailled  maxClients " + maxClients);
				expiresDate = retrieveExpiresDate(detailled.get(KEY_EXPIRES));
			} else {
				booleanValue = checkBoolean(ob);

				if (booleanValue == null) {
					expiresDate = retrieveExpiresDate(ob);
					maxClients = retrieveMaxClients(ob);
					Logging.debug(this, "maxClients directly given " + maxClients);
				} else if (Boolean.TRUE.equals(booleanValue)) {
					maxClients = ExtendedInteger.INFINITE;
				} else {
					maxClients = ExtendedInteger.ZERO;
				}
			}
		}

		if (expiresDate == ExtendedDate.ZERO)
			expiresDate = defaultExpires;

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
