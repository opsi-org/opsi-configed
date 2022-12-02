package de.uib.configed.type.licences;

import java.util.LinkedList;
import java.util.Map;

import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;

public class Table_LicenceContracts extends Relation {

	/*
	 * describe LICENSE_CONTRACT ;
	 * | Field | Type | Null | Key | Default | Extra
	 * | licenseContractId | varchar(100) | NO | PRI | NULL |
	 * | partner | varchar(100) | YES | | NULL |
	 * | conclusionDate | timestamp | NO | | 0000-00-00 00:00:00 |
	 * | notificationDate | timestamp | NO | | 0000-00-00 00:00:00 |
	 * | expirationDate | timestamp | NO | | 0000-00-00 00:00:00 |
	 * | notes | varchar(1000) | YES | | NULL |
	 * | type | varchar(30) | NO | MUL | NULL |
	 * | description | varchar(100) | NO | | NULL | |
	 * 
	 */

	public static final String idKEY = "id";
	public static final String identKEY = "ident";
	public static final String idDBKEY = "licenseContractId";
	public static final String partnerKEY = "partner";
	public static final String conclusionDateKEY = "conclusionDate";
	public static final String notificationDateKEY = "notificationDate";
	public static final String expirationDateKEY = "expirationDate";
	public static final String notesKEY = "notes";
	public static final String descriptionKEY = "description";

	public static final String opsiNOMtype = "LicenseContract";
	public static final String typeKEY = "type";

	public static final java.util.List<String> DB_ATTRIBUTES;
	// public static final String[] ATTRIBUTES_asArray;
	static {
		DB_ATTRIBUTES = new LinkedList<String>();
		DB_ATTRIBUTES.add(idDBKEY);
		DB_ATTRIBUTES.add(partnerKEY);
		DB_ATTRIBUTES.add(conclusionDateKEY);
		DB_ATTRIBUTES.add(notificationDateKEY);
		DB_ATTRIBUTES.add(expirationDateKEY);
		DB_ATTRIBUTES.add(notesKEY);
		DB_ATTRIBUTES.add(descriptionKEY);
	}

	public static final java.util.List<String> INTERFACED_ATTRIBUTES;
	static {
		INTERFACED_ATTRIBUTES = new LinkedList<String>();
		INTERFACED_ATTRIBUTES.add(idDBKEY);
		INTERFACED_ATTRIBUTES.add(partnerKEY);
		INTERFACED_ATTRIBUTES.add(conclusionDateKEY);
		INTERFACED_ATTRIBUTES.add(notificationDateKEY);
		INTERFACED_ATTRIBUTES.add(expirationDateKEY);
		INTERFACED_ATTRIBUTES.add(notesKEY);
		// INTERFACED_ATTRIBUTES.add(descriptionKEY);
	}

	public static final java.util.List<String> ALLOWED_ATTRIBUTES;
	// public static final String[] ATTRIBUTES_asArray;
	static {
		ALLOWED_ATTRIBUTES = new LinkedList<String>(DB_ATTRIBUTES);
		ALLOWED_ATTRIBUTES.add(idKEY);
		ALLOWED_ATTRIBUTES.add(identKEY);
		ALLOWED_ATTRIBUTES.add(typeKEY);
	}

	public Table_LicenceContracts() {
		super(INTERFACED_ATTRIBUTES);
	}

	// @Override
	public StringValuedRelationElement integrateRaw(Map<String, Object> m) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		{
			rowmap.put(idDBKEY, rowmap.get(idKEY));
		}
		rowmap.remove(typeKEY);
		rowmap.remove(identKEY);

		return rowmap;
	}
}
