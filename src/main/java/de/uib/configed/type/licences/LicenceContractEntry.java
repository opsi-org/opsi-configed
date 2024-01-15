/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licences;

import java.util.Map;

import de.uib.utilities.datastructure.StringValuedRelationElement;

public class LicenceContractEntry extends StringValuedRelationElement {
	public LicenceContractEntry() {
		super();
		super.setAllowedAttributes(TableLicenceContracts.ALLOWED_ATTRIBUTES);
	}

	public LicenceContractEntry(Map<String, Object> m) {
		this();
		produceFrom(m);
	}

	private static String removeTime(String datetime) {
		if (datetime != null && datetime.length() > 0) {
			int idx = datetime.indexOf(" ");
			if (idx > -1) {
				return datetime.substring(0, idx);
			}
		}

		return datetime;
	}

	@Override
	protected final void produceFrom(Map<String, ? extends Object> map) {
		super.produceFrom(map);

		put(TableLicenceContracts.ID_DB_KEY, get(TableLicenceContracts.ID_KEY));
		remove(TableLicenceContracts.TYPE_KEY);
		remove(TableLicenceContracts.IDENT_KEY);

		put(TableLicenceContracts.CONCLUSION_DATE_KEY, removeTime(get(TableLicenceContracts.CONCLUSION_DATE_KEY)));

		put(TableLicenceContracts.NOTIFICATION_DATE_KEY, removeTime(get(TableLicenceContracts.NOTIFICATION_DATE_KEY)));
		put(TableLicenceContracts.EXPIRATION_DATE_KEY, removeTime(get(TableLicenceContracts.EXPIRATION_DATE_KEY)));
	}

	public String getId() {
		return get(TableLicenceContracts.ID_DB_KEY);
	}
}
