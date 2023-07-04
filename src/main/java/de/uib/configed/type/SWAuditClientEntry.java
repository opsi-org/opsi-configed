/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import de.uib.configed.Globals;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

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
	public static final String LICENCE_KEY = "licenseKey";
	public static final String LAST_MODIFICATION = "lastseen";

	private static List<String> notFoundSoftwareIDs;
	private static Long lastUpdateTime;
	private static final long MS_AFTER_THIS_ALLOW_NEXT_UPDATE = 60000;

	public static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(SWAuditEntry.ID);
		KEYS.add(SWAuditEntry.NAME);
		KEYS.add(SWAuditEntry.VERSION);
		KEYS.add(SWAuditEntry.SUB_VERSION);
		KEYS.add(SWAuditEntry.ARCHITECTURE);
		KEYS.add(SWAuditEntry.LANGUAGE);
		KEYS.add(LICENCE_KEY);
		KEYS.add(SWAuditEntry.WINDOWS_SOFTWARE_ID);
	}

	private static final List<String> KEYS_FOR_GUI_TABLES = new LinkedList<>();
	static {
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.ID);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.NAME);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.VERSION);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.ARCHITECTURE);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.LANGUAGE);
		KEYS_FOR_GUI_TABLES.add(LICENCE_KEY);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.WINDOWS_SOFTWARE_ID);
	}

	private static Map<String, String> locale = new StringIdentityMap(KEYS);

	public static final String DB_TABLE_NAME = "SOFTWARE_CONFIG";

	public static final Map<String, String> DB_COLUMNS = new LinkedHashMap<>();
	static {
		DB_COLUMNS.put(CLIENT_ID, DB_TABLE_NAME + "." + "clientId");
		DB_COLUMNS.put(SWAuditEntry.NAME, DB_TABLE_NAME + "." + "name");
		DB_COLUMNS.put(SWAuditEntry.VERSION, DB_TABLE_NAME + "." + "version");
		DB_COLUMNS.put(SWAuditEntry.SUB_VERSION, DB_TABLE_NAME + "." + "subVersion");
		DB_COLUMNS.put(SWAuditEntry.ARCHITECTURE, DB_TABLE_NAME + "." + "architecture");
		DB_COLUMNS.put(SWAuditEntry.LANGUAGE, DB_TABLE_NAME + "." + "language");
		DB_COLUMNS.put(LICENCE_KEY, DB_TABLE_NAME + "." + "licenseKey");
		DB_COLUMNS.put(LAST_MODIFICATION, DB_TABLE_NAME + "." + "lastseen");
	}

	public static final List<String> DB_COLUMN_NAMES = new ArrayList<>();
	static {
		for (String value : DB_COLUMNS.values()) {
			DB_COLUMN_NAMES.add(value);
		}
	}

	private Integer swId;
	private String swIdent;
	private String lastModificationS;

	private final Map<String, String> data;
	private List<String> software;
	private NavigableMap<String, Integer> software2Number;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SWAuditClientEntry(final Map<String, Object> m) {

		data = new HashMap<>();
		data.put(SWAuditEntry.ID, Globals.produceNonNull(m.get(CLIENT_ID)));
		swIdent = produceSWident(m);
		this.software = persistenceController.getSoftwareList();
		this.software2Number = persistenceController.getSoftware2Number();
		produceSWid();
		data.put(LICENCE_KEY, Globals.produceNonNull(m.get(LICENCE_KEY)));
		lastModificationS = Globals.produceNonNull(m.get(LAST_MODIFICATION));

	}

	public static String produceSWident(List<String> keys, List<String> values) {
		// from db columns

		return Globals.pseudokey(new String[] { values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.NAME))),
				values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.VERSION))),
				values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.SUB_VERSION))),
				values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.LANGUAGE))),
				values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE))), });
	}

	private void updateSoftware() {
		Logging.info(this, "updateSoftware");
		if (lastUpdateTime != null && System.currentTimeMillis() - lastUpdateTime > MS_AFTER_THIS_ALLOW_NEXT_UPDATE) {
			persistenceController.installedSoftwareInformationRequestRefresh();
			software = persistenceController.getSoftwareList();
			lastUpdateTime = System.currentTimeMillis();
			notFoundSoftwareIDs = new ArrayList<>();
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
					notFoundSoftwareIDs = new ArrayList<>();
				}
				notFoundSoftwareIDs.add(swIdent);
			}
		}

		return swId;
	}

	public static String produceSWident(Map<String, Object> readMap) {
		return Globals.pseudokey(new String[] { (String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME)),
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

	public static List<String> getDisplayKeys() {
		return KEYS_FOR_GUI_TABLES;
	}

	public static String getDisplayKey(int i) {
		return locale.get(KEYS.get(i));
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
