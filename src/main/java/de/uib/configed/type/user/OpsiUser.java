/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class OpsiUser
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes Privileges which can be attributed to users
 *
 */

 
 /* 
 	create table USER
 		(
 			//user_id INTEGER NOT NULL AUTO_INCREMENT,
 			user_name VARCHAR(300)
 			user_description VARCHAR(2000),
 			user_lastchanged TIMESTAMP
 			
 			//PRIMARY KEY (user_id,)
 		}
 
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class OpsiUser
{
	private Integer id;
	private String name;
	private String description;
	private java.sql.Timestamp lastchanged;
	
	public final static String COL_ID = "user_id";
	public final static String COL_NAME = "user_name";
	public final static String COL_DESCRIPTION = "user_description";
	public final static String COL_LASTCHANGED = "user_lastchanged";
	
	public final static String[] COLNAMES = new String[] 
		{COL_ID, COL_NAME, COL_DESCRIPTION, COL_LASTCHANGED};
	
	//public static OpsiUser ADMINUSER = new OpsiUser("adminuser", "default opsi administrator", null);
	//can be overridden to be a user in database
	
	
	public OpsiUser(String name)
	{
		this(name, name, null);
	}
	
	public OpsiUser(String name, String description, Long lastchanged)
	{
		logging.info(this, "created " + name) ;
		//this.id = id;
		this.name = name;
		this.description = description;
		if (lastchanged == null)
			this.lastchanged = new java.sql.Timestamp(new Date().getTime());
		else
			this.lastchanged = new java.sql.Timestamp(lastchanged);
			
	}
	
}
