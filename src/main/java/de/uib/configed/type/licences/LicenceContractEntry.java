package de.uib.configed.type.licences;

import java.util.Map;

import de.uib.utilities.datastructure.StringValuedRelationElement;

public class LicenceContractEntry extends StringValuedRelationElement {
	public LicenceContractEntry() {
		super();
		allowedAttributes = Table_LicenceContracts.ALLOWED_ATTRIBUTES;
	}

	public LicenceContractEntry(Map<String, Object> m) {
		super();
		setAllowedAttributes(Table_LicenceContracts.ALLOWED_ATTRIBUTES);
		produceFrom(m);
	}

	private String removeTime(String datetime) {
		if (datetime != null && datetime.length() > 0) {
			int idx = datetime.indexOf(" ");
			if (idx > -1)
				return datetime.substring(0, idx);
		}

		return datetime;
	}

	@Override
	protected void produceFrom(Map<String, ? extends Object> map) {
		super.produceFrom(map);

		put(Table_LicenceContracts.ID_DB_KEY, get(Table_LicenceContracts.ID_KEY));
		remove(Table_LicenceContracts.TYPE_KEY);
		remove(Table_LicenceContracts.IDENT_KEY);

		put(Table_LicenceContracts.CONCLUSION_DATE_KEY, removeTime(get(Table_LicenceContracts.CONCLUSION_DATE_KEY)));

		put(Table_LicenceContracts.NOTIFICATION_DATE_KEY,
				removeTime(get(Table_LicenceContracts.NOTIFICATION_DATE_KEY)));
		put(Table_LicenceContracts.EXPIRATION_DATE_KEY, removeTime(get(Table_LicenceContracts.EXPIRATION_DATE_KEY)));

	}

	public String getId() {
		return get(Table_LicenceContracts.ID_DB_KEY);
	}
}
