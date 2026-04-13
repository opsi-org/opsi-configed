/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.type.licenses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.ConfigUtils;
import de.uib.configed.share.datastructure.StringValuedRelationElement;
import de.uib.configed.share.logging.Logging;

public class AuditSoftwareXLicensePool {
	public static final String SW_ID = "swId";

	private static final List<String> SOFTWARE_ATTRIBUTES = List.of(LicensepoolEntry.ID_SERVICE_KEY, SWAuditEntry.NAME,
			SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION, SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE);

	private static final List<String> INTERFACED_ATTRIBUTES = Stream
			.concat(SOFTWARE_ATTRIBUTES.stream(), Stream.of(SW_ID)).toList();

	public static final List<String> SERVICE_ATTRIBUTES = List.of(LicensepoolEntry.ID_SERVICE_KEY, SWAuditEntry.NAME,
			SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION, SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE);

	private List<StringValuedRelationElement> relations = new ArrayList<>();

	private static String produceSWident(Map<String, Object> m) {
		return ConfigUtils.pseudokey(new String[] { getStringValue(m.get(SWAuditEntry.NAME)),
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
		relations.add(rowmap);

		return rowmap;
	}

	public List<StringValuedRelationElement> getRelations() {
		return relations;
	}

	private static String getStringValue(Object s) {
		if (s == null) {
			return "";
		}

		return s.toString();
	}
}
