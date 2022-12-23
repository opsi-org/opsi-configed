package de.uib.configed.type.licences;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.utilities.datastructure.Relation;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.logging;

public class LicencePoolXOpsiProduct extends Relation {
	/*
	 * PRODUCT_ID_TO_LICENSE_POOL;
	 * | licensePoolId | varchar(100) | NO | PRI | NULL | |
	 * | productId | varchar(255) | NO | PRI | | |
	 * 
	 */

	public static final String licencepoolKEY = LicencepoolEntry.idSERVICEKEY;
	public static final String productIdKEY = "productId";
	public static final String idKEY = "id";
	public static final String productsKEY = "productIds";

	public static final List<String> ATTRIBUTES;
	static {
		ATTRIBUTES = new LinkedList<>();
		ATTRIBUTES.add(licencepoolKEY);
		ATTRIBUTES.add(productIdKEY);
	}

	public static final List<String> SERVICE_ATTRIBUTES;
	static {
		SERVICE_ATTRIBUTES = new LinkedList<>();
		SERVICE_ATTRIBUTES.add(idKEY);
		SERVICE_ATTRIBUTES.add(productsKEY);
	}

	public static final String[] SERVICE_ATTRIBUTES_asArray;
	static {
		SERVICE_ATTRIBUTES_asArray = SERVICE_ATTRIBUTES.toArray(new String[] {});
	}

	public LicencePoolXOpsiProduct() {
		super(ATTRIBUTES);
	}

	/*
	 * private String produceSWident(Map<String, Object> m)
	 * {
	 * return
	 * Globals.pseudokey(new String[]{
	 * Globals.getStringValue( m.get(SWAuditEntry.NAME ) ),
	 * Globals.getStringValue( m.get(SWAuditEntry.VERSION ) ),
	 * Globals.getStringValue( m.get(SWAuditEntry.SUBVERSION ) ),
	 * Globals.getStringValue( m.get(SWAuditEntry.LANGUAGE ) ),
	 * Globals.getStringValue( m.get (SWAuditEntry.ARCHITECTURE ) )
	 * }
	 * );
	 * }
	 */

	public void integrateRawFromService(Map<String, Object> m) {
		String licensePoolId = (String) m.get(idKEY);
		try {
			

			List<Object> productList = ((org.json.JSONArray) m.get(productsKEY)).toList();

			for (Object p : productList) {
				String productId = (String) p;
				StringValuedRelationElement rowmap = new StringValuedRelationElement();
				rowmap.setAllowedAttributes(ATTRIBUTES);
				rowmap.put(LicencepoolEntry.idSERVICEKEY, licensePoolId);
				rowmap.put(productIdKEY, productId);
				add(rowmap);
				
			}
		} catch (Exception ex) {
			logging.error("integrateRawFromService " + m + " exception " + ex);
		}
	}

}
