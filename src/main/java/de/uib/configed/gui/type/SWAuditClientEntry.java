/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.share.Utils;

public class SWAuditClientEntry {
	public static final String CLIENT_ID = "clientId";
	public static final String LICENSE_KEY = "licenseKey";
	public static final String LAST_MODIFICATION = "lastseen";
	public static final List<String> KEYS = List.of(SWAuditEntry.NAME, SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION,
			SWAuditEntry.ARCHITECTURE, SWAuditEntry.LANGUAGE, LICENSE_KEY, SWAuditEntry.WINDOWS_SOFTWARE_ID,
			SWAuditEntry.IS_OPERATING_SYSTEM);

	private String swIdent;

	private final Map<String, String> data;
	private final Map<String, Object> originalMap;

	public SWAuditClientEntry(final Map<String, Object> originalMap) {
		this.originalMap = originalMap;
		data = new HashMap<>();
		data.put(SWAuditEntry.ID, produceNonNull(originalMap.get(CLIENT_ID)));
		swIdent = produceSWIdent(originalMap);
		data.put(LICENSE_KEY, produceNonNull(originalMap.get(LICENSE_KEY)));
	}

	private static String produceNonNull(Object o) {
		return o != null ? o.toString() : "";
	}

	private static String produceSWIdent(Map<String, Object> readMap) {
		return Utils.pseudokey(new String[] { (String) readMap.get(SWAuditEntry.NAME),
				(String) readMap.get(SWAuditEntry.VERSION), (String) readMap.get(SWAuditEntry.SUB_VERSION),
				(String) readMap.get(SWAuditEntry.LANGUAGE), (String) readMap.get(SWAuditEntry.ARCHITECTURE) });
	}

	public String getClientId() {
		return data.get(SWAuditEntry.ID);
	}

	public String getLastModification() {
		return Utils.formatDateTimeStringToLocal((String) originalMap.get(LAST_MODIFICATION));
	}

	public String getSWIdent() {
		return swIdent;
	}

	public Map<String, Object> getExpandedMap(SWAuditEntry swAuditEntry) {
		Map<String, Object> dataMap = new HashMap<>(data);
		dataMap.putAll(swAuditEntry);
		return dataMap;
	}

	@Override
	public String toString() {
		return "<" + data.toString() + ", swIdent= " + swIdent + ">";
	}
}
