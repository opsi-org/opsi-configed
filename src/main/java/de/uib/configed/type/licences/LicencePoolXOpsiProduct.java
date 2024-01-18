/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.type.licences;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;

public class LicencePoolXOpsiProduct extends Relation {
	public static final String LICENCE_POOL_KEY = LicencepoolEntry.ID_SERVICE_KEY;
	public static final String PRODUCT_ID_KEY = "productId";
	public static final String ID_KEY = "id";
	public static final String PRODUCTS_KEY = "productIds";

	private static final List<String> LICENCE_ATTRIBUTES;
	static {
		LICENCE_ATTRIBUTES = new LinkedList<>();
		LICENCE_ATTRIBUTES.add(LICENCE_POOL_KEY);
		LICENCE_ATTRIBUTES.add(PRODUCT_ID_KEY);
	}

	public static final List<String> SERVICE_ATTRIBUTES = List.of(ID_KEY, PRODUCTS_KEY);

	public LicencePoolXOpsiProduct() {
		super(LICENCE_ATTRIBUTES);
	}

	public void integrateRawFromService(Map<String, Object> m) {
		String licensePoolId = (String) m.get(ID_KEY);

		List<?> productList = (List<?>) m.get(PRODUCTS_KEY);

		for (Object p : productList) {
			String productId = (String) p;
			StringValuedRelationElement rowmap = new StringValuedRelationElement();
			rowmap.setAllowedAttributes(LICENCE_ATTRIBUTES);
			rowmap.put(LicencepoolEntry.ID_SERVICE_KEY, licensePoolId);
			rowmap.put(PRODUCT_ID_KEY, productId);
			add(rowmap);
		}
	}
}
