/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utils.Utils;
import de.uib.utils.datastructure.StringValuedRelationElement;
import de.uib.utils.logging.Logging;

public class LicenseUsageEntry extends StringValuedRelationElement {
	public static final String IDENT_KEY = "ident";
	public static final String ID_KEY = "license_on_client_id";
	public static final String LICENSE_POOL_ID_KEY = "licensePoolId";
	public static final String LICENSE_ID_KEY = "softwareLicenseId";
	public static final String CLIENT_ID_KEY = "clientId";
	public static final String LICENSE_KEY_KEY = "licenseKey";
	public static final String NOTES_KEY = "notes";

	public static final String OPSI_NOM_TYPE = "LicenseOnClient";

	private static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(ID_KEY);
		KEYS.add(LICENSE_POOL_ID_KEY);
		KEYS.add(LICENSE_ID_KEY);
		KEYS.add(CLIENT_ID_KEY);
		KEYS.add(LICENSE_KEY_KEY);
		KEYS.add(NOTES_KEY);
	}

	public LicenseUsageEntry(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey,
			String notes) {
		super();

		super.setAllowedAttributes(KEYS);

		if (hostId == null) {
			super.put(CLIENT_ID_KEY, "");
		} else {
			super.put(CLIENT_ID_KEY, hostId);
		}

		if (softwareLicenseId == null) {
			super.put(LICENSE_ID_KEY, "");
		} else {
			super.put(LICENSE_ID_KEY, softwareLicenseId);
		}

		if (licensePoolId == null) {
			super.put(LICENSE_POOL_ID_KEY, "");
		} else {
			super.put(LICENSE_POOL_ID_KEY, licensePoolId);
		}

		if (licenseKey == null) {
			super.put(LICENSE_KEY_KEY, "");
		} else {
			super.put(LICENSE_KEY_KEY, licenseKey);
		}

		if (notes == null) {
			super.put(NOTES_KEY, "");
		} else {
			super.put(NOTES_KEY, notes);
		}
	}

	public LicenseUsageEntry(Map<String, Object> entry) {
		super();
		super.setAllowedAttributes(KEYS);

		entry.remove(IDENT_KEY);
		for (Entry<String, Object> mapEntry : entry.entrySet()) {
			super.put(mapEntry.getKey(), "" + mapEntry.getValue());
		}

		if (super.get(LICENSE_ID_KEY) == null || super.get(LICENSE_POOL_ID_KEY) == null) {
			Logging.warning(this.getClass(), "missing values ", entry);
		}
	}

	public String getClientId() {
		return get(CLIENT_ID_KEY);
	}

	public String getLicensePool() {
		return get(LICENSE_POOL_ID_KEY);
	}

	public String getLicenseId() {
		return get(LICENSE_ID_KEY);
	}

	public Map<String, Object> getNOMobject() {
		Map<String, Object> m = new HashMap<>();
		m.put(CLIENT_ID_KEY, getClientId());
		m.put(LICENSE_ID_KEY, getLicenseId());
		m.put(LICENSE_POOL_ID_KEY, getLicensePool());
		m.put("type", OPSI_NOM_TYPE);
		return m;
	}

	public static String produceKey(String hostId, String licensePoolId, String licenseId) {
		return Utils.pseudokey(new String[] { hostId, licensePoolId, licenseId });
	}

	public String getPseudoKey() {
		return produceKey(getClientId(), getLicensePool(), getLicenseId());
	}
}
