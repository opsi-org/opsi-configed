/**
 *   OpsiProductInfo
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 *    
 *  copyright:  2014
 *  organization: uib.de
 *  @author  R. Roeder 
 */


package de.uib.configed.type;
import java.util.*;

import de.uib.opsidatamodel.productstate.ActionRequest;

import de.uib.utilities.logging.*;

//data source product table
public class OpsiProductInfo extends OpsiPackage
{
	protected ArrayList<String> possibleActions;
	protected String productName;
	protected String description;
	protected String advice;
	protected boolean hasUserLoginScript;
	protected Integer priority;
	
	public static final int NO_PRIORITY = -101;
	
	public static final String SERVICEkeyPRIORITY = "priority";
	public static final String SERVICEkeyUSER_LOGIN_SCRIPT = "userLoginScript";
	public static final String SERVICEkeyPRODUCT_TYPE = "type";
	public static final String SERVICEkeyPRODUCT_ADVICE = "advice";
	public static final String SERVICEkeyPRODUCT_DESCRIPTION = "description";
	public static final String SERVICEkeyPRODUCT_NAME = "name";
	
	
	
	public OpsiProductInfo(Map<String, Object> m)
	{
		super(m);
		
		possibleActions = new ArrayList<String>();
		possibleActions.add(ActionRequest.getState2Label().get(ActionRequest.NONE));
		//keys are the possible script types
		for (String scriptKey: ActionRequest.getScriptKeys())
		{
			if (m.get(scriptKey) != null &&  !("" + m.get(scriptKey)).equals("") )
				possibleActions.add( ActionRequest.getScriptKey2Label().get( scriptKey)  );
		}
		
		productName = "";
		if ( m.get(SERVICEkeyPRODUCT_NAME) != null )
			productName = "" + m.get(SERVICEkeyPRODUCT_NAME);
		
		description = "";
		if (m.get(SERVICEkeyPRODUCT_DESCRIPTION) != null)
			description = "" + m.get(SERVICEkeyPRODUCT_DESCRIPTION);
		
		//logging.info(this, " description " + description);
			
		advice = "";
		if (m.get(SERVICEkeyPRODUCT_ADVICE) != null)
			advice = "" + m.get(SERVICEkeyPRODUCT_ADVICE) ;
		
		//logging.info(this, " advice " + advice);
		
		try
		{
			priority = Integer.valueOf("" + m.get(SERVICEkeyPRIORITY));
		}
		catch(NumberFormatException ex)
		{
			logging.info(this, "no priority " + m.get(SERVICEkeyPRIORITY));
		}
		
		hasUserLoginScript = (
			m.get(SERVICEkeyUSER_LOGIN_SCRIPT) !=  null
			&&
			!("" + m.get(SERVICEkeyUSER_LOGIN_SCRIPT)).equals("")
			);

		logging.debug(this, "created with description " + description + "\n,"
			+ " possible actions " +  possibleActions  
			+ ", hasUserLoginScript " + hasUserLoginScript);
		//System.exit(0);
		
	
	}
	
	
	public java.util.List getPossibleActions()
	{
		return possibleActions;
	}
	
	public boolean hasLoginScript()
	{
		return hasUserLoginScript;
	}
	
	public String getProductName()
	{
		return productName;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getAdvice()
	{
		return advice;
	}
	
	public int getPriority()
	{
		if (priority == null) 
			return NO_PRIORITY;
		else 
			return priority;
	}
	
	
	public Vector<Object> appendValues(Vector<Object> row)
	{
		row.add(getProductName());
		return row;
	}
		

	@Override
	public String toString()
	{
		return  getClass().getName() 
		+ "{productId=" + productId
		+  ";productName=" + productName + ";description=" + description 
		+ ";possibleActions" + possibleActions + ";hasUserLoginScript=" + hasUserLoginScript
		+ ";priority=" + priority
		+ ";description=" + description
		+ ";advice=" + advice
		+"}";
	}
}
	

