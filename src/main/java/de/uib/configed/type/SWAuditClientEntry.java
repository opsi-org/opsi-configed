package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class SWAuditClientEntry
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

{
	public static final String CLIENT_ID = "clientId";
	public static final String LICENCEkEY = "licenseKey";
	public static final String LAST_MODIFICATION = "lastseen";
	public static final String UNINSTALL_STRING = "uninstallString";

	protected final Map<String, String> data;
	protected ArrayList<String> software;
	protected java.util.TreeMap<String, Integer> software2Number;
	private static List<String> notFoundSoftwareIDs;
	private static Long lastUpdateTime;
	private static final long msAfterThisAllowNextUpdate = 60000;
	protected Integer swId;
	protected String swIdent;
	protected String lastModificationS;

	static long startmillis1stPartOfConstructor;
	static long startmillis2ndPartOfConstructor;
	static long endmillis1stPartOfConstructor;
	static long endmillis2ndPartOfConstructor;
	public static long summillis1stPartOfConstructor = 0;
	public static long summillis2ndPartOfConstructor = 0;

	de.uib.opsidatamodel.PersistenceController controller; // for retrieving softwarelist

	public static List<String> KEYS;
	static {
		KEYS = new LinkedList<>();
		KEYS.add(SWAuditEntry.id);
		KEYS.add(SWAuditEntry.NAME);
		KEYS.add(SWAuditEntry.VERSION);
		KEYS.add(SWAuditEntry.SUBVERSION);
		KEYS.add(SWAuditEntry.ARCHITECTURE);
		KEYS.add(SWAuditEntry.LANGUAGE);
		KEYS.add(LICENCEkEY);
		KEYS.add(SWAuditEntry.WINDOWSsOFTWAREid);
	}

	private static List<String> KEYS_FOR_GUI_TABLES;
	static {
		KEYS_FOR_GUI_TABLES = new LinkedList<>();
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.id);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.NAME);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.VERSION);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.ARCHITECTURE);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.LANGUAGE);
		KEYS_FOR_GUI_TABLES.add(LICENCEkEY);
		KEYS_FOR_GUI_TABLES.add(SWAuditEntry.WINDOWSsOFTWAREid);
	}

	protected static Map<String, String> locale = new StringIdentityMap(KEYS);

	public static void setLocale() {
		// locale = new HashMap<>();
		locale.put(SWAuditEntry.id, "ID");
		locale.put(SWAuditEntry.NAME, configed.getResourceValue("PanelSWInfo.tableheader_displayName"));
		locale.put(SWAuditEntry.VERSION, configed.getResourceValue("PanelSWInfo.tableheader_displayVersion"));
		// locale.put(subversion,
		// configed.getResourceValue("PanelSWInfo.tableheader_displaySubVersion"));
		locale.put(SWAuditEntry.ARCHITECTURE, configed.getResourceValue("PanelSWInfo.tableheader_architecture"));
		locale.put(SWAuditEntry.LANGUAGE, configed.getResourceValue("PanelSWInfo.tableheader_displayLanguage"));
		locale.put(LICENCEkEY, configed.getResourceValue("PanelSWInfo.tableheader_displayLicenseKey"));
		locale.put(SWAuditEntry.WINDOWSsOFTWAREid, configed.getResourceValue("PanelSWInfo.tableheader_softwareId"));
	}

	public static final String DB_TABLE_NAME = "SOFTWARE_CONFIG";

	public static final LinkedHashMap<String, String> DB_COLUMNS = new LinkedHashMap<>();
	static {
		DB_COLUMNS.put(CLIENT_ID, DB_TABLE_NAME + "." + "clientId");
		DB_COLUMNS.put(SWAuditEntry.NAME, DB_TABLE_NAME + "." + "name");
		DB_COLUMNS.put(SWAuditEntry.VERSION, DB_TABLE_NAME + "." + "version");
		DB_COLUMNS.put(SWAuditEntry.SUBVERSION, DB_TABLE_NAME + "." + "subVersion");
		DB_COLUMNS.put(SWAuditEntry.ARCHITECTURE, DB_TABLE_NAME + "." + "architecture");
		DB_COLUMNS.put(SWAuditEntry.LANGUAGE, DB_TABLE_NAME + "." + "language");
		DB_COLUMNS.put(LICENCEkEY, DB_TABLE_NAME + "." + "licenseKey");
		DB_COLUMNS.put(LAST_MODIFICATION, DB_TABLE_NAME + "." + "lastseen");
		// DB_COLUMNS.put(SWAuditEntry.WINDOWSsOFTWAREid);

	}

	/*
	 * public static final LinkedHashMap<String, String> DB_COLUMNS = new
	 * LinkedHashMap<String, String>();
	 * static {
	 * DB_COLUMNS.put("config_id", "INTEGER");
	 * //DB_COLUMNS.put("clientId", "VARCHAR(255)");
	 * DB_COLUMNS.put("name", "VARCHAR(100)");
	 * 
	 * DB_COLUMNS.put("version", "VARCHAR(100)");
	 * DB_COLUMNS.put("subVersion", "VARCHAR(100)");
	 * DB_COLUMNS.put("architecture", "VARCHAR(3)");
	 * DB_COLUMNS.put("language", "VARCHAR(10)");
	 * DB_COLUMNS.put("licenseKey", "VARCHAR(255)");
	 * 
	 * DB_COLUMNS.put("lastseen", "TIMESTAMP"); //lastStateChange");
	 * }
	 */

	public static final List<String> DB_COLUMN_NAMES = new ArrayList<>();
	static {
		for (String key : DB_COLUMNS.keySet()) {
			DB_COLUMN_NAMES.add(DB_COLUMNS.get(key));
		}
	}

	public static final int columnIndexLastStateChange = DB_COLUMN_NAMES.indexOf("modificationTime");

	public SWAuditClientEntry(final List<String> keys, final List<String> values,
			de.uib.opsidatamodel.PersistenceController controller) {
		

		startmillis1stPartOfConstructor = System.currentTimeMillis();

		data = new HashMap<>();
		/*
		 * for (int i = 0; i < keys.size(); i++)
		 * {
		 * data.put(keys.get(i), values.get(i));
		 * }
		 */
		data.put(SWAuditEntry.id, values.get(keys.indexOf(DB_COLUMNS.get(CLIENT_ID))));
		data.put(LICENCEkEY, values.get(keys.indexOf(DB_COLUMNS.get(LICENCEkEY))));
		// data.put(SWAuditEntry.SUBVERSION,
		// values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.SUBVERSION))));
		lastModificationS = values.get(keys.indexOf(DB_COLUMNS.get(LAST_MODIFICATION)));
		swIdent = produceSWident(keys, values);
		this.controller = controller;
		this.software = controller.getSoftwareList();
		this.software2Number = controller.getSoftware2Number();
		endmillis1stPartOfConstructor = System.currentTimeMillis();
		summillis1stPartOfConstructor = summillis1stPartOfConstructor
				+ (endmillis1stPartOfConstructor - startmillis1stPartOfConstructor);

		startmillis2ndPartOfConstructor = System.currentTimeMillis();
		produceSWid();
		endmillis2ndPartOfConstructor = System.currentTimeMillis();

		summillis2ndPartOfConstructor = summillis2ndPartOfConstructor
				+ (endmillis2ndPartOfConstructor - startmillis2ndPartOfConstructor);

		
	}

	public SWAuditClientEntry(final Map<String, Object> m, de.uib.opsidatamodel.PersistenceController controller) {
		
		data = new HashMap<>();
		data.put(SWAuditEntry.id, Globals.produceNonNull(m.get(CLIENT_ID)));
		swIdent = produceSWident(m);
		this.controller = controller;
		this.software = controller.getSoftwareList();
		this.software2Number = controller.getSoftware2Number();
		produceSWid();
		data.put(LICENCEkEY, Globals.produceNonNull(m.get(LICENCEkEY)));
		lastModificationS = Globals.produceNonNull(m.get(LAST_MODIFICATION));
		// uninstallString = Globals.produceNonNull(m.get(UNINSTALL_STRING));

	}

	public static String produceSWident(List<String> keys, List<String> values)
	// from db columns
	{
		
		// " -- " + values);
		String result = "";
		try {
			result = Globals.pseudokey(new String[] {
					// (values.get( keys.indexOf( DB_COLUMNS.get(SWAuditEntry.NAME) )
					// )).toLowerCase(),
					values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.NAME))),
					values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.VERSION))),
					values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.SUBVERSION))),
					values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.LANGUAGE))),
					values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE))), });
		} catch (Exception ex) {
			logging.info("SWAuditClientEntry:: produceSWident keys -- value : " + keys + " -- " + values);

			logging.info("SWAuditClientEntry:: produceSWident key " + DB_COLUMNS.get(SWAuditEntry.NAME));
			logging.info("SWAuditClientEntry:: produceSWident value "
					+ values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.NAME))));

			logging.info("SWAuditClientEntry:: produceSWident key " + DB_COLUMNS.get(SWAuditEntry.VERSION));
			logging.info("SWAuditClientEntry:: produceSWident value "
					+ values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.VERSION))));

			logging.info("SWAuditClientEntry:: produceSWident key " + DB_COLUMNS.get(SWAuditEntry.SUBVERSION));
			logging.info("SWAuditClientEntry:: produceSWident value "
					+ values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.SUBVERSION))));

			logging.info("SWAuditClientEntry:: produceSWident key " + DB_COLUMNS.get(SWAuditEntry.LANGUAGE));
			logging.info("SWAuditClientEntry:: produceSWident value "
					+ values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.LANGUAGE))));

			logging.info("SWAuditClientEntry:: produceSWident key " + DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE));
			logging.info("SWAuditClientEntry:: produceSWident value "
					+ values.get(keys.indexOf(DB_COLUMNS.get(SWAuditEntry.ARCHITECTURE))));

		}

		// return result.toLowerCase();
		return result;
	}

	protected void updateSoftware() {
		logging.info(this, "updateSoftware");
		if (lastUpdateTime != null && (System.currentTimeMillis() - lastUpdateTime > msAfterThisAllowNextUpdate)) {
			controller.installedSoftwareInformationRequestRefresh();
			software = controller.getSoftwareList();
			lastUpdateTime = System.currentTimeMillis();
			notFoundSoftwareIDs = new ArrayList<>();
		} else
			logging.warning(this, "updateSoftware: doing nothing since we just updated");
	}

	private Integer getIndex(List<String> list, String element) {
		/*
		 * int result = -1;
		 * if (list == null || element == null)
		 * return result;
		 * 
		 * int i = 0;
		 * while (result == -1 && i < list.size())
		 * {
		 * if (list.get(i).equalsIgnoreCase( element ) )
		 * {
		 * result = i;
		 * logging.info(this, "indexOfIgnoreCase found equality of " + element +
		 * " to entry \n" + i + " : " + list.get(i));
		 * }
		 * i++;
		 * }
		 * 
		 * return result;
		 */

		int result = -1;

		if (!swIdent.equals(element))
			logging.warning(this,
					"getIndex gobal swIdent was assumed to be equal to element " + swIdent + ". " + element);
		// int result = software.indexOf( swIdent );
		Integer j = software2Number.get(element);
		
		// "getIndex, for swIdent " + swIdent + " differs indexOf " + result + " from
		// mapped value " + j);

		if (j == null) {
			logging.info(this,
					"getIndex, probably because of an upper-lower case or a null issue, not found for  " + element);
		} else
			result = j;

		if (result == -1 && list != null) {
			
			// for " + swIdent );
			int i = 0;
			while (result == -1 && i < list.size()) {
				if (list.get(i).equalsIgnoreCase(element)) {
					result = i;
					logging.warning(this, "indexOfIgnoreCase found equality of " + element + " to entry \n" + i + " : "
							+ list.get(i));
				}
				i++;
			}
			if (result == -1) {
				logging.warning(this, "tried indexOfIgnoreCase in vain for " + element);
			}
		}

		return result;

	}

	protected Integer produceSWid() {
		swId = getIndex(software, swIdent);
		logging.debug(this, "search index for software with ident " + swIdent + " \nswId " + swId);

		if (swId == -1) {
			logging.info(this, "software with ident " + swIdent + " not yet indexed");
			if (notFoundSoftwareIDs != null && !notFoundSoftwareIDs.contains(swIdent)) {
				updateSoftware();
				swId = getIndex(software, swIdent);
			}

			
			if (swId == -1) {
				logging.warning(this, "swIdent not found in softwarelist: " + swIdent);
				if (notFoundSoftwareIDs == null)
					notFoundSoftwareIDs = new ArrayList<>();
				notFoundSoftwareIDs.add(swIdent);
			}
		}

		return swId;
	}

	public static String produceSWident(Map<String, Object> readMap) {
		String result = Globals.pseudokey(new String[] {
				// ((String)
				// readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME))).toLowerCase(),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.SUBVERSION)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE)),
				(String) readMap.get(SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE)) });

		// return result.toLowerCase();
		return result;
	}

	public String getClientId() {
		return data.get(SWAuditEntry.id);
	}

	public String getLicenceKey() {
		return data.get(data.get(LICENCEkEY));
	}

	public String getLastModification() {
		return lastModificationS;
		// Globals.produceNonNull(readMap.get(LAST_MODIFICATION));
	}

	public Integer getSWid() {
		return swId;
	}

	public String getSWident() {
		return swIdent;
	}

	/*
	 * public String[] getData()
	 * {
	 * logging.info(this, "getData " + data);
	 * String[] values = new String[KEYS.size()];
	 * 
	 * for (int i = 0; i < KEYS.size(); i++)
	 * {
	 * values[i] = data.get(KEYS.get(i));
	 * }
	 * 
	 * return values;
	 * }
	 */

	/*
	 * public String getUninstallString()
	 * {
	 * return uninstallString;
	 * }
	 */

	public static List<String> getDisplayKeys() {
		return KEYS_FOR_GUI_TABLES;
	}

	public static String getDisplayKey(int i) {
		return locale.get(KEYS.get(i));
	}

	public Map<String, String> getExpandedMap(Map<String, SWAuditEntry> installedSoftwareInformation, String swIdent) {
		Map<String, String> dataMap = new HashMap<>(data);
		dataMap.putAll(installedSoftwareInformation.get(swIdent));
		
		return dataMap;
	}

	public String[] getExpandedData(Map<String, SWAuditEntry> installedSoftwareInformation, String swIdent) {
		Map<String, String> dataMap = new HashMap<>(data);
		dataMap.putAll(installedSoftwareInformation.get(swIdent));

		String[] result = new String[KEYS.size()];

		for (int i = 0; i < KEYS.size(); i++) {
			result[i] = dataMap.get(KEYS.get(i));
		}

		return result;
	}

	@Override
	public String toString() {
		return "<" + data.toString() + ", swIdent= " + swIdent + ">";
	}

}
