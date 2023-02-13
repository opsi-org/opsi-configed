package de.uib.configed.type.licences;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;

public class LicencePoolXOpsiProduct extends Relation {
	/*
	 * PRODUCT_ID_TO_LICENSE_POOL:
	 * | licensePoolId | varchar(100) | NO | PRI | NULL | |
	 * | productId | varchar(255) | NO | PRI | | |
	 * 
	 */

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

	private static final List<String> SERVICE_ATTRIBUTES;
	static {
		SERVICE_ATTRIBUTES = new LinkedList<>();
		SERVICE_ATTRIBUTES.add(ID_KEY);
		SERVICE_ATTRIBUTES.add(PRODUCTS_KEY);
	}

	public static final String[] SERVICE_ATTRIBUTES_asArray;
	static {
		SERVICE_ATTRIBUTES_asArray = SERVICE_ATTRIBUTES.toArray(new String[] {});
	}

	public LicencePoolXOpsiProduct() {
		super(LICENCE_ATTRIBUTES);
	}

	public void integrateRawFromService(Map<String, Object> m) {
		String licensePoolId = (String) m.get(ID_KEY);
		try {

			List<Object> productList = ((org.json.JSONArray) m.get(PRODUCTS_KEY)).toList();

			for (Object p : productList) {
				String productId = (String) p;
				StringValuedRelationElement rowmap = new StringValuedRelationElement();
				rowmap.setAllowedAttributes(LICENCE_ATTRIBUTES);
				rowmap.put(LicencepoolEntry.ID_SERVICE_KEY, licensePoolId);
				rowmap.put(PRODUCT_ID_KEY, productId);
				add(rowmap);
			}
		} catch (Exception ex) {
			Logging.error("integrateRawFromService " + m + " exception " + ex);
		}
	}
}
