/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uib.utils.ExtendedInteger;
import de.uib.utils.logging.Logging;

// software license in opsi data base
public class LicenseEntry extends TreeMap<String, Object> {
	public static final String ID_SERVICE_KEY = "id";
	public static final String TYPE_SERVICE_KEY = "type";

	public static final String ID_KEY = "softwareLicenseId";
	public static final String LICENSE_CONTRACT_ID_KEY = "licenseContractId";
	public static final String BOUND_TO_HOST_KEY = "boundToHost";
	public static final String MAX_INSTALLATIONS_KEY = "maxInstallations";
	public static final String EXPIRATION_DATE_KEY = "expirationDate";
	public static final String TYPE_KEY = "licenseType";

	public static final String VOLUME = "VOLUME";
	public static final String OEM = "OEM";
	public static final String RETAIL = "RETAIL";
	public static final String CONCURRENT = "CONCURRENT";

	public static final String VOLUME_SERVICE = "VolumeSoftwareLicense";
	public static final String OEM_SERVICE = "OEMSoftwareLicense";
	public static final String RETAIL_SERVICE = "RetailSoftwareLicense";
	public static final String CONCURRENT_SERVICE = "ConcurrentSoftwareLicense";

	public static final List<String> LICENSE_TYPES = List.of(VOLUME, OEM, RETAIL, CONCURRENT);

	public LicenseEntry(Map<String, Object> importedEntry) {
		super(importedEntry);
		if (importedEntry.get(ID_SERVICE_KEY) != null) {
			super.put(ID_KEY, importedEntry.get(ID_SERVICE_KEY));
		}

		if (super.get(ID_KEY) == null) {
			Logging.warning(this.getClass(), "missing primary key in " + importedEntry);
		}

		Object maxInstallations = importedEntry.get(MAX_INSTALLATIONS_KEY);

		if (maxInstallations == null) {
			importedEntry.put(MAX_INSTALLATIONS_KEY, ExtendedInteger.ZERO);
		} else if (maxInstallations instanceof Integer integer) {
			if (integer == 0) {
				super.put(MAX_INSTALLATIONS_KEY, ExtendedInteger.INFINITE);
			} else {
				super.put(MAX_INSTALLATIONS_KEY, new ExtendedInteger(integer));
			}
		} else {
			Logging.warning(this.getClass(),
					" " + importedEntry.get(ID_KEY) + " has not an integer for " + maxInstallations);
		}

		if (importedEntry.get(TYPE_SERVICE_KEY) != null) {
			super.put(TYPE_KEY, translateTypeFromService((String) importedEntry.get(TYPE_SERVICE_KEY)));
		}
	}

	private String translateTypeFromService(String servicetype) {
		String result = "";
		switch (servicetype) {
		case VOLUME_SERVICE:
			result = VOLUME;
			break;
		case OEM_SERVICE:
			result = OEM;
			break;
		case RETAIL_SERVICE:
			result = RETAIL;
			break;
		case CONCURRENT_SERVICE:
			result = CONCURRENT;
			break;
		default:
			Logging.warning(this, "illlegal servicetype " + servicetype);
			result = "";
		}
		return result;
	}

	public String getId() {
		return (String) get(ID_KEY);
	}

	public ExtendedInteger getMaxInstallations() {
		return (ExtendedInteger) get(MAX_INSTALLATIONS_KEY);
	}

	public static String produceNormalizedCount(String count) {
		if (count == null) {
			return null;
		}

		if ("0".equals(count.trim()) || new ExtendedInteger(count).equals(ExtendedInteger.INFINITE)) {
			return "0";
		}

		return count;
	}
}
