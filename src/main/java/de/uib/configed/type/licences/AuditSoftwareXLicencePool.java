package de.uib.configed.type.licences;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;

public class AuditSoftwareXLicencePool extends Relation {
	/*
	 * AUDIT_SOFTWARE_TO_LICENSE_POOL in database
	 * 
	 * | Field | Type | Null | Key
	 * | licensePoolId | varchar(100) | NO | MUL | NULL
	 * | name | varchar(100) | NO | PRI | NULL
	 * | version | varchar(100) | NO | PRI | NULL
	 * | subVersion | varchar(100) | NO | PRI | NULL
	 * | language | varchar(10) | NO | PRI | NULL
	 * | architecture | varchar(3) | NO | PRI | NULL
	 * 
	 */

	public static final String SW_ID = "swId";

	public static final List<String> SOFTWARE_ATTRIBUTES;

	static {
		SOFTWARE_ATTRIBUTES = new LinkedList<>();
		SOFTWARE_ATTRIBUTES.add(LicencepoolEntry.ID_SERVICE_KEY);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.NAME);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.VERSION);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.SUB_VERSION);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.LANGUAGE);
		SOFTWARE_ATTRIBUTES.add(SWAuditEntry.ARCHITECTURE);

	}

	public static final List<String> INTERFACED_ATTRIBUTES;
	static {
		INTERFACED_ATTRIBUTES = new LinkedList<>(SOFTWARE_ATTRIBUTES);
		INTERFACED_ATTRIBUTES.add(SW_ID);
	}

	public static final String[] SERVICE_ATTRIBUTES = new String[] { LicencepoolEntry.ID_SERVICE_KEY, SWAuditEntry.NAME,
			SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION, SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE };

	public AuditSoftwareXLicencePool() {
		super(SOFTWARE_ATTRIBUTES);
	}

	private String produceSWident(Map<String, Object> m) {
		return Globals.pseudokey(new String[] { Globals.getStringValue(m.get(SWAuditEntry.NAME)),
				Globals.getStringValue(m.get(SWAuditEntry.VERSION)),
				Globals.getStringValue(m.get(SWAuditEntry.SUB_VERSION)),
				Globals.getStringValue(m.get(SWAuditEntry.LANGUAGE)),
				Globals.getStringValue(m.get(SWAuditEntry.ARCHITECTURE)) });
	}

	public static Map<String, String> produceMapFromSWident(String ident) {
		Map<String, String> m = new HashMap<>();
		if (ident == null) {
			Logging.warning("produceMapFromSWident, ident null ");
			return new HashMap<>();
		}

		// give zero length parts as ""
		String[] parts = ident.split(";", -1);
		if (parts.length < 5) {
			Logging.warning("produceMapFromSWident, ident can not be splitted. " + ident);
		}

		m.put(SWAuditEntry.NAME, parts[0]);
		m.put(SWAuditEntry.VERSION, parts[1]);
		m.put(SWAuditEntry.SUB_VERSION, parts[2]);
		m.put(SWAuditEntry.LANGUAGE, parts[3]);
		m.put(SWAuditEntry.ARCHITECTURE, parts[4]);

		return m;
	}

	@Override
	public StringValuedRelationElement integrateRaw(Map<String, Object> m) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		String swIdent = "" + produceSWident(m);
		rowmap.put(SW_ID, swIdent);

		rowmap.put(LicencepoolEntry.ID_SERVICE_KEY, Globals.getStringValue(m.get(LicencepoolEntry.ID_SERVICE_KEY)));
		add(rowmap);

		return rowmap;
	}

}
