package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.configed.Configed;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.datastructure.TableEntry;

public class SWAuditEntry extends TableEntry
/*
 * type of auditSoftware_getObjects resp
 * SOFTWARE in opsi data base
 * 
 * | Field | Type | Null | Key | Default
 * | installSize | bigint(20) | YES | | NULL
 * | name | varchar(100) | NO | PRI | NULL
 * | version | varchar(100) | NO | PRI | NULL
 * | subVersion | varchar(100) | NO | PRI | NULL
 * | language | varchar(10) | NO | PRI | NULL
 * | architecture | varchar(3) | NO | PRI | NULL
 * | windowsSoftwareId | varchar(100) | NO | MUL | NULL
 * | windowsDisplayName | varchar(100) | NO | | NULL
 * | windowsDisplayVersion | varchar(100) | NO | | NULL
 * | type | varchar(30) | NO | MUL | NULL
 */

{
	public static final String id = "ID";
	public static final String NAME = "name";
	public static final String VERSION = "version";
	public static final String SUBVERSION = "subVersion";
	public static final String ARCHITECTURE = "architecture";
	public static final String LANGUAGE = "language";
	public static final String WINDOWSsOFTWAREid = "windowsSoftwareID";

	public static final String EXISTING_IDS = "(variants)";

	private static List<String> KEYS;
	static {
		KEYS = new LinkedList<>();
		KEYS.add(id);
		KEYS.add(NAME);
		KEYS.add(VERSION);
		KEYS.add("subversion");
		KEYS.add(ARCHITECTURE);
		KEYS.add(LANGUAGE);

		KEYS.add(WINDOWSsOFTWAREid);
	}

	private static List<String> KEYS_FOR_GUI_TABLES;
	static {
		KEYS_FOR_GUI_TABLES = new LinkedList<>();
		KEYS_FOR_GUI_TABLES.add(id);
		KEYS_FOR_GUI_TABLES.add(NAME);
		KEYS_FOR_GUI_TABLES.add(VERSION);
		KEYS_FOR_GUI_TABLES.add(SUBVERSION);
		KEYS_FOR_GUI_TABLES.add(ARCHITECTURE);
		KEYS_FOR_GUI_TABLES.add(LANGUAGE);

		KEYS_FOR_GUI_TABLES.add(WINDOWSsOFTWAREid);
	}

	public static List<String> KEYS_FOR_IDENT;
	static {
		KEYS_FOR_IDENT = new ArrayList<>();
		KEYS_FOR_IDENT.add(NAME);
		KEYS_FOR_IDENT.add(VERSION);
		KEYS_FOR_IDENT.add("subversion");
		KEYS_FOR_IDENT.add(LANGUAGE);
		KEYS_FOR_IDENT.add(ARCHITECTURE);
	}

	public static List<String> ID_VARIANTS_COLS;
	static {
		ID_VARIANTS_COLS = new ArrayList<>();
		ID_VARIANTS_COLS.add(NAME);
		ID_VARIANTS_COLS.add(EXISTING_IDS);

	}

	public static final Map<String, String> key2serverKey;
	static {
		key2serverKey = new HashMap<>();
		key2serverKey.put(NAME, "name");
		key2serverKey.put(VERSION, "version");
		key2serverKey.put(SUBVERSION, "subVersion");
		key2serverKey.put(ARCHITECTURE, "architecture");
		key2serverKey.put(LANGUAGE, "language");

		key2serverKey.put(WINDOWSsOFTWAREid, "windowsSoftwareId");
	}

	protected static Map<String, String> locale = new StringIdentityMap(KEYS);

	public static void setLocale() {

		locale.put(id, "ID");
		locale.put(NAME, Configed.getResourceValue("PanelSWInfo.tableheader_displayName"));
		locale.put(VERSION, Configed.getResourceValue("PanelSWInfo.tableheader_displayVersion"));

		locale.put(ARCHITECTURE, Configed.getResourceValue("PanelSWInfo.tableheader_architecture"));
		locale.put(LANGUAGE, Configed.getResourceValue("PanelSWInfo.tableheader_displayLanguage"));

		locale.put(WINDOWSsOFTWAREid, Configed.getResourceValue("PanelSWInfo.tableheader_softwareId"));
	}

	public static List<String> getDisplayKeys() {
		return KEYS_FOR_GUI_TABLES;
	}

	private String lastseen = "";
	private String ident;
	private String identReduced;

	public SWAuditEntry(StringValuedRelationElement auditSoftwareXlicencePool_element,
			AuditSoftwareXLicencePool relation)
	// called for RelationElements of AuditSoftwareXLicencePool
	// the parameter is only requested in order to get a distinction of the
	// constructors
	{
		super(auditSoftwareXlicencePool_element);
		ident = auditSoftwareXlicencePool_element.get(AuditSoftwareXLicencePool.SwID);
	}

	public SWAuditEntry(Map<String, Object> entry) {
		super(entry);

		remap(NAME, key2serverKey.get(NAME));
		remap(VERSION, key2serverKey.get(VERSION));

		remap(ARCHITECTURE, key2serverKey.get(ARCHITECTURE));
		remap(LANGUAGE, key2serverKey.get(LANGUAGE));

		remap(WINDOWSsOFTWAREid, "windowsSoftwareId");

		String subversion // not included in key-values
				= entryRetrieved.get(key2serverKey.get(SUBVERSION));

		if (subversion == null)
			subversion = "";
		put(key2serverKey.get(SUBVERSION), subversion);

		ident = Globals.pseudokey(new String[] { get(NAME), // KEYS_FOR_IDENT.get(0) ...
				get(VERSION), subversion, get(LANGUAGE), get(ARCHITECTURE) });

		identReduced = Globals.pseudokey(new String[] { get(VERSION), get(ARCHITECTURE) });

		if (entry.get("lastseen") != null)
			lastseen = entry.get("lastseen").toString();

		put(id, ident);

	}

	public static String getDisplayKey(int i) {
		return locale.get(KEYS.get(i));
	}

	public String[] getData() {
		String[] data = new String[KEYS.size()];

		for (int i = 0; i < KEYS.size(); i++) {
			data[i] = get(KEYS.get(i));
		}

		return data;
	}

	public String getLastseen() {
		return lastseen;
	}

	public String getIdent() {
		return ident;
	}

	public String getIdentReduced() {
		return identReduced;
	}

}
