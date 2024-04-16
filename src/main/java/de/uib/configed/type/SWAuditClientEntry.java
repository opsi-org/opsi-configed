/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class SWAuditClientEntry {
	/*
	 * type of auditSoftwareOnClient_getObjects resp
	 * SOFTWARE_CONFIG in opsi data base
	 * 
	 * | Field | Type | Null | Key | Default
	 * | config_id | int(11) | NO | PRI | NULL
	 * | clientId | varchar(255) | NO | MUL | NULL
	 * | firstseen | timestamp | NO | | 0000-00-00 00:00:00
	 * | lastseen | timestamp | NO | | 0000-00-00 00:00:00
	 * | state | tinyint(4) | NO | | NULL
	 * | usageFrequency | int(11) | NO | | -1
	 * | lastUsed | timestamp | NO | | 0000-00-00 00:00:00
	 * | name | varchar(100) | NO | MUL | NULL
	 * | version | varchar(100) | NO | | NULL
	 * | subVersion | varchar(100) | NO | | NULL
	 * | language | varchar(10) | NO | | NULL
	 * | architecture | varchar(3) | NO | | NULL
	 * | uninstallString | varchar(200) | YES | | NULL
	 * | binaryName | varchar(100) | YES | | NULL
	 * | licenseKey | varchar(100) | YES | | NULL
	 * 
	 */

	public static final String CLIENT_ID = "clientId";
	public static final String LICENSE_KEY = "licenseKey";
	public static final String LAST_MODIFICATION = "lastseen";
	private static final long MS_AFTER_THIS_ALLOW_NEXT_UPDATE = 60000;
	public static final List<String> KEYS = List.of(SWAuditEntry.NAME, SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION,
			SWAuditEntry.ARCHITECTURE, SWAuditEntry.LANGUAGE, LICENSE_KEY, SWAuditEntry.WINDOWS_SOFTWARE_ID);

	public static final String DB_TABLE_NAME = "SOFTWARE_CONFIG";

	private Set<String> notFoundSoftwareIDs;
	private Long lastUpdateTime;

	private Integer swId;
	private String swIdent;
	private String lastModificationS;

	private final Map<String, String> data;
	private List<String> software;
	private NavigableMap<String, Integer> software2Number;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SWAuditClientEntry(final Map<String, Object> m) {
		data = new HashMap<>();
		data.put(SWAuditEntry.ID, produceNonNull(m.get(CLIENT_ID)));
		swIdent = produceSWident(m);
		this.software = persistenceController.getSoftwareDataService().getSoftwareListPD();
		this.software2Number = persistenceController.getSoftwareDataService().getSoftware2NumberPD();
		produceSWid();
		data.put(LICENSE_KEY, produceNonNull(m.get(LICENSE_KEY)));
		lastModificationS = produceNonNull(m.get(LAST_MODIFICATION));
	}

	private static String produceNonNull(Object o) {
		return o != null ? o.toString() : "";
	}

	private void updateSoftware() {
		Logging.info(this, "updateSoftware");
		if (lastUpdateTime != null && System.currentTimeMillis() - lastUpdateTime > MS_AFTER_THIS_ALLOW_NEXT_UPDATE) {
			persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
			software = persistenceController.getSoftwareDataService().getSoftwareListPD();
			lastUpdateTime = System.currentTimeMillis();
			notFoundSoftwareIDs = new HashSet<>();
		} else {
			Logging.warning(this, "updateSoftware: doing nothing since we just updated");
		}
	}

	private Integer getIndex(List<String> list, String element) {
		int result = -1;

		if (!swIdent.equals(element)) {
			Logging.warning(this,
					"getIndex gobal swIdent was assumed to be equal to element " + swIdent + ". " + element);
		}

		Integer j = software2Number.get(element);

		if (j == null) {
			Logging.info(this,
					"getIndex, probably because of an upper-lower case or a null issue, not found for  " + element);
		} else {
			result = j;
		}

		if (result == -1 && list != null) {
			int i = 0;
			while (result == -1 && i < list.size()) {
				if (list.get(i).equalsIgnoreCase(element)) {
					result = i;
					Logging.warning(this, "indexOfIgnoreCase found equality of " + element + " to entry \n" + i + " : "
							+ list.get(i));
				}
				i++;
			}
			if (result == -1) {
				Logging.warning(this, "tried indexOfIgnoreCase in vain for " + element);
			}
		}

		return result;
	}

	private Integer produceSWid() {
		swId = getIndex(software, swIdent);
		Logging.debug(this, "search index for software with ident " + swIdent + " \nswId " + swId);

		if (swId == -1) {
			Logging.info(this, "software with ident " + swIdent + " not yet indexed");
			if (notFoundSoftwareIDs != null && !notFoundSoftwareIDs.contains(swIdent)) {
				updateSoftware();
				swId = getIndex(software, swIdent);
			}

			if (swId == -1) {
				Logging.warning(this, "swIdent not found in softwarelist: " + swIdent);
				if (notFoundSoftwareIDs == null) {
					notFoundSoftwareIDs = new HashSet<>();
				}
				notFoundSoftwareIDs.add(swIdent);
			}
		}

		return swId;
	}

	public static String produceSWident(Map<String, Object> readMap) {
		return Utils.pseudokey(new String[] { (String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.SUB_VERSION)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE)) });
	}

	public String getClientId() {
		return data.get(SWAuditEntry.ID);
	}

	public String getLastModification() {
		return lastModificationS;
	}

	public Integer getSWid() {
		return swId;
	}

	public String getSWident() {
		return swIdent;
	}

	public Map<String, Object> getExpandedMap(Map<String, SWAuditEntry> installedSoftwareInformation, String swIdent) {
		Map<String, Object> dataMap = new HashMap<>(data);
		dataMap.putAll(installedSoftwareInformation.get(swIdent));

		return dataMap;
	}

	@Override
	public String toString() {
		return "<" + data.toString() + ", swIdent= " + swIdent + ">";
	}
}
