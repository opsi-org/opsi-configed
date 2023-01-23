package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.Logging;

public class LicenceEntry extends TreeMap<String, Object>
// software license in opsi data base
{
	/*
	 * desc SOFTWARE_LICENSE :
	 * | Field | Type | Null | Key | Default | Extra
	 * | softwareLicenseId | varchar(100) | NO | PRI | NULL
	 * | licenseContractId | varchar(100) | NO | MUL | NULL
	 * | boundToHost | varchar(255) | YES | MUL | NULL
	 * | maxInstallations | int(11) | YES | | NULL |
	 * | expirationDate | timestamp | NO | | 0000-00-00 00:00:00
	 * | type | varchar(30) | NO | MUL | NULL |
	 */

	public static final String ID_SERVICE_KEY = "id";
	public static final String MAX_INSTALLATIONS_SERVICE_KEY = "maxInstallations";
	public static final String TYPE_SERVICE_KEY = "type";

	public static final String ID_KEY = "softwareLicenseId";
	public static final String LICENCE_CONTRACT_ID_KEY = "licenseContractId";
	public static final String BOUND_TO_HOST_KEY = "boundToHost";
	public static final String MAX_INSTALLATIONS_KEY = "maxInstallations";
	public static final String EXPIRATION_DATE_KEY = "expirationDate";
	public static final String TYPE_KEY = "licenseType";

	private static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(ID_KEY);
		KEYS.add(LICENCE_CONTRACT_ID_KEY);
		KEYS.add(BOUND_TO_HOST_KEY);
		KEYS.add(MAX_INSTALLATIONS_KEY);
		KEYS.add(EXPIRATION_DATE_KEY);
		KEYS.add(TYPE_KEY);
	}

	public static final String VOLUME = "VOLUME";
	public static final String OEM = "OEM";
	public static final String RETAIL = "RETAIL";
	public static final String CONCURRENT = "CONCURRENT";

	public static final String VOLUME_SERVICE = "VolumeSoftwareLicense";
	public static final String OEM_SERVICE = "OEMSoftwareLicense";
	public static final String RETAIL_SERVICE = "RetailSoftwareLicense";
	public static final String CONCURRENT_SERVICE = "ConcurrentSoftwareLicense";

	public static final String[] LICENCE_TYPES = new String[] { VOLUME, OEM, RETAIL, CONCURRENT };

	public static List<String> getKeys() {
		return KEYS;
	}

	private String translateTypeFromService(String servicetype) {
		switch (servicetype) {
		case VOLUME_SERVICE:
			return VOLUME;
		case OEM_SERVICE:
			return OEM;
		case RETAIL_SERVICE:
			return RETAIL;
		case CONCURRENT_SERVICE:
			return CONCURRENT;

		}

		Logging.warning(this, "illlegal servicetype " + servicetype);
		return "";
	}

	public LicenceEntry(Map<String, Object> importedEntry) {
		super(importedEntry);
		if (importedEntry.get(ID_SERVICE_KEY) != null)
			put(ID_KEY, importedEntry.get(ID_SERVICE_KEY));

		if (get(ID_KEY) == null)
			Logging.warning(this, "missing primary key in " + importedEntry);

		if (importedEntry.get(MAX_INSTALLATIONS_SERVICE_KEY) == null) {
			importedEntry.put(MAX_INSTALLATIONS_KEY, ExtendedInteger.ZERO);
		}

		else {
			if (!(importedEntry.get(MAX_INSTALLATIONS_SERVICE_KEY) instanceof Integer)) {
				Logging.warning(this, " " + importedEntry.get(ID_KEY) + " has not an integer for "
						+ importedEntry.get(MAX_INSTALLATIONS_SERVICE_KEY));
			} else {
				int val = (Integer) importedEntry.get(MAX_INSTALLATIONS_SERVICE_KEY);
				if (val == 0)
					put(MAX_INSTALLATIONS_KEY, ExtendedInteger.INFINITE);
				else
					put(MAX_INSTALLATIONS_KEY, new ExtendedInteger(val));
			}
		}
		if (importedEntry.get(TYPE_SERVICE_KEY) != null)
			put(TYPE_KEY, translateTypeFromService((String) importedEntry.get(TYPE_SERVICE_KEY)));
	}

	public String getId() {
		return (String) get(ID_KEY);
	}

	public ExtendedInteger getMaxInstallations() {
		return (ExtendedInteger) get(MAX_INSTALLATIONS_KEY);
	}

	public static String produceNormalizedCount(String count) {
		if (count == null)
			return null;

		if (count.trim().equals("0"))
			return "0";

		ExtendedInteger ei = new ExtendedInteger(count);

		if (ei.equals(ExtendedInteger.INFINITE))
			return "0";

		return count;
	}

}
