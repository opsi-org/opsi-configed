/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.type.licenses.AuditSoftwareXLicensePool;
import de.uib.utils.Utils;
import de.uib.utils.datastructure.AbstractTableEntry;
import de.uib.utils.datastructure.StringValuedRelationElement;

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

	public static final List<String> KEYS_FOR_IDENT = List.of(NAME, VERSION, "subversion", LANGUAGE, ARCHITECTURE);
	public static final List<String> ID_VARIANTS_COLS = List.of(NAME, EXISTING_IDS);

	private String ident;
	private String identReduced;

	public SWAuditEntry(StringValuedRelationElement auditSoftwareXLicensePoolElement) {
		// called for RelationElements of AuditSoftwareXLicensePool
		// the parameter is only requested in order to get a distinction of the
		// constructors

		super(auditSoftwareXLicensePoolElement);
		ident = auditSoftwareXLicensePoolElement.get(AuditSoftwareXLicensePool.SW_ID);
	}

	public SWAuditEntry(Map<String, Object> entry) {
		super(entry);

		super.remap(NAME, NAME);
		super.remap(VERSION, VERSION);

		super.remap(ARCHITECTURE, ARCHITECTURE);
		super.remap(LANGUAGE, LANGUAGE);

		super.remap(WINDOWS_SOFTWARE_ID, "windowsSoftwareId");

		// not included in key-values
		String subversion = entryRetrieved.get(SUB_VERSION);

		if (subversion == null) {
			subversion = "";
		}

		super.put(SUB_VERSION, subversion);

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
