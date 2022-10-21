/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

public class TableEditItem
{
	protected Object source;
	protected int keyCol = -1;
	
	
	public Object getSource()
	{
		return source;
	}
	
	public void setSource(Object o)
	{
		source = o;
	}
	
	public boolean keyChanged()
	{
		return true;
	}
	
}
