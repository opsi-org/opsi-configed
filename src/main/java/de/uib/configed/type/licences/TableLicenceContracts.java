package de.uib.configed.type.licences;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;

public class TableLicenceContracts extends Relation {

	/*
	 * describe LICENSE_CONTRACT 
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

	public static final String ID_KEY = "id";
	public static final String IDENT_KEY = "ident";
	public static final String ID_DB_KEY = "licenseContractId";
	public static final String PARTNER_KEY = "partner";
	public static final String CONCLUSION_DATE_KEY = "conclusionDate";
	public static final String NOTIFICATION_DATE_KEY = "notificationDate";
	public static final String EXPIRATION_DATE_KEY = "expirationDate";
	public static final String NOTES_KEY = "notes";
	public static final String DESCRIPTION_KEY = "description";

	public static final String TYPE_KEY = "type";

	private static final List<String> DB_ATTRIBUTES;

	static {
		DB_ATTRIBUTES = new LinkedList<>();
		DB_ATTRIBUTES.add(ID_DB_KEY);
		DB_ATTRIBUTES.add(PARTNER_KEY);
		DB_ATTRIBUTES.add(CONCLUSION_DATE_KEY);
		DB_ATTRIBUTES.add(NOTIFICATION_DATE_KEY);
		DB_ATTRIBUTES.add(EXPIRATION_DATE_KEY);
		DB_ATTRIBUTES.add(NOTES_KEY);
		DB_ATTRIBUTES.add(DESCRIPTION_KEY);
	}

	private static final List<String> INTERFACED_ATTRIBUTES;
	static {
		INTERFACED_ATTRIBUTES = new LinkedList<>();
		INTERFACED_ATTRIBUTES.add(ID_DB_KEY);
		INTERFACED_ATTRIBUTES.add(PARTNER_KEY);
		INTERFACED_ATTRIBUTES.add(CONCLUSION_DATE_KEY);
		INTERFACED_ATTRIBUTES.add(NOTIFICATION_DATE_KEY);
		INTERFACED_ATTRIBUTES.add(EXPIRATION_DATE_KEY);
		INTERFACED_ATTRIBUTES.add(NOTES_KEY);

	}

	public static final List<String> ALLOWED_ATTRIBUTES;

	static {
		ALLOWED_ATTRIBUTES = new LinkedList<>(DB_ATTRIBUTES);
		ALLOWED_ATTRIBUTES.add(ID_KEY);
		ALLOWED_ATTRIBUTES.add(IDENT_KEY);
		ALLOWED_ATTRIBUTES.add(TYPE_KEY);
	}

	public TableLicenceContracts() {
		super(INTERFACED_ATTRIBUTES);
	}

	@Override
	public StringValuedRelationElement integrateRaw(Map<String, Object> m) {
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);

		rowmap.put(ID_DB_KEY, rowmap.get(ID_KEY));

		rowmap.remove(TYPE_KEY);
		rowmap.remove(IDENT_KEY);

		return rowmap;
	}
}
