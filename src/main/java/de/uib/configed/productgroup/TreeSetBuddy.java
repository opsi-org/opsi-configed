package de.uib.configed.productgroup;

import java.util.*;

public class TreeSetBuddy extends TreeSet<String>
{
	public TreeSetBuddy(Collection<String> c)
	{
		super(c);
	}
	
	@Override public boolean equals(Object o)
	{
		return super.equals(o);
	}
	

	public boolean equals(TreeSetBuddy other)
	{
		if (other == null)
			return false;
		
		if (other.size() != size())
			return false;
		
		boolean equal = true;
		
		Iterator<String> iter = this.iterator();
		Iterator<String> otherIter = other.iterator();
		
		while (equal && iter.hasNext())
		{
			String str0 = iter.next();
			String str1 = otherIter.next();
			//System.out.println(" str0 -- str1 " + str0 + " --- " + str1);
			
			if ( !str0.equals(str1) )
				equal = false;
			
		}
		
		if (equal && otherIter.hasNext())
			equal = false;
		
		return equal;
	}
	
	private static void testWith()
	{
		
		TreeSetBuddy test1 = null;
		TreeSetBuddy test2 = null;
		
		
		test1 = new TreeSetBuddy(Arrays.asList("a","b","c"));
		test2 = new TreeSetBuddy(Arrays.asList("a","b","c"));
		assert(test1.equals(test2));
		
		test2 = new TreeSetBuddy(Arrays.asList("a","b"));
		assert(!test1.equals(test2));
		
		test2 = new TreeSetBuddy(Arrays.asList("a","b","c","d"));
		assert(!test1.equals(test2));
		
		test2 = new TreeSetBuddy(Arrays.asList("a","b","e"));
		assert(!test1.equals(test2));
	}
	
	private static void testWithout()
	{
		TreeSet stest1 = null;
		TreeSet stest2 = null;
		
		stest1 = new TreeSet(Arrays.asList("a","b","c","d","e","f","g","h","i","j"));
		stest2 = new TreeSet(Arrays.asList("a","b","c","d","e","f","g","h","i","j"));
		
		
		assert(stest1.equals(stest2));
		
		stest2 = new TreeSet(Arrays.asList("a","b"));
		assert(!stest1.equals(stest2));
		
		stest2 = new TreeSet(Arrays.asList("a","b","c","d"));
		assert(!stest1.equals(stest2));
		
		stest2 = new TreeSet(Arrays.asList("a","b","e"));
		assert(!stest1.equals(stest2));
	}	
		
	
	public static void main(String[] args)
	{
		long startmillis = 0;
		long endmillis = 0;
		
		
		testWith();
		testWithout();
		
		System.out.println(" without knowledge");
		
		startmillis = System.currentTimeMillis();
		System.out.println(" startmillis " + startmillis);
		
		
		for (int i = 0; i<100; i++)
		{
			testWithout();
		}
		
		endmillis = System.currentTimeMillis();
		
		System.out.println("endmillis " + endmillis +  " diff " + (endmillis - startmillis) );
	
	
		System.out.println(" withknowledge");
		startmillis = System.currentTimeMillis();
		
		for (int i = 0; i<100; i++)
		{
			testWith();
		}
		endmillis = System.currentTimeMillis();
		System.out.println("endmillis " + endmillis +  " diff " + (endmillis - startmillis) );
		
		
	}
	
	
	
}
		
			
		
