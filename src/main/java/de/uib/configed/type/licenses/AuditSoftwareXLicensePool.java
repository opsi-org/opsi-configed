/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.type.SWAuditEntry;
import de.uib.utils.Utils;
import de.uib.utils.datastructure.Relation;
import de.uib.utils.datastructure.StringValuedRelationElement;
import de.uib.utils.logging.Logging;

public class AuditSoftwareXLicensePool extends Relation {
	public static final String SW_ID = "swId";

	private static final List<String> SOFTWARE_ATTRIBUTES;
	static {
		SOFTWARE_ATTRIBUTES = new LinkedList<>();
		SOFTWARE_ATTRIBUTES.add(LicensepoolEntry.ID_SERVICE_KEY);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.NAME);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.VERSION);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.SUB_VERSION);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.LANGUAGE);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.ARCHITECTURE);
	}

	private static final List<String> INTERFACED_ATTRIBUTES;
	static {
		INTERFACED_ATTRIBUTES = new LinkedList<>(SOFTWARE_ATTRIBUTES);
		INTERFACED_ATTRIBUTES.add(SW_ID);
	}

	public static final List<String> SERVICE_ATTRIBUTES = List.of(LicensepoolEntry.ID_SERVICE_KEY, SWAuditEntry.NAME,
			SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION, SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE);

	public AuditSoftwareXLicensePool() {
		super(SOFTWARE_ATTRIBUTES);
	}

	private static String produceSWident(Map<String, Object> m) {
		return Utils.pseudokey(new String[] { getStringValue(m.get(SWAuditEntry.NAME)),
				getStringValue(m.get(SWAuditEntry.VERSION)), getStringValue(m.get(SWAuditEntry.SUB_VERSION)),
				getStringValue(m.get(SWAuditEntry.LANGUAGE)), getStringValue(m.get(SWAuditEntry.ARCHITECTURE)) });
	}

	public static Map<String, String> produceMapFromSWident(String ident) {
		if (ident == null) {
			Logging.warning("produceMapFromSWident, ident null ");
			return new HashMap<>();
		}

		// give zero length parts as ""
		String[] parts = ident.split(";", -1);
		if (parts.length < 5) {
			Logging.warning("produceMapFromSWident, ident can not be splitted. ", ident);
		}

		Map<String, String> m = new HashMap<>();

		m.put(SWAuditEntry.NAME, parts[0]);
		m.put(SWAuditEntry.VERSION, parts[1]);
		m.put(SWAuditEntry.SUB_VERSION, parts[2]);
		m.put(SWAuditEntry.LANGUAGE, parts[3]);
		m.put(SWAuditEntry.ARCHITECTURE, parts[4]);

		return m;
	}

	public StringValuedRelationElement integrateRaw(Map<String, Object> m) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		String swIdent = "" + produceSWident(m);
		rowmap.put(SW_ID, swIdent);

		rowmap.put(LicensepoolEntry.ID_SERVICE_KEY, getStringValue(m.get(LicensepoolEntry.ID_SERVICE_KEY)));
		add(rowmap);

		return rowmap;
	}

	private static String getStringValue(Object s) {
		if (s == null) {
			return "";
		}

		return s.toString();
	}
}
