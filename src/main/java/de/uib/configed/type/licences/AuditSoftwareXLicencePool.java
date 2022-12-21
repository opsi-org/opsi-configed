package de.uib.configed.type.licences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;

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

	public ArrayList<String> registeredSoftware;

	public static final String SwID = "swId";

	public static final java.util.List<String> ATTRIBUTES;
	// public static final String[] ATTRIBUTES_asArray;
	static {
		ATTRIBUTES = new LinkedList<>();
		ATTRIBUTES.add(LicencepoolEntry.idSERVICEKEY);
		// ATTRIBUTES.add(SwID);

		ATTRIBUTES.add(SWAuditEntry.NAME);
		ATTRIBUTES.add(SWAuditEntry.VERSION);
		ATTRIBUTES.add(SWAuditEntry.SUBVERSION);
		ATTRIBUTES.add(SWAuditEntry.LANGUAGE);
		ATTRIBUTES.add(SWAuditEntry.ARCHITECTURE);

		// ATTRIBUTES_asArray = ATTRIBUTES.toArray(new String[0]);
	}

	public static final java.util.List<String> INTERFACED_ATTRIBUTES;
	static {
		INTERFACED_ATTRIBUTES = new LinkedList<>(ATTRIBUTES);
		INTERFACED_ATTRIBUTES.add(SwID);
	}

	public static final String[] SERVICE_ATTRIBUTES = new String[] { LicencepoolEntry.idSERVICEKEY, SWAuditEntry.NAME,
			SWAuditEntry.VERSION, SWAuditEntry.SUBVERSION, SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE };

	public AuditSoftwareXLicencePool(ArrayList<String> allRegisteredSoftware) {
		super(ATTRIBUTES);
		registeredSoftware = allRegisteredSoftware;
		// logging.info(this, "registeredSoftware");
		// for (String sw : registeredSoftware) logging.info(this, sw);

	}

	/*
	 * private String produceSWident(Map<String, String> m)
	 * {
	 * String result = Globals.pseudokey(new String[]{
	 * Globals.getStringValue( m.get(SWAuditEntry.NAME) ),
	 * Globals.getStringValue( m.get(SWAuditEntry.VERSION) ),
	 * Globals.getStringValue( m.get(SWAuditEntry.SUBVERSION) ) ,
	 * Globals.getStringValue( m.get(SWAuditEntry.LANGUAGE) ),
	 * Globals.getStringValue (m.get (SWAuditEntry.ARCHITECTURE) )
	 * }
	 * );
	 * 
	 * return result;
	 * }
	 */

	/*
	 * private Integer produceSWidRaw(Map<String, Object> m)
	 * {
	 * return produceSWid(new RelationElement(m));
	 * }
	 * 
	 * private Integer produceSWid(Map<String, String> m)
	 * {
	 * String swIdent = produceSWident(m);
	 * 
	 * int result = -1;
	 * 
	 * boolean newEntry = false;
	 * 
	 * 
	 * logging.info(this, "produceSWid for " + swIdent);
	 * 
	 * int swId = registeredSoftware.indexOf(swIdent);
	 * 
	 * 
	 * if (swId > -1)
	 * result = swId;
	 * else
	 * {
	 * //logging.error("no software entry for " + swIdent);
	 * registeredSoftware.add(swIdent);
	 * result = registeredSoftware.size();
	 * newEntry = true;
	 * }
	 * 
	 * String info = "";
	 * if (m.get(LicencepoolEntry.idSERVICEKEY) != null)
	 * info = " " + m.get(LicencepoolEntry.idSERVICEKEY) + " ";
	 * 
	 * //logging.info(this, "registeredSoftware " + registeredSoftware);
	 * logging.info(this, "swident  " + swIdent + " for " + info + "  ==== id (new "
	 * + newEntry +") " + result);
	 * 
	 * return result;
	 * }
	 */

	private String produceSWident(Map<String, Object> m) {
		return Globals.pseudokey(new String[] { Globals.getStringValue(m.get(SWAuditEntry.NAME)),
				Globals.getStringValue(m.get(SWAuditEntry.VERSION)),
				Globals.getStringValue(m.get(SWAuditEntry.SUBVERSION)),
				Globals.getStringValue(m.get(SWAuditEntry.LANGUAGE)),
				Globals.getStringValue(m.get(SWAuditEntry.ARCHITECTURE)) });
	}

	public static Map<String, String> produceMapFromSWident(String ident) {
		Map<String, String> m = new HashMap<>();
		if (ident == null) {
			logging.warning("produceMapFromSWident, ident null ");
			return null;
		}

		String[] parts = ident.split(";", -1); // give zero length parts as ""
		if (parts.length < 5)
			logging.warning("produceMapFromSWident, ident can not be splitted. " + ident);
		m.put(SWAuditEntry.NAME, parts[0]);
		m.put(SWAuditEntry.VERSION, parts[1]);
		m.put(SWAuditEntry.SUBVERSION, parts[2]);
		m.put(SWAuditEntry.LANGUAGE, parts[3]);
		m.put(SWAuditEntry.ARCHITECTURE, parts[4]);

		return m;
	}

	@Override
	public StringValuedRelationElement integrateRaw(Map<String, Object> m) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		String swIdent = "" + produceSWident(m);
		rowmap.put(SwID, swIdent);

		/*
		 * if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)
		 * logging.info(this, "integrateRaw " + m);
		 */

		rowmap.put(LicencepoolEntry.idSERVICEKEY, Globals.getStringValue(m.get(LicencepoolEntry.idSERVICEKEY)));
		add(rowmap);

		// logging.info(this, " StringValuedRelationElement " + rowmap);

		return rowmap;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			String sToSplit = "firefox;25.0-3.fc20;;;x64";
			logging.debug(" no argument given, taken " + sToSplit);
			logging.debug("getting map " + produceMapFromSWident(sToSplit));
		} else
			logging.debug("getting map " + produceMapFromSWident(args[0]));
	}

}
