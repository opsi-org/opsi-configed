package de.uib.configed.type.licences;

import java.util.LinkedList;
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

	public static final java.util.List<String> ATTRIBUTES;
	static {
		ATTRIBUTES = new LinkedList<String>();
		ATTRIBUTES.add(licencepoolKEY);
		ATTRIBUTES.add(productIdKEY);
	}

	public static final java.util.List<String> SERVICE_ATTRIBUTES;
	static {
		SERVICE_ATTRIBUTES = new LinkedList<String>();
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
	 * de.uib.utilities.Globals.pseudokey(new String[]{
	 * de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.NAME ) ),
	 * de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.VERSION ) ),
	 * de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.SUBVERSION ) ),
	 * de.uib.utilities.Globals.getStringValue( m.get(SWAuditEntry.LANGUAGE ) ),
	 * de.uib.utilities.Globals.getStringValue( m.get (SWAuditEntry.ARCHITECTURE ) )
	 * }
	 * );
	 * }
	 */

	public void integrateRawFromService(Map<String, Object> m) {
		String licensePoolId = (String) m.get(idKEY);
		try {
			// logging.info(this, "integrateRawFromService " + m );

			java.util.List<Object> productList = ((org.json.JSONArray) m.get(productsKEY)).toList();

			for (Object p : productList) {
				String productId = (String) p;
				StringValuedRelationElement rowmap = new StringValuedRelationElement();
				rowmap.setAllowedAttributes(ATTRIBUTES);
				rowmap.put(LicencepoolEntry.idSERVICEKEY, licensePoolId);
				rowmap.put(productIdKEY, productId);
				add(rowmap);
				// logging.info(this, "integrateRawFromService given rowmap" + rowmap);
			}
		} catch (Exception ex) {
			logging.error("integrateRawFromService " + m + " exception " + ex);
		}
	}

}
