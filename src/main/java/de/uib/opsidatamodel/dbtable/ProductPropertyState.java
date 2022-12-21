package de.uib.opsidatamodel.dbtable;

import java.io.File;
import java.util.ArrayList;

import de.uib.configed.Globals;

public class ProductPropertyState extends Table {
	public static final String tableName = "PRODUCT_PROPERTY_STATE";

	public static final String version = ProductPropertyState.class.getName() + " " + Globals.VERSION;

	public static final String PRODUCT_ID = "productId";
	public static final String PROPERTY_ID = "propertyId";
	public static final String OBJECT_ID = "objectId";
	public static final String VALUES = "values";

	public static java.util.List<String> columns;
	static {
		columns = new ArrayList<>();
		// columns.add("product_property_state_id");
		columns.add(PRODUCT_ID);
		columns.add(PROPERTY_ID);
		columns.add(OBJECT_ID);
		columns.add(VALUES);
	}
	/*
	 * public static String columnsString;
	 * static{
	 * columnsString = Arrays.toString( columns.toArray( new String[]{} ) ) ;
	 * columnsString = columnsString.substring(1);
	 * columnsString = columnsString.substring(0, columnsString.length()-1);
	 * }
	 */

	public static java.util.List<String> primaryKey;
	public static String primaryKeyString;
	static {
		primaryKey = new ArrayList<>();
		primaryKey.add("product_property_state_id");

		StringBuffer sb = new StringBuffer("");
		for (String key : primaryKey) {
			sb.append(key);
			sb.append(";");
		}
		primaryKeyString = sb.toString();
	}

	public ProductPropertyState(String localTablePath) {
		super(localTablePath);
		this.localTablePath = localTablePath + File.separator + localFileName;
	}
}
