package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.datastructure.*;

public class Table_LicenceContracts extends Relation
{

/*
	describe LICENSE_CONTRACT ;
	| Field             		| Type          		| Null 	| Key 	| Default             		| Extra 
	| licenseContractId 	| varchar(100)  	| NO   	| PRI 	| NULL                		|       
	| partner           		| varchar(100)  	| YES  	|     		| NULL                		|       
	| conclusionDate    	| timestamp     	| NO   	|     		| 0000-00-00 00:00:00 |       
	| notificationDate  		| timestamp     	| NO   	|     		| 0000-00-00 00:00:00 |       
	| expirationDate    	| timestamp     	| NO   	|     		| 0000-00-00 00:00:00 |       
	| notes             		| varchar(1000) 	| YES  	|     		| NULL                		|       
	| type              		| varchar(30)   	| NO   	| MUL 	| NULL                		|       
	| description       | varchar(100)  | NO   	|     		| NULL     |       				|
	
	*/
	
	
	public final static String  idKEY = "id";
	public final static String  identKEY = "ident";
	public final static String  idDBKEY = "licenseContractId";
	public final static String  partnerKEY = "partner";
	public final static String  conclusionDateKEY = "conclusionDate";
	public final static String  notificationDateKEY = "notificationDate";
	public final static String  expirationDateKEY = "expirationDate";
	public final static String  notesKEY = "notes";
	public final static String  descriptionKEY = "description";
	
	public static final String opsiNOMtype = "LicenseContract";
	public static final String typeKEY = "type";
	
	
	
	public final static java.util.List<String> DB_ATTRIBUTES;
	//public final static String[] ATTRIBUTES_asArray;
	static{
		DB_ATTRIBUTES = new  LinkedList<String>();
		DB_ATTRIBUTES.add(idDBKEY);
		DB_ATTRIBUTES.add(partnerKEY);
		DB_ATTRIBUTES.add(conclusionDateKEY);
		DB_ATTRIBUTES.add(notificationDateKEY);
		DB_ATTRIBUTES.add(expirationDateKEY);
		DB_ATTRIBUTES.add(notesKEY);
		DB_ATTRIBUTES.add(descriptionKEY);
	}
	
	
	public final static java.util.List<String> INTERFACED_ATTRIBUTES;
	static{
		INTERFACED_ATTRIBUTES = new  LinkedList<String>();
		INTERFACED_ATTRIBUTES.add(idDBKEY);
		INTERFACED_ATTRIBUTES.add(partnerKEY);
		INTERFACED_ATTRIBUTES.add(conclusionDateKEY);
		INTERFACED_ATTRIBUTES.add(notificationDateKEY);
		INTERFACED_ATTRIBUTES.add(expirationDateKEY);
		INTERFACED_ATTRIBUTES.add(notesKEY);
		//INTERFACED_ATTRIBUTES.add(descriptionKEY);
	}
	
	
	public final static java.util.List<String> ALLOWED_ATTRIBUTES;
	//public final static String[] ATTRIBUTES_asArray;
	static{
		ALLOWED_ATTRIBUTES = new  LinkedList<String>(
			DB_ATTRIBUTES);
		ALLOWED_ATTRIBUTES.add(idKEY);
		ALLOWED_ATTRIBUTES.add(identKEY);
		ALLOWED_ATTRIBUTES.add(typeKEY);
	}
	
	public Table_LicenceContracts()
	{
		super(INTERFACED_ATTRIBUTES);
	}
	
	//@Override
	public StringValuedRelationElement integrateRaw(Map<String,Object> m)
	{
		StringValuedRelationElement rowmap = new StringValuedRelationElement();
		rowmap.setAllowedAttributes(INTERFACED_ATTRIBUTES);
		{
			rowmap.put(idDBKEY, rowmap.get(idKEY));
		}
		rowmap.remove(typeKEY);
		rowmap.remove(identKEY);
		
		return rowmap;
	}
}
