package de.uib.utilities.tree;

import java.util.*;
import javax.swing.tree.*;

public class SimpleTreePath extends ArrayList<String>
	implements Comparable<SimpleTreePath>
{
	
	public SimpleTreePath()
	{
		super();
	}
	
	public SimpleTreePath(Collection<String> prototype)
	{
		super(prototype);
	}
	
	public SimpleTreePath(Object[] path)
	{
		super();
		for (int i = 0; i < path.length; i++)
		{
			add(path[i].toString());
		}
	}
	
	public java.util.Set<String> collectNodeNames()
	{
		HashSet<String> set = new HashSet<String>();
		
		for (String nodename : this)
		{
			set.add( nodename );
		}
		
		return set;
	}
	
	
	public SimpleTreePath subList(int j,  int i)
	{
		return new SimpleTreePath (super.subList(j, i));
	}
	
	
	public String dottedString(int j,  int i)
	{
		StringBuffer buf = new StringBuffer("");
		
		for (int k = j; k < i - 1; k++)
		{
			buf.append(get(k));
			buf.append(".");
		}
		buf.append(get(i-1));
		
		return buf.toString();
	}
	
	
	public static String dottedString(int startNodeNo, TreePath path)
	{
		Object[] parts = path.getPath();
		
		if (parts.length <= startNodeNo)
			return "";
		
		StringBuffer res = new StringBuffer(parts[startNodeNo].toString());
		
		for (int i = startNodeNo + 1; i < parts.length; i++)
		{
			res.append(".");
			res.append(parts[i].toString());
		}
		
		return res.toString();
	}
		
	
	/*
	public boolean equals(Object o)
	{
		boolean result = true;
		
		if (o == null)
			result = false;
		
		if (o instanceof SimpleTreePath)
			result = false;
		
		if (result)
		{
			SimpleTreePath ob = (SimpleTreePath) o;
			
			if (size()!= ob.size())
				result = false;
			
			int i = 0;
			while (result && i < size())
			{
				result = (get(i) == ob.get(i));
			}
		}
		
		return result;
	}
	*/

	
	//interface Comparable
	public int compareTo(SimpleTreePath o)
	{
		int result = 0;
		int i = 0;
		while (result == 0 && i < size() && i < o.size()) 
		{
			result = get(i).compareTo(o.get(i));
			i++;
		}
		
		if (result == 0  && (size() < o.size()))
			result = -1;
		
		if (result == 0  && (size() > o.size()))
			result = +1;
		
		return result;
	}
	
	
	
}
