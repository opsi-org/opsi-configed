/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.utilities.datastructure.AbstractTableEntry;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import utils.Utils;

public class SWAuditEntry extends AbstractTableEntry {
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

	public static final String ID = "ID";
	public static final String NAME = "name";
	public static final String VERSION = "version";
	public static final String SUB_VERSION = "subVersion";
	public static final String ARCHITECTURE = "architecture";
	public static final String LANGUAGE = "language";
	public static final String WINDOWS_SOFTWARE_ID = "windowsSoftwareID";

	public static final String EXISTING_IDS = "(variants)";

	private static final List<String> KEYS = new LinkedList<>();
	static {
		KEYS.add(ID);
		KEYS.add(NAME);
		KEYS.add(VERSION);
		KEYS.add("subversion");
		KEYS.add(ARCHITECTURE);
		KEYS.add(LANGUAGE);

		KEYS.add(WINDOWS_SOFTWARE_ID);
	}

	private static final List<String> KEYS_FOR_GUI_TABLES = new LinkedList<>();
	static {
		KEYS_FOR_GUI_TABLES.add(ID);
		KEYS_FOR_GUI_TABLES.add(NAME);
		KEYS_FOR_GUI_TABLES.add(VERSION);
		KEYS_FOR_GUI_TABLES.add(SUB_VERSION);
		KEYS_FOR_GUI_TABLES.add(ARCHITECTURE);
		KEYS_FOR_GUI_TABLES.add(LANGUAGE);

		KEYS_FOR_GUI_TABLES.add(WINDOWS_SOFTWARE_ID);
	}

	public static final List<String> KEYS_FOR_IDENT = new ArrayList<>();
	static {
		KEYS_FOR_IDENT.add(NAME);
		KEYS_FOR_IDENT.add(VERSION);
		KEYS_FOR_IDENT.add("subversion");
		KEYS_FOR_IDENT.add(LANGUAGE);
		KEYS_FOR_IDENT.add(ARCHITECTURE);
	}

	public static final List<String> ID_VARIANTS_COLS = new ArrayList<>();
	static {
		ID_VARIANTS_COLS.add(NAME);
		ID_VARIANTS_COLS.add(EXISTING_IDS);

	}

	public static final Map<String, String> key2serverKey;
	static {
		key2serverKey = new HashMap<>();
		key2serverKey.put(NAME, "name");
		key2serverKey.put(VERSION, "version");
		key2serverKey.put(SUB_VERSION, "subVersion");
		key2serverKey.put(ARCHITECTURE, "architecture");
		key2serverKey.put(LANGUAGE, "language");

		key2serverKey.put(WINDOWS_SOFTWARE_ID, "windowsSoftwareId");
	}

	private String ident;
	private String identReduced;

	public SWAuditEntry(StringValuedRelationElement auditSoftwareXLicencePoolElement) {
		// called for RelationElements of AuditSoftwareXLicencePool
		// the parameter is only requested in order to get a distinction of the
		// constructors

		super(auditSoftwareXLicencePoolElement);
		ident = auditSoftwareXLicencePoolElement.get(AuditSoftwareXLicencePool.SW_ID);
	}

	public SWAuditEntry(Map<String, Object> entry) {
		super(entry);

		super.remap(NAME, key2serverKey.get(NAME));
		super.remap(VERSION, key2serverKey.get(VERSION));

		super.remap(ARCHITECTURE, key2serverKey.get(ARCHITECTURE));
		super.remap(LANGUAGE, key2serverKey.get(LANGUAGE));

		super.remap(WINDOWS_SOFTWARE_ID, "windowsSoftwareId");

		// not included in key-values
		String subversion = entryRetrieved.get(key2serverKey.get(SUB_VERSION));

		if (subversion == null) {
			subversion = "";
		}

		super.put(key2serverKey.get(SUB_VERSION), subversion);

		ident = Utils.pseudokey(new String[] { super.get(NAME), super.get(VERSION), subversion, super.get(LANGUAGE),
				super.get(ARCHITECTURE) });
		identReduced = Utils.pseudokey(new String[] { super.get(VERSION), super.get(ARCHITECTURE) });

		super.put(ID, ident);
	}

	public static List<String> getDisplayKeys() {
		return KEYS_FOR_GUI_TABLES;
	}

	public String getIdent() {
		return ident;
	}

	public String getIdentReduced() {
		return identReduced;
	}
}
