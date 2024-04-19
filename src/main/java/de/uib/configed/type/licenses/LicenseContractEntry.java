/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licenses;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.utils.datastructure.StringValuedRelationElement;

public class LicenseContractEntry extends StringValuedRelationElement {
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

	private static final List<String> ALLOWED_ATTRIBUTES;
	static {
		ALLOWED_ATTRIBUTES = new LinkedList<>(DB_ATTRIBUTES);
		ALLOWED_ATTRIBUTES.add(ID_KEY);
		ALLOWED_ATTRIBUTES.add(IDENT_KEY);
		ALLOWED_ATTRIBUTES.add(TYPE_KEY);
	}

	public LicenseContractEntry() {
		super();
		super.setAllowedAttributes(ALLOWED_ATTRIBUTES);
	}

	public LicenseContractEntry(Map<String, Object> m) {
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

		put(ID_DB_KEY, get(ID_KEY));
		remove(TYPE_KEY);
		remove(IDENT_KEY);

		put(CONCLUSION_DATE_KEY, removeTime(get(CONCLUSION_DATE_KEY)));

		put(NOTIFICATION_DATE_KEY, removeTime(get(NOTIFICATION_DATE_KEY)));
		put(EXPIRATION_DATE_KEY, removeTime(get(EXPIRATION_DATE_KEY)));
	}

	public String getId() {
		return get(ID_DB_KEY);
	}
}
