/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class LicenceUsageEntry extends StringValuedRelationElement {
	/*
	 * describe LICENSE_ON_CLIENT:
	 * | Field | Type | Null | Key | Default | Extra
	 * | license_on_client_id | int(11) | NO | PRI | NULL | auto_increment |
	 * | softwareLicenseId | varchar(100) | NO | MUL | NULL |
	 * | licensePoolId | varchar(100) | NO | | NULL |
	 * | clientId | varchar(255) | YES | MUL | NULL |
	 * | licenseKey | varchar(100) | YES | | NULL |
	 * | notes | varchar(1024) | YES | | NULL |
	 */

	public static final String IDENT_KEY = "ident";
	public static final String ID_KEY = "license_on_client_id";
	public static final String LICENCE_POOL_ID_KEY = "licensePoolId";
	public static final String LICENCE_ID_KEY = "softwareLicenseId";
	public static final String CLIENT_ID_KEY = "clientId";
	public static final String LICENCE_KEY_KEY = "licenseKey";
	public static final String NOTES_KEY = "notes";

	public static final String OPSI_NOM_TYPE = "LicenseOnClient";

	private static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(ID_KEY);
		KEYS.add(LICENCE_POOL_ID_KEY);
		KEYS.add(LICENCE_ID_KEY);
		KEYS.add(CLIENT_ID_KEY);
		KEYS.add(LICENCE_KEY_KEY);
		KEYS.add(NOTES_KEY);
	}

	public LicenceUsageEntry(String hostId, String softwareLicenceId, String licencePoolId, String licenceKey,
			String notes) {
		super();

		super.setAllowedAttributes(KEYS);

		if (hostId == null) {
			super.put(CLIENT_ID_KEY, "");
		} else {
			super.put(CLIENT_ID_KEY, hostId);
		}

		if (softwareLicenceId == null) {
			super.put(LICENCE_ID_KEY, "");
		} else {
			super.put(LICENCE_ID_KEY, softwareLicenceId);
		}

		if (licencePoolId == null) {
			super.put(LICENCE_POOL_ID_KEY, "");
		} else {
			super.put(LICENCE_POOL_ID_KEY, licencePoolId);
		}

		if (licenceKey == null) {
			super.put(LICENCE_KEY_KEY, "");
		} else {
			super.put(LICENCE_KEY_KEY, licenceKey);
		}

		if (notes == null) {
			super.put(NOTES_KEY, "");
		} else {
			super.put(NOTES_KEY, notes);
		}
	}

	public LicenceUsageEntry(Map<String, Object> entry) {
		super();
		super.setAllowedAttributes(KEYS);

		entry.remove(IDENT_KEY);
		for (Entry<String, Object> mapEntry : entry.entrySet()) {
			super.put(mapEntry.getKey(), "" + mapEntry.getValue());
		}

		if (super.get(LICENCE_ID_KEY) == null || super.get(LICENCE_POOL_ID_KEY) == null) {
			Logging.warning(this.getClass(), "missing values " + entry);
		}
	}

	public String getClientId() {
		return get(CLIENT_ID_KEY);
	}

	public String getLicensePool() {
		return get(LICENCE_POOL_ID_KEY);
	}

	public String getLicenceId() {
		return get(LICENCE_ID_KEY);
	}

	public Map<String, Object> getNOMobject() {
		Map<String, Object> m = new HashMap<>();
		m.put(CLIENT_ID_KEY, getClientId());
		m.put(LICENCE_ID_KEY, getLicenceId());
		m.put(LICENCE_POOL_ID_KEY, getLicensePool());
		m.put("type", OPSI_NOM_TYPE);
		return m;
	}

	public static String produceKey(String hostId, String licencePoolId, String licenceId) {
		return Utils.pseudokey(new String[] { hostId, licencePoolId, licenceId });
	}

	public String getPseudoKey() {
		return produceKey(getClientId(), getLicensePool(), getLicenceId());
	}

}
