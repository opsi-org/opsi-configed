/* 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */
 

package de.uib.utilities.table;
import java.util.*;
import de.uib.utilities.logging.*;

public class ChainedTableModelFilter extends TableModelFilter
{
	LinkedHashMap<String, TableModelFilter> chain;
	
	public ChainedTableModelFilter()
	{
		chain = new LinkedHashMap<String, TableModelFilter>();
	}
		
	public ChainedTableModelFilter set(String filterName, TableModelFilter filter)
	{
		chain.put(filterName, filter);
		return this;
	}
	
	public void clear()
	{
		chain.clear();
	}
	
	public boolean hasFilterName(String name)
	{
		return chain.containsKey(name);
	}
	
	
	public TableModelFilter getElement(String name)
	{
		return chain.get(name);
	}
	
	@Override
	public boolean isInUse()
	{
		boolean result = false;
		
		for (String filterName : chain.keySet())
		{
			if (chain.get(filterName).isInUse())
			{
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	@Override
	public boolean test(Vector<Object> row)
	{
		if (!inUse)
			return true;
		
		boolean testresult = true;
		
		for (String filterName : chain.keySet())
		{
			if (chain.get(filterName).isInUse())
			{
				testresult = testresult && chain.get(filterName).test(row);
			}
			//logging.info(this, "test result, filtered by "  + filterName + ", "  + testresult);
		}
		
		if (inverted)
		{
			testresult = !testresult;
		}
		
		
		return testresult;
	}
			
	public String getActiveFilters()
	{
		StringBuffer result = new StringBuffer();
		
		for (String filterName : chain.keySet())
		{
			if (chain.get(filterName).isInUse())
			{
				result.append(" - ");
				result.append(filterName);
			}
		}
		
		return result.toString();
	}
		
	
	@Override
	public String toString()
	{
		return  getClass().getName() + ", chain is: " + chain;
	}
	
}
			
			