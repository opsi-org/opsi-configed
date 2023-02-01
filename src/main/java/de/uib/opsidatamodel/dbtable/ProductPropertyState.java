package de.uib.opsidatamodel.dbtable;

// TODO what to do with it? can can it be removed? Where store the values?
public class ProductPropertyState extends Table {
	public static final String TABLE_NAME = "PRODUCT_PROPERTY_STATE";

	public static final String PRODUCT_ID = "productId";
	public static final String PROPERTY_ID = "propertyId";
	public static final String OBJECT_ID = "objectId";
	public static final String VALUES = "values";

	private ProductPropertyState() {
		super("");
	}
}
