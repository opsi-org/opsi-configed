package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.logging;

public class LicenceEntry extends TreeMap<String, Object>
// software license in opsi data base
{
	/*
	 * desc SOFTWARE_LICENSE ;
	 * | Field | Type | Null | Key | Default | Extra
	 * | softwareLicenseId | varchar(100) | NO | PRI | NULL
	 * | licenseContractId | varchar(100) | NO | MUL | NULL
	 * | boundToHost | varchar(255) | YES | MUL | NULL
	 * | maxInstallations | int(11) | YES | | NULL |
	 * | expirationDate | timestamp | NO | | 0000-00-00 00:00:00
	 * | type | varchar(30) | NO | MUL | NULL |
	 */

	public static final String idSERVICEKEY = "id";
	public static final String licenceContractIdSERVICEKEY = "licenseContractId";
	public static final String boundToHostSERVICEKEY = "boundToHost";
	public static final String maxInstallationsSERVICEKEY = "maxInstallations";
	public static final String expirationDateSERVICEKEY = "expirationDate";
	public static final String typeSERVICEKEY = "type";

	public static final String idKEY = "softwareLicenseId";
	public static final String licenceContractIdKEY = "licenseContractId";
	public static final String boundToHostKEY = "boundToHost";
	public static final String maxInstallationsKEY = "maxInstallations";
	public static final String expirationDateKEY = "expirationDate";
	public static final String typeKEY = "licenseType";

	private static List<String> KEYS;
	static {
		KEYS = new ArrayList<String>();
		KEYS.add(idKEY);
		KEYS.add(licenceContractIdKEY);
		KEYS.add(boundToHostKEY);
		KEYS.add(maxInstallationsKEY);
		KEYS.add(expirationDateKEY);
		KEYS.add(typeKEY);

	}

	public static final String VOLUME = "VOLUME";
	public static final String OEM = "OEM";
	public static final String RETAIL = "RETAIL";
	public static final String CONCURRENT = "CONCURRENT";

	public static final String VOLUMEservice = "VolumeSoftwareLicense";
	public static final String OEMservice = "OEMSoftwareLicense";
	public static final String RETAILservice = "RetailSoftwareLicense";
	public static final String CONCURRENTservice = "ConcurrentSoftwareLicense";

	public static final String[] LICENCE_TYPES = new String[] { VOLUME, OEM, RETAIL, CONCURRENT };

	public static List<String> getKeys() {
		return KEYS;
	}

	private String translateTypeFromService(String servicetype) {
		switch (servicetype) {
		case VOLUMEservice:
			return VOLUME;
		case OEMservice:
			return OEM;
		case RETAILservice:
			return RETAIL;
		case CONCURRENTservice:
			return CONCURRENT;

		}

		logging.warning(this, "illlegal servicetype " + servicetype);
		return "";
	}

	/*
	 * @Override
	 * public Object put(String key, Object value)
	 * {
	 * assert KEYS.indexOf(key) > -1 : "not valid key " + key;
	 * 
	 * if (KEYS.indexOf(key) > -1)
	 * {
	 * if (key.equals(typeKEY) && TYPE_LIST.indexOf(value) == -1)
	 * logging.warning(this, "value " + value + " not possible for key " + key);
	 * 
	 * return super.put(key, value);
	 * }
	 * 
	 * return null;
	 * 
	 * }
	 */

	public LicenceEntry(Map<String, Object> importedEntry) {
		super(importedEntry);
		if (importedEntry.get(idSERVICEKEY) != null)
			put(idKEY, importedEntry.get(idSERVICEKEY));

		if (get(idKEY) == null)
			logging.warning(this, "missing primary key in " + importedEntry);

		if (importedEntry.get(maxInstallationsSERVICEKEY) == null) {
			importedEntry.put(maxInstallationsKEY, ExtendedInteger.ZERO);
		}

		else {
			if (!(importedEntry.get(maxInstallationsSERVICEKEY) instanceof Integer)) {
				logging.warning(this, " " + importedEntry.get(idKEY) + " has not an integer for "
						+ importedEntry.get(maxInstallationsSERVICEKEY));
			} else {
				int val = (Integer) importedEntry.get(maxInstallationsSERVICEKEY);
				if (val == 0)
					put(maxInstallationsKEY, ExtendedInteger.INFINITE);
				else
					put(maxInstallationsKEY, new ExtendedInteger(val));
			}
		}
		if (importedEntry.get(typeSERVICEKEY) != null)
			put(typeKEY, translateTypeFromService((String) importedEntry.get(typeSERVICEKEY)));
	}

	public String getId() {
		return (String) get(idKEY);
	}

	public ExtendedInteger getMaxInstallations() {
		return (ExtendedInteger) get(maxInstallationsKEY);
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
