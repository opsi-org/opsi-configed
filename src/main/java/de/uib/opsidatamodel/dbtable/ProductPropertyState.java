package de.uib.opsidatamodel.dbtable;

import java.sql.*;
import java.io.File;

import java.util.*;
import de.uib.utilities.table.*;
import de.uib.utilities.logging.logging;

public class ProductPropertyState extends Table
{
	public final static String tableName = "PRODUCT_PROPERTY_STATE";
	
	public final static String version 
		= ProductPropertyState.class.getName() + " " +  de.uib.configed.Globals.VERSION;
	
	public final static String PRODUCT_ID = "productId";
	public final static String PROPERTY_ID = "propertyId";
	public final static String OBJECT_ID = "objectId";
	public final static String VALUES = "values";
	
		
	public static java.util.List<String> columns;
	static {
		columns = new ArrayList<String>();
		//columns.add("product_property_state_id");
		columns.add(PRODUCT_ID);
		columns.add(PROPERTY_ID);
		columns.add(OBJECT_ID);
		columns.add(VALUES);
	}
	/*
	public static String columnsString;
	static{
		columnsString = Arrays.toString( columns.toArray( new String[]{} )  ) ;
		columnsString = columnsString.substring(1);
		columnsString = columnsString.substring(0, columnsString.length()-1);
	}
	*/
	
	public static java.util.List<String> primaryKey;
	public static String primaryKeyString;
	static{
		primaryKey = new ArrayList<String>();
		primaryKey.add("product_property_state_id");
		
		StringBuffer sb = new StringBuffer("");
		for (String key : primaryKey)
		{
			sb.append(key);
			sb.append(";");
		}
		primaryKeyString = sb.toString();
	}
	
	
	public ProductPropertyState(String localTablePath) 
	{
		super(localTablePath);
		this.localTablePath = localTablePath + File.separator + localFileName;
	}
}
