package de.uib.configed.type;

import java.util.*;
import de.uib.utilities.logging.logging;


public class DateExtendedByVars extends java.sql.Date
{
	
	public static String MINUS = "minus";
	public static char varDelimiter = '%';
	
	//we use only the public function
	private DateExtendedByVars(long date)
	{
		super(date);
	}
		
	
	private static String stripTimeFromDay(String datetime)
	{
		
		if (datetime == null)
			return null;
		
		int idx = datetime.indexOf(" "); 
		if ( idx < 0 )
			return datetime;
			
		return datetime.substring(0, idx);
	}
	
	
	private static String interpretVar(final String s)
	{
		logging.debug("OpsiDataDateMatcher interpretVar in " + s);
		
		int i = s.indexOf(varDelimiter);
		
		if (i == -1)
			return s;
		
		i++;
		
		if (i > s.length())
		{
			logging.info("OpsiDataDateMatcher interpretVar \"" + varDelimiter
				+ "\" found at end of string");
			return s;
		}
		
		String replaceContent = s.substring(i);
		i = replaceContent.indexOf(varDelimiter);
		
		replaceContent = replaceContent.substring(0, i);
		
		logging.debug("OpsiDataDateMatcher interpretVar replaceContent " + replaceContent);
		
		if (!replaceContent.startsWith(MINUS))
		{
			logging.info("OpsiDataDateMatcher interpretVar expected: \"" + MINUS + "\"");
			return s;
		}
		
		String subtrahendS = replaceContent.substring(MINUS.length());
		
		
		Integer subtrahend = null;
		
		try{
			subtrahend = Integer.valueOf(subtrahendS);
		}
		catch (NumberFormatException ex)
		{
			logging.info("OpsiDataDateMatcher interpretVar not a number: " +subtrahendS +", error: " + ex);
			return s;
		}
		
		Calendar cal = new java.util.GregorianCalendar();
		
		cal.add(Calendar.DAY_OF_MONTH, -subtrahend);
		
		java.util.Date myTime = new java.sql.Timestamp (cal.getTimeInMillis());
		
		String timeS = stripTimeFromDay(myTime.toString());
		
		logging.debug("OpsiDataDateMatcher interpretVar produced time " + timeS); 
		
		String toReplace = varDelimiter + replaceContent + varDelimiter;
		
		return s.replace(toReplace, timeS);
	}

	
	
	public static String dayValueOf( String s)  
	{
		return interpretVar(s);
	}
	
	
	
}



